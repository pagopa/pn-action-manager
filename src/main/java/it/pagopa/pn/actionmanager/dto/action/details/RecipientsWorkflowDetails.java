package it.pagopa.pn.actionmanager.dto.action.details;


import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipientsWorkflowDetails implements ActionDetails {

  private String quickAccessLinkToken;
}
