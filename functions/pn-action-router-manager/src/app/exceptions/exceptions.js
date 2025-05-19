class LambdaDisabledException extends Error {
  constructor() {
    super(`${e.message} - date range exceeded, so lambda is disabled`);
    this.name = "LambdaDisabledException";
  }
}

module.exports = {
  LambdaDisabledException
};