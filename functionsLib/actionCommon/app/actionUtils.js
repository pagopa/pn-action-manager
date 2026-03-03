function getCompleteActionType(actionType, details) {
    if(!details){
        return actionType;
    }
    if (actionType === 'DOCUMENT_CREATION_RESPONSE' && details.documentCreationType === 'SENDER_ACK') {
        return actionType + '_VALIDATION';
    }
    return actionType;
}

module.exports = {
    getCompleteActionType
}