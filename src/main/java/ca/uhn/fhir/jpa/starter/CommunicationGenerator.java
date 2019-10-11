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
    private final String baseUrl = "http://localhost:8080/fhir/";//"https://davinci-pcde-ri.logicahealth.org/fhir/";

    public CommunicationGenerator(JSONObject cr) {
        this.communicationRequest = cr;
        requestHandler = new RequestHandler(baseUrl);
    }
    public String makeCommunication(){
        System.out.println("Making communication");
        JSONObject communication = null;
        System.out.println(communicationRequest);
        JSONObject subject = (JSONObject) communicationRequest.get("subject");
        System.out.println(subject);
        String reference = subject.get("reference").toString();
        System.out.println(reference);
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

          // NOTE: Bundle ID is currently hard coded since the bundle will normally be generated on the fly
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
        // Communication and handling of the PCDE document
        String payload = "";
        if (patientID.equals("14")) {
            try {
                payload = requestHandler.sendGet("Bundle", "1?_format=xml");
            } catch(Exception e) {
                System.out.println("Exeption getting bundle: " + e);
            }
        }
        return payload;

    }

}
