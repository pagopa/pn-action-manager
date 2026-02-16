class SQSServiceException extends Error {
  constructor(e) {
    super(`${e.message}`);
    this.name = "SQSServiceException";
  }
}

class EventBridgeServiceException extends Error {
  constructor(e) {
    super(`${e.message}`);
    this.name = "EventBridgeServiceException";
  }
}

class TimeoutException extends Error {
  constructor(e) {
    super(`${e.message}`);
    this.name = "TimeoutException";
  }
}

module.exports = {
  SQSServiceException,
  TimeoutException,
  EventBridgeServiceException
};
