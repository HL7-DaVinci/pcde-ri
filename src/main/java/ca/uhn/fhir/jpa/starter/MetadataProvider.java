package ca.uhn.fhir.jpa.starter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CapabilityStatement.*;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.api.server.RequestDetails;

public class MetadataProvider extends JpaConformanceProviderR4 {
  MetadataProvider(RestfulServer theRestfulServer, IFhirSystemDao<Bundle, Meta> theSystemDao, DaoConfig theDaoConfig) {
    super(theRestfulServer, theSystemDao, theDaoConfig);
    setCache(false);
  }

  @Override
  public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
    CapabilityStatement metadata = super.getServerConformance(theRequest);
    metadata.setTitle("Da Vinci PCDE Reference Implementation");
    metadata.setStatus(PublicationStatus.DRAFT);
    metadata.setExperimental(true);
    metadata.setPublisher("Da Vinci");

    Calendar calendar = Calendar.getInstance();
    calendar.set(2019, 8, 5, 0, 0, 0);
    metadata.setDate(calendar.getTime());

    CapabilityStatementSoftwareComponent software = new CapabilityStatementSoftwareComponent();
    software.setName("https://github.com/HL7-DaVinci/pcde-ri");
    metadata.setSoftware(software);

    metadata.addImplementationGuide("http://build.fhir.org/ig/HL7/davinci-pcde/index.html");
    metadata.addImplementationGuide("https://wiki.hl7.org/Da_Vinci_Payer_Coverage_Decision_FHIR_IG_Proposal");

    updateRestComponents(metadata.getRest());
    return metadata;
  }

  private void updateRestComponents(
    List<CapabilityStatementRestComponent> originalRests
  ) {
    for(CapabilityStatementRestComponent rest : originalRests) {
      List<CapabilityStatementRestResourceComponent> resources = rest.getResource();
      for(CapabilityStatementRestResourceComponent resource : resources) {
        if(resource.getType() == "CarePlan") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-pcde/StructureDefinition/profile-careplan");
        } else if (resource.getType()  == "Composition") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-pcde/StructureDefinition/profile-composition");
        } else if (resource.getType() == "Bundle") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-pcde/StructureDefinition/profile-pcde-bundle");
        } else if (resource.getType() == "Claim") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/profile-claim");
        } else if (resource.getType() == "ClaimResponse") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/profile-claimresponse");
        } else if (resource.getType() == "Patient") {
          resource.setProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
        } else if (resource.getType() == "CommunicationRequest") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-cdex/StructureDefinition/cdex-communicationrequest");
        } else if (resource.getType() == "Communication") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-cdex/StructureDefinition/cdex-communication");
        } else if (resource.getType() == "Organization") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-hrex/StructureDefinition/hrex-organization");
        } else if (resource.getType() == "PractitionerRole") {
          resource.setProfile("http://hl7.org/fhir/us/davinci-hrex/StructureDefinition/hrex-practitionerrole");
        }
      }
    }
  }
}
