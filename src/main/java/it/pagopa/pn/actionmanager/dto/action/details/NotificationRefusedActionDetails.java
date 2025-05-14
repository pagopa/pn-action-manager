package it.pagopa.pn.actionmanager.dto.action.details;


import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import it.pagopa.pn.actionmanager.dto.timeline.NotificationRefusedErrorInt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRefusedActionDetails implements ActionDetails {
    private List<NotificationRefusedErrorInt> errors;
}
