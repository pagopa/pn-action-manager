const { expect } = require("chai");
const { describe, it } = require("mocha");

const {
  generateKoResponse
} = require("../app/responses.js");

  it("generateKoResponse ", () => {
    const result = generateKoResponse(new Error("TEST ERROR"));

    expect(result).to.not.be.null;
    expect(result.statusCode).to.be.eq(500);
  });
