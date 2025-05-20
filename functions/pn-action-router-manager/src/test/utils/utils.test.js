//2024-07-05T13:15:11.508Z
const sinon = require("sinon");
const { expect } = require("chai");
const { addDaysToDate, isLambdaDisabled } = require("../../app/utils/utils.js");

describe("addDateTest", function () {
  it("test Ok", async () => {
    let startDate = "2024-07-25T13:15:11.508011744Z";
    

    let endDate = addDaysToDate(startDate, 10);

    console.log("EndDate is ",endDate);

    expect(endDate).deep.equals(1722777311);
  });
});

describe("isLambdaDisabled", function() {
  let clock;

  afterEach(() => {
    if (clock) clock.restore();
  });

  it("should return true if currentDate is before start", () => {
    // Set current date to 2023-01-01T00:00:00.000Z
    clock = sinon.useFakeTimers(new Date("2023-01-01T00:00:00.000Z").getTime());
    const featureFlag = {
      start: "2023-02-01T00:00:00.000Z",
      end: "2023-03-01T00:00:00.000Z"
    };
    expect(isLambdaDisabled(featureFlag)).to.be.true;
  });

  it("should return true if currentDate is after end", () => {
    // Set current date to 2023-04-01T00:00:00.000Z
    clock = sinon.useFakeTimers(new Date("2023-04-01T00:00:00.000Z").getTime());
    const featureFlag = {
      start: "2023-02-01T00:00:00.000Z",
      end: "2023-03-01T00:00:00.000Z"
    };
    expect(isLambdaDisabled(featureFlag)).to.be.true;
  });

  it("should return false if currentDate is between start and end", () => {
    // Set current date to 2023-02-15T12:00:00.000Z
    clock = sinon.useFakeTimers(new Date("2023-02-15T12:00:00.000Z").getTime());
    const featureFlag = {
      start: "2023-02-01T00:00:00.000Z",
      end: "2023-03-01T00:00:00.000Z"
    };
    expect(isLambdaDisabled(featureFlag)).to.be.false;
  });
});