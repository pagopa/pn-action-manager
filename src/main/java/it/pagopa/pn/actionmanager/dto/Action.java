package it.pagopa.pn.actionmanager.dto;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class Action {

    private String iun;

    private String actionId;

    private Instant notBefore;

    private ActionType type;

    private Boolean logicalDeleted;

    // Required and used for SEND_PEC and RECEIVE_PEC ActionType
    private Integer recipientIndex;

    private String timelineId;

    private String timeslot;

    private String details;
}
