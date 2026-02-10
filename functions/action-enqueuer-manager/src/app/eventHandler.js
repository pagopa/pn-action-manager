const { putMessages } = require("./eventBridgeFunctions");
const { unmarshall } = require("@aws-sdk/util-dynamodb");
const config = require("config");

const TOLLERANCE_IN_MILLIS = config.get("RUN_TOLLERANCE_IN_MILLIS");

const isTimeToLeave = (context) =>
  context.getRemainingTimeInMillis() < TOLLERANCE_IN_MILLIS;

function decodeBase64(data) {
  const payload = Buffer.from(data, "base64").toString("ascii");
  let decodedPayload = JSON.parse(payload);
  return decodedPayload;
}

const isRecordToSend = (record) => record.eventName === "REMOVE";

function mapMessageFromKinesisToAction(record) {
  let action = record.dynamodb.OldImage;
  return unmarshall(action);
}

function isActionLogicalDeleted(action){
  return action.logicalDeleted === true;
}

const sendMessages = async (actions, timeoutFn) => {
  const notSendedResult = {
    batchItemFailures: [],
  };

  console.debug(
    "[ACTION_ENQUEUER]",
    `Sending ${actions.length} actions to event bridge ${BUS_NAME}`
  );

  const notSended = await putMessages(actions, timeoutFn);
  if (notSended.length !== 0) {
    notSended.forEach((element) =>
      notSendedResult.batchItemFailures.push({ itemIdentifier: element.seqNo })
    );
    console.warn("[ACTION_ENQUEUER]", "NOT SENDED", notSendedResult);
  }
  return notSendedResult;
};


  // ////////
  // x ogni record kinesis
  //   - verifico se è un record da inviare
  //   caso "non è un record da inviare" -> continua (prossimo record)
  //   caso "è un record da inviare"
  //     - verifico  la destinazione
  //     caso "la destinazione è cambiata o è il primo record"
  //       faccio invio dei record in lista usando la destinazione precedente
  //       caso "invio non ha avuto successo": restituisco la lista dei record non inviati ed esco
  //       caso "invio  ha avuto successo": azzero la lista
  //     in ogni caso:
  //       metto in lista di invio l'item attuale
  // caso "la lista dei record da inviare è vuota": esco
  // caso "la lista dei record da inviare non è vuota":
  //   - faccio invio dei record in lista usando l'ultima destinazione trovata
  //   - caso "invio non ha avuto successo": restituisco la lista dei record non inviati ed esco

async function handleEvent(event, context) {
  const emptyResult = {
    batchItemFailures: [],
  };

  console.log("[ACTION_ENQUEUER]", "Started");
  console.log("[ACTION_ENQUEUER]", "Event DATA", event);
  if (!event.Records) {
    console.warn("[ACTION_ENQUEUER]", "No Records to process");
    return emptyResult;
  }
  const isTimedOut = () => isTimeToLeave(context);

  let actions = [];
  let lastDestination;

  for (let i = 0; i < event.Records.length; i++) {
    let record = event.Records[i];
    let decodedRecord = decodeBase64(record.kinesis.data);
    if (isRecordToSend(decodedRecord)) {
      const action = mapMessageFromKinesisToAction(decodedRecord);

      if (isActionLogicalDeleted(action))
        continue;

      action.seqNo = record.kinesis.sequenceNumber;

      actions.push(action);
    } else {
      console.log("[ACTION_ENQUEUER]", "Discarded record", decodedRecord);
    }
  }
  if (actions.length !== 0) {
    console.debug(
      "[ACTION_ENQUEUER]",
      "Sending last actions",
      JSON.stringify(actions)
    );
    const notSended = await sendMessages(actions, isTimedOut);
    if (notSended.batchItemFailures.length != 0) {
      return notSended;
    }
  }

  console.log("[ACTION_ENQUEUER]", "No more Records to process");
  return emptyResult;
}

module.exports = { handleEvent };
