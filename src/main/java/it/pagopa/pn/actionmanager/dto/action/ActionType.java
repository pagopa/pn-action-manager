package it.pagopa.pn.actionmanager.dto.action;

import it.pagopa.pn.actionmanager.dto.action.details.*;
import lombok.Getter;

@Getter
public enum ActionType {

    NOTIFICATION_VALIDATION(NotificationValidationActionDetails.class),

    NOTIFICATION_REFUSED(NotificationRefusedActionDetails.class),

    NOTIFICATION_CANCELLATION(NotHandledDetails.class),

    SCHEDULE_RECEIVED_LEGALFACT_GENERATION(NotHandledDetails.class),

    CHECK_ATTACHMENT_RETENTION(NotHandledDetails.class),

    START_RECIPIENT_WORKFLOW(RecipientsWorkflowDetails.class),

    CHOOSE_DELIVERY_MODE(NotHandledDetails.class),

    ANALOG_WORKFLOW(NotHandledDetails.class),

    DIGITAL_WORKFLOW_NEXT_ACTION(NotHandledDetails.class),

    DIGITAL_WORKFLOW_NEXT_EXECUTE_ACTION(NotHandledDetails.class),

    DIGITAL_WORKFLOW_NO_RESPONSE_TIMEOUT_ACTION(NotHandledDetails.class),

    DIGITAL_WORKFLOW_RETRY_ACTION(NotHandledDetails.class),

    SEND_DIGITAL_FINAL_STATUS_RESPONSE(SendDigitalFinalStatusResponseDetails.class),

    REFINEMENT_NOTIFICATION(NotHandledDetails.class),

    SENDER_ACK(NotHandledDetails.class),

    DOCUMENT_CREATION_RESPONSE(DocumentCreationResponseActionDetails.class),

    POST_ACCEPTED_PROCESSING_COMPLETED(NotHandledDetails.class),

    SEND_ANALOG_FINAL_STATUS_RESPONSE(NotHandledDetails.class);

    private final Class<? extends ActionDetails> detailsJavaClass;

    ActionType(Class<? extends ActionDetails> detailsJavaClass) {
        this.detailsJavaClass = detailsJavaClass;
    }

    public String buildActionId(Action action) {
        throw new UnsupportedOperationException("Must be implemented for each action type");
    }

}
