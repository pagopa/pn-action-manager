const { expect } = require("chai");
const proxyquire = require("proxyquire").noPreserveCache();

describe("writeToEventBridge.writeMessagesToEventBridge", function () {
	let isTimeToLeaveImpl;
	let eventBridgeSendImpl;
	let sqsSendImpl;
	let capturedEventBridgeCommands;
	let capturedSqsCommands;

	const configValues = {
		MAX_EVENT_BRIDGE_BATCH: 2,
		"timeout.DEFAULT_SOCKET_TIMEOUT": 1000,
		"timeout.DEFAULT_REQUEST_TIMEOUT": 1000,
		"timeout.DEFAULT_CONNECTION_TIMEOUT": 1000,
		TIMEOUT_EXCEPTIONS: ["TimeoutError", "ECONNRESET"],
		BUS_NAME: "test-bus",
		ACTION_TIMEOUT_ERROR_DLQ_URL: "https://sqs.test/timeout-dlq",
	};

	function loadModuleUnderTest() {
		capturedEventBridgeCommands = [];
		capturedSqsCommands = [];

		class PutEventsCommand {
			constructor(input) {
				this.input = input;
			}
		}

		class SendMessageBatchCommand {
			constructor(input) {
				this.input = input;
			}
		}

		class EventBridgeClient {
			// eslint-disable-next-line no-useless-constructor
			constructor(_) {}
			async send(command) {
				capturedEventBridgeCommands.push(command);
				return eventBridgeSendImpl(command);
			}
		}

		class SQSClient {
			// eslint-disable-next-line no-useless-constructor
			constructor(_) {}
			async send(command) {
				capturedSqsCommands.push(command);
				return sqsSendImpl(command);
			}
		}

		const moduleUnderTest = proxyquire.noCallThru().load(
			"../../app/eventBridge/writeToEventBridge.js",
			{
				"@aws-sdk/client-eventbridge": {
					EventBridgeClient,
					PutEventsCommand,
				},
				"@aws-sdk/client-sqs": {
					SQSClient,
					SendMessageBatchCommand,
				},
				"@aws-sdk/node-http-handler": {
					NodeHttpHandler: class NodeHttpHandler {
						// eslint-disable-next-line no-useless-constructor
						constructor(_) {}
					},
				},
				uuid: {
					v4: () => "uuid-fixed",
				},
				config: {
					get: (key) => configValues[key],
				},
				"../utils/utils.js": {
					isTimeToLeave: (context) => isTimeToLeaveImpl(context),
				},
				"pn-action-common": {
					ActionUtils: {
						getCompleteActionType: (type) => type || "UNKNOWN",
					},
				},
			}
		);

		return moduleUnderTest;
	}

	beforeEach(() => {
		isTimeToLeaveImpl = () => false;
		eventBridgeSendImpl = async () => ({ Failed: [] });
		sqsSendImpl = async () => ({ Failed: [] });
	});

	it("returns [] and never calls EventBridge when input is empty", async () => {
		const { writeMessagesToEventBridge } = loadModuleUnderTest();
		const res = await writeMessagesToEventBridge([], {});

		expect(res).to.deep.equal([]);
		expect(capturedEventBridgeCommands).to.have.length(0);
		expect(capturedSqsCommands).to.have.length(0);
	});

	it("returns all actions unchanged when isTimeToLeave is true immediately", async () => {
		isTimeToLeaveImpl = () => true;
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [{ iun: "iun-1", type: "T1", details: {} }];
		const res = await writeMessagesToEventBridge(actions, {});

		expect(res).to.deep.equal(actions);
		expect(capturedEventBridgeCommands).to.have.length(0);
	});

	it("sends all actions in multiple batches and returns [] on success", async () => {
		configValues.MAX_EVENT_BRIDGE_BATCH = 2;
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [
			{ iun: "iun-1", type: "T1", details: {} },
			{ iun: "iun-2", type: "T2", details: {} },
			{ iun: "iun-3", type: "T3", details: {} },
		];

		const res = await writeMessagesToEventBridge(actions, {});

		expect(res).to.deep.equal([]);
		expect(capturedEventBridgeCommands).to.have.length(2);

		const first = capturedEventBridgeCommands[0].input;
		const second = capturedEventBridgeCommands[1].input;

		expect(first).to.have.property("Entries");
		expect(first.Entries).to.have.length(2);
		expect(first.Entries[0].EventBusName).to.equal("test-bus");
		expect(second.Entries).to.have.length(1);
		expect(second.Entries[0].EventBusName).to.equal("test-bus");
	});

	it("stops sending when time runs out and returns remaining actions", async () => {
		configValues.MAX_EVENT_BRIDGE_BATCH = 2;
		let calls = 0;
		isTimeToLeaveImpl = () => {
			calls += 1;
			return calls >= 2; // allow only first loop iteration
		};
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [
			{ iun: "iun-1", type: "T1", details: {} },
			{ iun: "iun-2", type: "T2", details: {} },
			{ iun: "iun-3", type: "T3", details: {} },
		];

		const res = await writeMessagesToEventBridge(actions, {});

		expect(capturedEventBridgeCommands).to.have.length(1);
		expect(res).to.deep.equal([{ iun: "iun-3", type: "T3", details: {} }]);
	});

	it("when response contains Failed entries, returns the spliced batch (current implementation)", async () => {
		configValues.MAX_EVENT_BRIDGE_BATCH = 5;
		eventBridgeSendImpl = async () => ({ Failed: [{ Id: undefined }] });
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [
			{ iun: "iun-1", type: "T1", details: {} },
			{ iun: "iun-2", type: "T2", details: {} },
		];

		const res = await writeMessagesToEventBridge(actions, {});

		expect(capturedEventBridgeCommands).to.have.length(1);
		expect(res).to.have.length(2);
		expect(res.map((a) => a.iun)).to.have.members(["iun-1", "iun-2"]);
	});

	it("on timeout exception, routes mapped messages to DLQ via SQS and returns []", async () => {
		const timeoutErr = new Error("timeout");
		timeoutErr.name = "TimeoutError";

		eventBridgeSendImpl = async () => {
			throw timeoutErr;
		};
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [
			{ iun: "iun-1", type: "T1", details: {} },
			{ iun: "iun-2", type: "T2", details: {} },
		];

		const res = await writeMessagesToEventBridge(actions, {});

		expect(res).to.deep.equal([]);
		expect(capturedEventBridgeCommands).to.have.length(1);
		expect(capturedSqsCommands).to.have.length(1);

		const sqsInput = capturedSqsCommands[0].input;
		expect(sqsInput.QueueUrl).to.equal(configValues.ACTION_TIMEOUT_ERROR_DLQ_URL);
		expect(sqsInput.Entries).to.have.length(2);
	});

	it("on generic exception, returns the whole batch to reschedule and does not write to DLQ", async () => {
		const genericErr = new Error("boom");
		genericErr.name = "SomethingElse";

		eventBridgeSendImpl = async () => {
			throw genericErr;
		};
		const { writeMessagesToEventBridge } = loadModuleUnderTest();

		const actions = [
			{ iun: "iun-1", type: "T1", details: {} },
			{ iun: "iun-2", type: "T2", details: {} },
		];

		const res = await writeMessagesToEventBridge(actions, {});

		expect(capturedEventBridgeCommands).to.have.length(1);
		expect(capturedSqsCommands).to.have.length(0);
		expect(res).to.have.length(2);
		expect(res.map((a) => a.iun)).to.have.members(["iun-1", "iun-2"]);
	});
});

