package it.pagopa.pn.actionmanager.dto.action.details;


import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import it.pagopa.pn.actionmanager.dto.address.DigitalAddressInfoSentAttempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class SendDigitalFinalStatusResponseDetails implements ActionDetails {
  private DigitalAddressInfoSentAttempt lastAttemptAddressInfo;
  private Boolean isFirstSendRetry;
  private String alreadyPresentRelatedFeedbackTimelineId;
}
