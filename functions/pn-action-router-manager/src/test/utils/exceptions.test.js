const {
  LambdaDisabledException
} = require("../app/exceptions/exceptions.js");

const { expect } = require("chai");
const { describe, it } = require("mocha");

describe("test LambdaDisabledException", () => {
  it("should set name", () => {
    const exception = new LambdaDisabledException();
    expect(exception.name).to.eq("LambdaDisabledException");
  });
});
