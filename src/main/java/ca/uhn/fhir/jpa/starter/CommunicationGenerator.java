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

import javax.net.ssl.HttpsURLConnection;

public class CommunicationGenerator {
    private JSONObject communicationRequest;
    private RequestHandler requestHandler;
    private String serverAddress;

    public CommunicationGenerator(JSONObject cr, String address) {
        this.communicationRequest = cr;
        serverAddress = address;
        requestHandler = new RequestHandler(serverAddress);
    }
    public String makeCommunication(){
        System.out.println("Making communication");
        JSONObject communication = null;
        JSONObject subject = (JSONObject) communicationRequest.get("subject");
        String reference = subject.get("reference").toString();
        try {
          // Use this patient to search for the payload
          String patientID = reference.substring(reference.lastIndexOf("/") + 1);
          String patient = requestHandler.sendGet("Patient", patientID + "?_format=json");
          communication = getCommunicationSkeleton();

          subject = (JSONObject) communication.get("subject");
          subject.put("reference", reference);
          JSONArray recipient = (JSONArray) communication.get("recipient");
          ((JSONObject) recipient.get(0)).put("reference", ((JSONObject)((JSONArray) communicationRequest.get("recipient")).get(0)).get("reference").toString());
          JSONObject sender = (JSONObject) communication.get("sender");
          sender.put("reference", ((JSONObject) communicationRequest.get("sender")).get("reference").toString());

          String payload = getBundle(patientID);
          String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
          JSONArray pl = (JSONArray) communication.get("payload");
          JSONObject content = (JSONObject) ((JSONObject) pl.get(0)).get("contentAttachment");
          content.put("data", encoded);

        } catch (Exception e) {
          System.out.println("Exception: " + e);
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
            System.out.println("Error creating communication skeleton");
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
            payload = requestHandler.sendGet("Bundle", "?type=document&_format=json");
            JSONParser parser = new JSONParser();
            JSONObject bundles = (JSONObject) parser.parse(payload);
            JSONArray entries = (JSONArray) bundles.get("entry");

            for (int i = 0; i < entries.size(); i++) {
                JSONObject bundle = ((JSONObject)((JSONObject) (entries.get(i))).get("resource"));
                JSONArray internalEntry = (JSONArray) bundle.get("entry");
                for (int j = 0; j < internalEntry.size(); j++) {
                    JSONObject resource = ((JSONObject)((JSONObject) internalEntry.get(j)).get("resource"));
                    if (resource.get("resourceType").equals("Patient")) {
                        if (resource.get("id").equals(patientID)) {
                            bundleID = (String) bundle.get("id");
                            System.out.println("Found the bundle for the patient");
                            System.out.println("Corresponding Bundle ID:" + bundleID);
                        }
                    }
                }

            }
        } catch(Exception e) {
            System.out.println("Exeption retrieving bundles");
        }

        if (!bundleID.equals("")) {
            try {
                payload = requestHandler.sendGet("Bundle", bundleID + "?_format=xml");
            } catch(Exception e) {
                System.out.println("Exeption getting bundle: " + e);
            }
        }
        return payload;

    }

}
