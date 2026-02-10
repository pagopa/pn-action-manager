const { SendMessageBatchCommand, SQSClient } = require("@aws-sdk/client-sqs");
const { EventBridgeClient, PutEventsCommand } = require("@aws-sdk/client-eventbridge");
const { NodeHttpHandler } = require("@aws-sdk/node-http-handler");
const { ActionUtils } = require("pn-action-common");

const { v4 } = require("uuid");
const config = require("config");

const {
  SQSServiceException,
  TimeoutException,
  EventBridgeServiceException,
} = require("./exceptions");

const MAX_EVENT_BRIDGE_BATCH = config.get("MAX_EVENT_BRIDGE_BATCH");
const DEFAULT_SOCKET_TIMEOUT = config.get("timeout.DEFAULT_SOCKET_TIMEOUT");
const DEFAULT_REQUEST_TIMEOUT = config.get("timeout.DEFAULT_REQUEST_TIMEOUT");
const DEFAULT_CONNECTION_TIMEOUT = config.get(
  "timeout.DEFAULT_CONNECTION_TIMEOUT"
);
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

function mapActionToEventBridgeMessage(action) {
  let origAction = Object.assign({}, action);
  origAction.timeslot = action.timeSlot;

  if(origAction.details){
    origAction.details.actionType = action.type;
  }

  console.debug("origAction", origAction);

  delete origAction.timeSlot;
  delete origAction.seqNo;
  const message = {
    Source: "deliveryPush",
    DetailType: ActionUtils.getCompleteActionType(action?.type, action?.details),
    Detail: JSON.stringify(origAction)
  };
  return message;
}

function mapActionToQueueMessage(action) {
  let uuid = v4();
  let origAction = Object.assign({}, action);
  origAction.timeslot = action.timeSlot;

  if(origAction.details){
    origAction.details.actionType = action.type;
  }

  console.debug("origAction", origAction);

  delete origAction.timeSlot;
  delete origAction.seqNo;
  const message = {
    Id: uuid,
    DelaySeconds: 0,
    MessageAttributes: {
      createdAt: {
        DataType: "String",
        StringValue: new Date().toISOString(),
      },
      eventId: {
        DataType: "String",
        StringValue: uuid,
      },
      eventType: {
        DataType: "String",
        StringValue: "ACTION_GENERIC",
      },
      iun: {
        DataType: "String",
        StringValue: action.iun,
      },
      publisher: {
        DataType: "String",
        StringValue: "deliveryPush",
      },
    },
    MessageBody: JSON.stringify(origAction),
  };
  return message;
}

async function putMessages(actions, isTimedOut) {
  while (actions.length > 0 && !isTimedOut()) {
    let currentChunk = actions.splice(0, MAX_EVENT_BRIDGE_BATCH);
    const messagesToSend = currentChunk.map((action) =>
      mapActionToEventBridgeMessage(action)
    );
    try {
      const failedActions = await _sendMessages(messagesToSend, currentChunk);
      if (failedActions.length != 0) {
        console.error(
          "[ACTION_ENQUEUER]",
          "Aborting for an error on sending messages. Failed messages:",
          failedActions
        );
        failedActions.forEach((failedAction) => {
          actions.push(failedAction);
        });

        return actions;
      }
    } catch (ex) {
      if (ex instanceof TimeoutException) {
        const messagesToSend = currentChunk.map((action) =>
          mapActionToQueueMessage(action)
        );
        await manageTimeout(messagesToSend);
      } else {
        console.log(
          "[ACTION_ENQUEUER]",
          "Adding back the current chunk and aborting"
        );
        currentChunk.forEach((element) => {
          delete element.Id;
          actions.push(element);
        });
        console.log("[ACTION_ENQUEUER]", "Remaining actions:", actions);
        return actions;
      }
    }
  } //while
  if (actions.length !== 0)
    console.log(
      "[ACTION_ENQUEUER]",
      "Timeout reached. Remaining actions:",
      actions
    );
  return actions;
}



async function manageTimeout(messagesToSend) {
  try {
    const failed = await sendTimeoutMessages(messagesToSend);

    if (failed.length != 0)
      console.error(
        "[ACTION_ENQUEUER]",
        "Can't write timeouted message on DLQ:",
        JSON.stringify(failed)
      );


  } catch (ex) {
    console.error(
      "[ACTION_ENQUEUER]",
      "Timeout detected for:",
      JSON.stringify(messagesToSend)
    );
  }
}

async function _sendMessages(messages, originalActions) {
  try {
    if (!messages || messages.length === 0)
      throw new Error("No messages to send");

    console.debug(
      "[ACTION_ENQUEUER]",
      `Proceeding to send ${messages.length} messages to event bridge bus ${BUS_NAME}`
    );

    console.log(
      "[ACTION_ENQUEUER]",
      "Sending the following batch of messages:",
      messages
    );

    const command = new PutEventsCommand({
      Entries: messages.map(msg => ({
        ...msg,
        EventBusName: BUS_NAME
      }))
    });

    const response = await eventBridgeclient.send(command);
    const failedEntryCount = response?.FailedEntryCount ?? response?.FailedEntityCount ?? 0;

    console.debug("[ACTION_ENQUEUER]", "Sent message response", response);
    if (failedEntryCount > 0) {
      const failedActions = (originalActions ?? []).filter((_, index) => {
        const entry = response?.Entries?.[index];
        return entry && entry.ErrorCode != null;
      });

      console.warn(
        "[ACTION_ENQUEUER]",
        `EventBridge PutEvents returned ${failedEntryCount} failed entries; rescheduling only failed messages`
      );

      return [...failedActions];
    }
  } catch (exc) {

    if (TIMEOUT_EXCEPTIONS.includes(exc.name)) {
      console.warn(
        "[ACTION_ENQUEUER]",
        "Timeout detected for:",
        JSON.stringify(messages)
      );
      throw new TimeoutException(exc);
    } else {
      console.error(
        "[ACTION_ENQUEUER]",
        "Error sending messages",
        JSON.stringify(messages),
        exc
      );
      
      throw new EventBridgeServiceException(exc);
    }
  }
  return [];
}

async function sendTimeoutMessages(messages) {
  let input;
  try {
    if (!messages || messages.length === 0)
      throw new Error("No messages to send");

    console.debug(
      "[ACTION_ENQUEUER]",
      `Proceeding to send ${messages.length} messages to timeout queue ${config.get("TIMEOUT_DLQ")}`
    );

    input = {
      QueueUrl: config.get("TIMEOUT_DLQ"),
      Entries: messages.map((msg) => ({
        Id: v4(),
        MessageBody: JSON.stringify(msg),
      })),
    };

    console.log(
      "[ACTION_ENQUEUER]",
      "Sending the following batch of messages:",
      input
    );

    const command = new SendMessageBatchCommand(input);

    const response = await sqs.send(command);
    console.debug("[ACTION_ENQUEUER]", "Sent message response", response);
    if (response.Failed && response.Failed.length > 0) {
      console.warn("[ACTION_ENQUEUER]", "Failed Messages", response.Failed);
      return response.Failed;
    }
  } catch (exc) {
    console.error(
        "[ACTION_ENQUEUER]",
        "Error sending messages",
        JSON.stringify(messages),
        input,
        exc
      );
      throw new SQSServiceException(exc);
  }
  return [];
}

module.exports = { putMessages };
