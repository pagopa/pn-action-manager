const { SQSClient, SendMessageBatchCommand } = require("@aws-sdk/client-sqs");
const { EventBridgeClient, PutEventsCommand } = require("@aws-sdk/client-eventbridge");
const { NodeHttpHandler } = require("@aws-sdk/node-http-handler");
const { v4: uuidv4 } = require("uuid");
const config = require("config");
const { isTimeToLeave } = require("../utils/utils.js");
const { ActionUtils } = require("pn-action-common");

const MAX_EVENT_BRIDGE_BATCH = config.get("MAX_EVENT_BRIDGE_BATCH");
const DEFAULT_SOCKET_TIMEOUT = config.get("timeout.DEFAULT_SOCKET_TIMEOUT");
const DEFAULT_REQUEST_TIMEOUT = config.get("timeout.DEFAULT_REQUEST_TIMEOUT");
const DEFAULT_CONNECTION_TIMEOUT = config.get("timeout.DEFAULT_CONNECTION_TIMEOUT");
const TIMEOUT_EXCEPTIONS = config.get("TIMEOUT_EXCEPTIONS");
const BUS_NAME = config.get("BUS_NAME");

const sqs = new SQSClient({
    requestHandler: new NodeHttpHandler({
      connectionTimeout: DEFAULT_CONNECTION_TIMEOUT,
      requestTimeout: DEFAULT_REQUEST_TIMEOUT,
      socketTimeout: DEFAULT_SOCKET_TIMEOUT,
    }),
});
const eventBridgeclient = new EventBridgeClient({});

async function writeMessagesToEventBridge(immediateActions, context) {
  console.log("Starting writeMessagesToEventBridge");
  while (immediateActions.length > 0 && !isTimeToLeave(context)) {
    console.log(
      "Proceeding to send " + immediateActions.length + " messages to eventBridge"
    );

    let splicedActionsArray = immediateActions.splice(0, MAX_EVENT_BRIDGE_BATCH);
    let actionsToSendMapped = getMappedMessageToSend(splicedActionsArray);

    const command = createBatchCommand(actionsToSendMapped);

    try {
      checkMandatoryInformation(actionsToSendMapped);
      const response = await eventBridgeclient.send(command);
      console.log("Sent message response: %j", response);

      if (response.Failed && response.Failed.length > 0) {
        return checkAndReturnFailedAction(splicedActionsArray, response);
      }
    }
    catch (exceptions) {
      console.error("Error in send event bridge message ", exceptions);
      console.log("Stringfy exception ", JSON.stringify(exceptions));
      if (exceptions.name && TIMEOUT_EXCEPTIONS.includes(exceptions.name)) {
        console.warn(
          "[ACTION_ROUTER]",
          "Timeout detected for:",
          JSON.stringify(actionsToSendMapped)
        );
        let actionTimeoutDlqUrl = config.get("ACTION_TIMEOUT_ERROR_DLQ_URL");
        await writeMessagesToSqsWithoutReturnFailed(actionsToSendMapped, actionTimeoutDlqUrl);

      }else{
        console.info('Generic exception in SQS send message, need to reschedule');
        return splicedActionsArray; //Non si conoscono gli item specifici falliti, viene restituito tutto il batch
      }
    }
  }

  console.log("Ending writeMessagesToQueue with arrayActionNotSended length", immediateActions.length);
  return immediateActions;

};

function checkAndReturnFailedAction(splicedActionsArray, response){
  console.log('There is an error in sending message ', response.Failed)
  let failedActionArray = [];

  console.log('Start find error in actionToSend ',JSON.stringify(splicedActionsArray) )

  splicedActionsArray.forEach((element) => {
    if (
      response.Failed.filter((currFailed) => currFailed.Id == element.Id)
        .length !== 0
    )
    failedActionArray.push(element); //Se fallisce nella put
  });

  return failedActionArray; //viene restituito l'array delle action Fallite
}

function checkMandatoryInformation(actionsToSendMapped){

  if (!actionsToSendMapped || actionsToSendMapped.length === 0){
    console.debug("message to send cannot be empty need to reschedule actions ", JSON.stringify(actionsToSendMapped));
    throw new Error("message to send cannot be empty");
  }
}

function getMappedMessageToSend(splicedActionsArray){
  let actionsToSendMapped = [];
  splicedActionsArray.forEach(function (action) {
    let messageToSend = mapActionToEventBridgeMessage(action);
    action.Id = messageToSend.Id;
    actionsToSendMapped.push(messageToSend);
  });
  return actionsToSendMapped;
}

function mapActionToEventBridgeMessage(action) {
    let uuid = uuidv4();
    let copiedAction = Object.assign({}, action);
    delete copiedAction.kinesisSeqNo;
    delete copiedAction.ttl;

    if(copiedAction.details){
      copiedAction.details.actionType = action.type;
    }

    console.log("copiedAction", JSON.stringify(copiedAction));

    const message = {
      Source: "deliveryPush",
      Resources: {
        createdAt: new Date().toISOString(),
        eventId: uuid,
        eventType: "ACTION_GENERIC",
        iun: action.iun
      },
      DetailType: ActionUtils.getCompleteActionType(action?.type, action?.details),
      Detail: JSON.stringify(copiedAction)
    };
    return message;
}

function createBatchCommand(actionsToSendMapped){
  console.log("Sending batch message: %j", actionsToSendMapped);
  const command = new PutEventsCommand({
    Entries: actionsToSendMapped.map(msg => ({
      ...msg,
      EventBusName: BUS_NAME
    }))
  });
  return command;
}

async function writeMessagesToSqsWithoutReturnFailed(actionsToSendMapped, destinationQueueUrl) {
  console.log(
    "writeMessagesToWithoutReturnFailed " +
      actionsToSendMapped.length +
      " messages to " +
      destinationQueueUrl
  );
  const command = createBatchSqsCommand(actionsToSendMapped, destinationQueueUrl);

  try {
    checkMandatorySqsInformation(actionsToSendMapped, destinationQueueUrl);
    const response = await sqs.send(command);
    console.log("Sent message response: %j", response);

    if (response.Failed && response.Failed.length > 0) {
      console.error(
        "[ACTION_ROUTER]",
        "Insert action failed:",
        JSON.stringify(response.Failed)
      );
    }
  }
  catch (exceptions) {
    console.error("Error in send sqs message ", exceptions);
    console.error(
      "[ACTION_ROUTER]",
      "Insert action failed:",
      JSON.stringify(actionsToSendMapped)
    );
  }
}

function createBatchSqsCommand(actionsToSendMapped, destinationQueueUrl){
  const input = {
    Entries: actionsToSendMapped,
    QueueUrl: destinationQueueUrl,
  };
  console.log("Sending batch message: %j", input);
  return new SendMessageBatchCommand(input);
}

function checkMandatorySqsInformation(actionsToSendMapped, destinationQueueUrl){
  if (!destinationQueueUrl){
    console.debug("Destination SQS queue cannot be empty need to reschedule actions ", JSON.stringify(actionsToSendMapped));
    throw new Error("No SQS queue supplied");
  }

  if (!actionsToSendMapped || actionsToSendMapped.length === 0){
    console.debug("message to send cannot be empty need to reschedule actions ", JSON.stringify(actionsToSendMapped));
    throw new Error("message to send cannot be empty");
  }
}

module.exports = { writeMessagesToEventBridge };