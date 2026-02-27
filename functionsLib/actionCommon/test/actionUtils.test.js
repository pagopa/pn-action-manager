const { expect } = require('chai');
const { getCompleteActionType } = require('../app/actionUtils');

describe('actionUtils', function () {
    describe('getCompleteActionType', function () {
        it('returns original actionType when details is null/undefined', function () {
            expect(getCompleteActionType('ANY_ACTION', undefined)).to.equal('ANY_ACTION');
            expect(getCompleteActionType('ANY_ACTION', null)).to.equal('ANY_ACTION');
        });

        it('returns original actionType when actionType is not DOCUMENT_CREATION_RESPONSE', function () {
            const details = { documentCreationType: 'SENDER_ACK' };
            expect(getCompleteActionType('NOTIFICATION_VALIDATION', details)).to.equal('NOTIFICATION_VALIDATION');
        });

        it('appends _VALIDATION for DOCUMENT_CREATION_RESPONSE + SENDER_ACK', function () {
            const details = { documentCreationType: 'SENDER_ACK' };
            expect(getCompleteActionType('DOCUMENT_CREATION_RESPONSE', details)).to.equal(
                'DOCUMENT_CREATION_RESPONSE_VALIDATION'
            );
        });

        it('does not append _VALIDATION for DOCUMENT_CREATION_RESPONSE with non-SENDER_ACK details', function () {
            expect(getCompleteActionType('DOCUMENT_CREATION_RESPONSE', { documentCreationType: 'NO_SENDER_ACK' })).to.equal(
                'DOCUMENT_CREATION_RESPONSE'
            );
            expect(getCompleteActionType('DOCUMENT_CREATION_RESPONSE', { documentCreationType: undefined })).to.equal(
                'DOCUMENT_CREATION_RESPONSE'
            );
            expect(getCompleteActionType('DOCUMENT_CREATION_RESPONSE', {})).to.equal('DOCUMENT_CREATION_RESPONSE');
        });
    });
});