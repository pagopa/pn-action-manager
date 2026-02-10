const { expect } = require("chai");
const { describe, it, beforeEach } = require("mocha");
const proxyquire = require("proxyquire").noPreserveCache();
const sinon = require("sinon");

function makeTestAction(overrides = {}) {
  return {
    timeSlot: "2026-01-01T00:00:00.000Z",
    type: "NOTIFICATION_CREATION",
    seqNo: 1,
    details: {
      foo: "bar",
    },
    ...overrides,
  };
}

describe("eventBridgeFunctions test", function () {
  let eventBridgeSendStub;
  let sqsSendStub;
  let fakeConfig;
  let fakeActionUtils;

  function loadModule() {
    eventBridgeSendStub = sinon.stub();
    sqsSendStub = sinon.stub();

    class FakeEventBridgeClient {
      // eslint-disable-next-line no-unused-vars
      constructor(_opts) {}
      send(cmd) {
        return eventBridgeSendStub(cmd);
      }
    }

    class FakePutEventsCommand {
      constructor(input) {
        this.input = input;
      }
    }

    class FakeSQSClient {
      // eslint-disable-next-line no-unused-vars
      constructor(_opts) {}
      send(cmd) {
        return sqsSendStub(cmd);
      }
    }

    class FakeSendMessageBatchCommand {
      constructor(input) {
        this.input = input;
      }
    }

    class FakeNodeHttpHandler {
      constructor(options) {
        this.options = options;
      }
    }

    fakeConfig = {
      get: sinon.stub(),
    };

    fakeConfig.get.withArgs("MAX_EVENT_BRIDGE_BATCH").returns(10);
    fakeConfig.get.withArgs("timeout.DEFAULT_SOCKET_TIMEOUT").returns(1000);
    fakeConfig.get.withArgs("timeout.DEFAULT_REQUEST_TIMEOUT").returns(1000);
    fakeConfig.get.withArgs("timeout.DEFAULT_CONNECTION_TIMEOUT").returns(1000);
    fakeConfig.get.withArgs("TIMEOUT_EXCEPTIONS").returns(["TimeoutError"]);
    fakeConfig.get.withArgs("BUS_NAME").returns("test-bus");
    fakeConfig.get.withArgs("TIMEOUT_DLQ").returns(
      "https://sqs.eu-south-1.amazonaws.com/000000000000/test-dlq"
    );

    fakeActionUtils = {
      getCompleteActionType: sinon
        .stub()
        .callsFake((type /*, details */) => type),
    };

    const mod = proxyquire.noCallThru().load("../app/eventBridgeFunctions.js", {
      "@aws-sdk/client-eventbridge": {
        EventBridgeClient: FakeEventBridgeClient,
        PutEventsCommand: FakePutEventsCommand,
      },
      "@aws-sdk/client-sqs": {
        SQSClient: FakeSQSClient,
        SendMessageBatchCommand: FakeSendMessageBatchCommand,
      },
      "@aws-sdk/node-http-handler": {
        NodeHttpHandler: FakeNodeHttpHandler,
      },
      "pn-action-common": {
        ActionUtils: fakeActionUtils,
      },
      config: fakeConfig,
    });

    return mod;
  }

  beforeEach(() => {
    sinon.restore();
  });

  it("should return empty array on full success", async () => {
    const mod = loadModule();

    eventBridgeSendStub.resolves({
      FailedEntryCount: 0,
      Entries: [{ EventId: "1" }, { EventId: "2" }],
    });

    const actions = [makeTestAction({ seqNo: 11 }), makeTestAction({ seqNo: 12 })];

    const result = await mod.putMessages([...actions], () => false);

    expect(result).to.deep.equal([]);
    expect(eventBridgeSendStub.calledOnce).to.eq(true);

    const cmd = eventBridgeSendStub.firstCall.args[0];
    expect(cmd).to.have.property("input");
    expect(cmd.input).to.have.property("Entries");
    expect(cmd.input.Entries).to.have.length(2);
    expect(cmd.input.Entries[0]).to.have.property("EventBusName", "test-bus");

    expect(fakeActionUtils.getCompleteActionType.called).to.eq(true);
  });

  it("should not send anything when already timed out", async () => {
    const mod = loadModule();

    const actions = [makeTestAction()];
    const result = await mod.putMessages(actions, () => true);

    expect(result).to.equal(actions);
    expect(eventBridgeSendStub.called).to.eq(false);
    expect(sqsSendStub.called).to.eq(false);
  });

  it("should requeue only failed actions when EventBridge returns failures", async () => {
    const mod = loadModule();

    eventBridgeSendStub.resolves({
      FailedEntryCount: 1,
      Entries: [{ ErrorCode: "ERR", ErrorMessage: "boom" }, {}],
    });

    const a1 = makeTestAction({ seqNo: 1 });
    const a2 = makeTestAction({ seqNo: 2 });

    const result = await mod.putMessages([a1, a2], () => false);

    expect(result).to.have.length(1);
    expect(result[0].seqNo).to.eq(1);
  });

  it("should write to DLQ on timeout exception", async () => {
    const mod = loadModule();

    const timeoutErr = new Error("timed out");
    timeoutErr.name = "TimeoutError";

    eventBridgeSendStub.rejects(timeoutErr);
    sqsSendStub.resolves({ Failed: [] });

    const actions = [makeTestAction()];
    const result = await mod.putMessages(actions, () => false);

    expect(result).to.deep.equal([]);
    expect(eventBridgeSendStub.calledOnce).to.eq(true);
    expect(sqsSendStub.calledOnce).to.eq(true);

    const dlqCmd = sqsSendStub.firstCall.args[0];
    expect(dlqCmd).to.have.property("input");
    expect(dlqCmd.input).to.have.property("QueueUrl");
    expect(dlqCmd.input.QueueUrl).to.include("test-dlq");
    expect(dlqCmd.input).to.have.property("Entries");
    expect(dlqCmd.input.Entries).to.have.length(1);
    expect(dlqCmd.input.Entries[0]).to.have.property("MessageBody");
  });

  it("should return original chunk on non-timeout errors", async () => {
    const mod = loadModule();

    const err = new Error("kaboom");
    err.name = "OtherError";
    eventBridgeSendStub.rejects(err);

    const a1 = makeTestAction({ seqNo: 101 });
    const a2 = makeTestAction({ seqNo: 102 });

    const result = await mod.putMessages([a1, a2], () => false);

    expect(result).to.have.length(2);
    expect(result.map((a) => a.seqNo)).to.have.members([101, 102]);
    expect(sqsSendStub.called).to.eq(false);
  });
});
