package ca.uhn.fhir.jpa.starter;
import org.hl7.fhir.r4.model.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import ca.uhn.fhir.jpa.starter.RequestHandler;
import ca.uhn.fhir.jpa.starter.JSONWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import javax.net.ssl.HttpsURLConnection;

public class CommunicationGenerator {
    private JSONWrapper communicationRequest;
    private RequestHandler requestHandler;
    private String serverAddress;

    public CommunicationGenerator(JSONObject cr, String address) {
        this.communicationRequest = new JSONWrapper(cr);
        serverAddress = address;
        requestHandler = new RequestHandler(serverAddress);
    }
    public String makeCommunication(HttpServletResponse theResponse) {
        theResponse.setStatus(404); // Assume the path fails
        String patientID = "";
        JSONWrapper cr = communicationRequest;
        JSONWrapper communication = null;
        if (communicationRequest.get("resourceType").getValue().equals("CommunicationRequest")) {
            // cr = communicationRequest;
            String reference = cr.get("subject").get("reference").getValue().toString();
            patientID = reference.substring(reference.lastIndexOf("/") + 1);
        } else if (communicationRequest.get("resourceType").getValue().equals("Bundle")) {
            JSONWrapper patientInfo = null;
            for (int i = 0; i < communicationRequest.get("entry").size(); i++) {
                if (communicationRequest.get("entry").get(i).get("resource").get("resourceType").getValue().equals("Patient")) {
                    patientInfo = communicationRequest.get("entry").get(i).get("resource");
                } else if (communicationRequest.get("entry").get(i).get("resource").get("resourceType").getValue().equals("CommunicationRequest")) {
                    cr = communicationRequest.get("entry").get(i).get("resource");
                }
            }
            // Need to somehow confirm that this is the actual patient. If more get returned need to
            // update communication accordingly
            JSONWrapper name = patientInfo.get("name").get(0);
            String given = "";
            String family = "";
            String bdate = "";
            try {
                given = name.get("given").get(0).getValue().toString();
            } catch (Exception e) {
                System.out.println("No given name");
            }
            try {
                family = name.get("family").getValue().toString();
            } catch (Exception e) {
                System.out.println("No family name");
            }
            try {
                bdate = patientInfo.get("birthDate").getValue().toString();
            } catch(Exception e) {
                System.out.println("No birthdate");
            }
            try {
              String patientResponse = requestHandler.sendGet("Patient", "?_format=json&given="+given+"&family="+family+"&birthdate="+bdate);
              JSONParser parser = new JSONParser();
              JSONWrapper responseBundle = new JSONWrapper(parser.parse(patientResponse));
              System.out.println("TOTAL: " + responseBundle.get("total").getValue());
              if ((long)responseBundle.get("total").getValue() == 1) {
                  patientID = responseBundle.get("entry").get(0).get("resource").get("id").getValue().toString();
              } else if ((long)responseBundle.get("total").getValue() > 1) {
                  // Found multiple patients. More info required
                  theResponse.setStatus(413);
                  String payload = "Found multiple patients matching the provided demographics";
                  String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
                  communication = new JSONWrapper(getCommunicationErrorStatus());
                  communication.get("payload").get(0).get("contentAttachment").put("data", encoded);
                  return communication.toString();
              } else {
                  theResponse.setStatus(404);
                  String payload = "Unable to locate patient";
                  String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
                  communication = new JSONWrapper(getCommunicationErrorStatus());
                  communication.get("payload").get(0).get("contentAttachment").put("data", encoded);
                  return communication.toString();
              }
            } catch (Exception e) {
                System.out.println("Error making communication " + e);
            }

        } else {
            // Invalid resource type
            theResponse.setStatus(404);
            String payload = "Invalid resource type";
            String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
            communication = new JSONWrapper(getCommunicationErrorStatus());
            communication.get("payload").get(0).get("contentAttachment").put("data", encoded);
            return communication.toString();
        }

        try {
          // Use this patient to search for the payload
          String reference = cr.get("subject").get("reference").getValue().toString();
          String patient = requestHandler.sendGet("Patient", patientID + "?_format=json");
          communication = new JSONWrapper(getCommunicationSkeleton());

          JSONWrapper subject = new JSONWrapper(communication.get("subject"));
          subject.put("reference", reference);
          JSONWrapper recipient = communication.get("recipient");
          recipient.get(0).put("reference", cr.get("recipient").get(0).get("reference").getValue());
          communication.get("sender").put("reference", cr.get("sender").get("reference").getValue());
          String payload = getBundle(patientID);
          String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
          communication.get("payload").get(0).get("contentAttachment").put("data", encoded);
          theResponse.setStatus(200); // Successfully created everything
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Exception finding patient: " + e);
        }
        return communication.toString();
    }
    public JSONObject getCommunicationSkeleton() {
        String base ="{\"resourceType\" : \"Communication\",\"text\" : {\"status\" : \"generated\",\"div\" : \"\"},\"basedOn\" : [{\"reference\" : \"Bundle/pcde-communicationrequest-example\"}],\"status\" : \"completed\",\"subject\" : {  \"reference\" : \"Patient/1\"},\"recipient\" : [  {    \"reference\" : \"Organization/1\"  }],\"sender\" : {  \"reference\" : \"Organization/2\"},\"payload\" : [  {    \"extension\" : [      {        \"url\" : \"http://hl7.org/fhir/us/davinci-cdex/StructureDefinition/cdex-payload-clinical-note-type\",        \"valueCodeableConcept\" : {\"coding\" : [  {    \"system\" : \"http://hl7.org/fhir/us/davinci-pcde/CodeSystem/PCDEDocumentCode\",    \"code\" : \"pcde\"  }]        }      }    ],    \"contentAttachment\" : {      \"contentType\" : \"application/fhir+xml\",      \"data\" : \"\"    }  }]}";
        JSONParser parser = new JSONParser();
        try {
          JSONObject com = (JSONObject) parser.parse(base);
          return com;
        } catch (Exception e) {
            System.out.println("Error creating communication skeleton " + e.getMessage());
        }
        return null;

    }
    public JSONObject getCommunicationErrorStatus() {
        String base ="{\"resourceType\" : \"Communication\",\"text\" : {\"status\" : \"generated\",\"div\" : \"\"},\"basedOn\" : [{\"reference\" : \"Bundle/pcde-communicationrequest-example\"}],\"status\" : \"completed\",\"recipient\" : [  {    \"reference\" : \"Organization/1\"  }],\"sender\" : {  \"reference\" : \"Organization/2\"},\"payload\" : [  {    \"extension\" : [      {        \"url\" : \"http://hl7.org/fhir/us/davinci-cdex/StructureDefinition/cdex-payload-clinical-note-type\",        \"valueCodeableConcept\" : {\"coding\" : [  {    \"system\" : \"http://hl7.org/fhir/us/davinci-pcde/CodeSystem/PCDEDocumentCode\",    \"code\" : \"pcde\"  }]        }      }    ],    \"contentAttachment\" : {      \"contentType\" : \"application/fhir+xml\",      \"data\" : \"\"    }  }]}";
        JSONParser parser = new JSONParser();
        try {
          JSONObject com = (JSONObject) parser.parse(base);
          return com;
        } catch (Exception e) {
            System.out.println("Error creating communication skeleton " + e.getMessage());
        }
        return null;
    }
    public String getBundle(String patientID) {
        // NOTE: This does not show how to generate the bundle, but rather focuses on the
        // Communication and handling of the PCDE document. It does retrieve the bundle based on the
        // referenced patient
        String payload = "";
        String bundleID = "";
        try {
            String payloadBundles = requestHandler.sendGet("Bundle", "?type=document&_format=json");
            JSONParser parser = new JSONParser();
            JSONObject bundles = (JSONObject) parser.parse(payloadBundles);
            JSONArray entries = (JSONArray) bundles.get("entry");

            for (int i = 0; i < entries.size(); i++) {
                JSONObject bundle = ((JSONObject)((JSONObject) (entries.get(i))).get("resource"));
                JSONArray internalEntry = (JSONArray) bundle.get("entry");
                for (int j = 0; j < internalEntry.size(); j++) {
                    JSONObject resource = ((JSONObject)((JSONObject) internalEntry.get(j)).get("resource"));
                    if (resource.get("resourceType").equals("Patient")) {
                        if (resource.get("id").equals(patientID)) {
                            bundleID = (String) bundle.get("id");
                        }
                    }
                }

            }
        } catch(Exception e) {
            System.out.println("Exeption retrieving bundles " + e.getMessage());
        }

        if (!bundleID.equals("")) {
            try {
                payload = requestHandler.sendGet("Bundle", bundleID + "?_format=xml");
            } catch(Exception e) {
                System.out.println("Exeption getting bundle: " + e.getMessage());
            }
        }
        return payload;

    }

}
