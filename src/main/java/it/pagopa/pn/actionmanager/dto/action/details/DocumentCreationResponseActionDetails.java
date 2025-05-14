package it.pagopa.pn.actionmanager.dto.action.details;


import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import it.pagopa.pn.actionmanager.dto.documentcreation.DocumentCreationTypeInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreationResponseActionDetails implements ActionDetails {
    private String key;
    private DocumentCreationTypeInt documentCreationType;
    private String timelineId;
}
