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
public class ActionEntity {
  public static final String FIELD_ACTION_ID = "actionId";

  private String actionId;
  private String iun;
  private Instant notBefore;
  private ActionType type;
  private Integer recipientIndex;
  private String timeslot;
  private String timelineId;
  private long ttl;
  private Map<String,Object> details;

  @DynamoDbPartitionKey
  @DynamoDbAttribute(value = FIELD_ACTION_ID)
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
