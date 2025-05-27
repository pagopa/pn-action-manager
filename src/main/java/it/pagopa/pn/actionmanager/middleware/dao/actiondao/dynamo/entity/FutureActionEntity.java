package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity;

import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.ActionDetailsConverter;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnoreNulls;

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

    private String timeSlot;
    private String actionId;
    private String iun;
    private Instant notBefore;
    private Boolean logicalDeleted;
    private ActionType type;
    private Integer recipientIndex;
    private String timelineId;
    private Map<String,Object> details;

    @DynamoDbAttribute(value = "details")
    @DynamoDbConvertedBy(ActionDetailsConverter.class)
    @DynamoDbIgnoreNulls
    public Map<String,Object> getDetails() {
        return details;
    }
}
