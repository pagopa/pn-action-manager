package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity;

import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.ActionDetailsConverter;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@DynamoDbBean
@Getter
@Setter
public class FutureActionEntity {
    public static final String FIELD_TIME_SLOT = "timeSlot";
    public static final String FIELD_ACTION_ID = "actionId";
    public static final String FIELD_IUN = "iun";
    public static final String FIELD_NOT_BEFORE = "notBefore";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_RECIPIENT_INDEX = "recipientIndex";
    public static final String FIELD_TIMESLOT = "timeslot";
    public static final String FIELD_TIMELINE_ID = "timelineId";
    public static final String FIELD_DETAILS = "details";
    public static final String FIELD_LOGICAL_DELETED = "logicalDeleted";

    private String timeSlot;
    private String actionId;
    private String iun;
    private Instant notBefore;
    private Boolean logicalDeleted;
    private ActionType type;
    private Integer recipientIndex;
    private String timelineId;
    private Map<String,Object> details;

    @DynamoDbPartitionKey
    @DynamoDbAttribute(value = FIELD_TIME_SLOT )
    public String getTimeSlot() {
        return timeSlot;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute(value = FIELD_ACTION_ID )
    public String getActionId() {
        return actionId;
    }

    @DynamoDbAttribute(value = "details")
    @DynamoDbConvertedBy(ActionDetailsConverter.class)
    @DynamoDbIgnoreNulls
    public Map<String,Object> getDetails() {
        return details;
    }
}
