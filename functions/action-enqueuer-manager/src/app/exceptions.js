class SQSServiceException extends Error {
  constructor(e) {
    super(`${e.message}`);
    this.name = "SQSServiceException";
  }
}

class TimeoutException extends Error {
  constructor(e) {
    super(`${e.message}`);
    this.name = "TimeoutException";
  }
}

class LambdaDisabledException extends Error {
  constructor() {
    super(`${e.message} - date range exceeded, so lambda is disabled`);
    this.name = "LambdaDisabledException";
  }
}

module.exports = {
  SQSServiceException,
  TimeoutException,
  LambdaDisabledException
};
