package ca.uhn.fhir.jpa.starter.utils;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import javax.net.ssl.HttpsURLConnection;

public class ParameterGenerator {
    private RequestHandler requestHandler;
    private String serverAddress;
    private PatientFinder patientFinder;
    private JSONParser parser;

    public ParameterGenerator(String address) {
        serverAddress = address;
        patientFinder = new PatientFinder(serverAddress);
        requestHandler = new RequestHandler();
        parser = new JSONParser();
    }
    public String getReturnParameters(HttpServletResponse theResponse, JSONWrapper json) {
        // Find the patient if they exist and set the status codes of the response
        JSONWrapper patient = patientFinder.findPatientAsJSON(theResponse, json);
        if (patient.getValue().equals("404") || patient.getValue().equals("413")) {
            return patient.toString();
        }
        // At this point the UMB needs to be added to the json patient passed in and the
        // New Health plan coverage needs to be put into the parameter resource
        JSONWrapper patientResource = json.get("parameter").get(1);
        patient = patient.get("resource");
        // A valid singlular patient was found add the custom identifier
        patientResource.get("resource").put("identifier", getIdentifier("UMB", patient.get("id").toString()));
        System.out.println(patientResource.getValue().toString());
        // Return the patient with the added identifier and the New Health plan coverage
        return createReturnParameter(patientResource.getValue().toString(), json.get("parameter").get(3).getValue().toString());
    }
    public String getCoverage(String patientID) {
        // Send get for coverage resource based on beneficiary id
        try {
          String response = requestHandler.sendGet(serverAddress+"/fhir/Coverage", "?beneficiary=" + patientID + "&_format=json");
          System.out.println(response);
          JSONWrapper coverageBundle = new JSONWrapper((JSONObject) parser.parse(response));
          return coverageBundle.get("entry").get(0).get("resource").getValue().toString();
        } catch (Exception e) {
            System.out.println("Error getting coverage " + e.getMessage());
        }
        return "";
    }
    public JSONWrapper getIdentifier(String type, String identifier) {
        // UMB identifier is created
        String temp = "[{\"type\": {\"coding\": [{\"system\": \"http://hl7.davinci.org\",\"code\": \""
            + type + "\"}]},\"system\": \"http://oldhealthplan.example.com\",\"value\": \""
            + identifier + "\",\"assigner\":  {\"reference\": \"Organization/2\",\"_reference\": {\"fhir_comments\": [\"UMB is assigned by the old health plan.\"]}}}]";
        try {
          JSONWrapper identifierJSON = new JSONWrapper(parser.parse(temp));
          return identifierJSON;
        } catch (Exception e) {
            System.out.println("Error making identifier " + e.getMessage());
        }
        return null;
    }
    public String createReturnParameter(String patient, String coverage) {
        // parameters for patient and coverage are added
        String parameters = "{\"resourceType\": \"Parameters\",\"parameter\": [{\"name\": \"exact\",\"valueBoolean\": true}, " + patient+ "," + coverage + "]}";
        return parameters;
    }

}
