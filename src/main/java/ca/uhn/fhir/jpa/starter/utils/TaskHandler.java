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

public class TaskHandler {
    private RequestHandler requestHandler;
    private String serverAddress;
    public TaskHandler(String address) {
        serverAddress = address;
        requestHandler = new RequestHandler(serverAddress);
    }
    public String getBundle(String patientID) {
        // NOTE: This does not show how to generate the bundle, but rather focuses on the
        // Communication and handling of the PCDE document. It does retrieve the bundle based on the
        // referenced patient
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
        return bundleID;

    }
    /*
      Search for the bundle. If it exists then update the task. Otherwise
      set the task status to fail
    */
    public String handleTask(JSONWrapper json, String type) {
          String bundleID = getBundle(json.get("for").get("identifier").get("value").getValue().toString());
          JSONParser parser = new JSONParser();
          if (!bundleID.equals("")) {
              // Update the task with success
              json.put("status", "completed");
              String output = "[{\"type\": {\"coding\": [{\"system\": \"http://hl7.org/fhir/us/davinci-pcde/CodeSystem/PCDEtempCodes\",  \"code\": \"document\"}]},\"valueReference\": {\"reference\": \"Bundle/" + bundleID + "\"}}]";
              try {
                  json.put("output", parser.parse(output));
              } catch (Exception e) {
                  System.out.println("Error adding output");
              }
          } else {
              // Update the task with fail
              json.put("status", "failed");
          }
          try {
              String response = "";
              if (type.equals("PUT")) {
                  response = requestHandler.sendPut(serverAddress + "/Task/" + json.get("id"), json.toString());
              } else {
                  response = requestHandler.sendPost(serverAddress + "/Task", json.toString());
              }
              JSONWrapper taskResponse = new JSONWrapper(parser.parse(response));
              return taskResponse.get("id").getValue().toString();
          } catch (Exception e) {
              System.out.println("Error parsing task response");
          }
          return "";
    }
}
