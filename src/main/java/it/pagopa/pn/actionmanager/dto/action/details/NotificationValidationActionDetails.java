package it.pagopa.pn.actionmanager.dto.action.details;



import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationValidationActionDetails implements ActionDetails {
  private int retryAttempt;
  private Instant startWorkflowTime;
}
