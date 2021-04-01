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

public class PatientFinder {
    private RequestHandler requestHandler;
    private String serverAddress;
    public PatientFinder(String address) {
        serverAddress = address;
        requestHandler = new RequestHandler();
    }
    public String findPatient(HttpServletResponse theResponse, JSONWrapper json) {
        return findPatientAsJSON(theResponse, json).toString();
    }
    public JSONWrapper findPatientAsJSON(HttpServletResponse theResponse, JSONWrapper json) {
        JSONWrapper patientInfo = json.get("parameter").get(1).get("resource");
        String patientID = "";
        JSONWrapper endpoint = null;
        JSONWrapper name = patientInfo.get("name").get(0);
        String given = "";
        String family = "";
        String bdate = "";
        ArrayList<String> identifiers = new ArrayList<String>();
        JSONParser parser = new JSONParser();
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
            for (int i = 0; i < patientInfo.get("identifier").size(); i++) {
                String identifier = patientInfo.get("identifier").get(i).get("value").getValue().toString();
                identifiers.add(identifier);
            }
        } catch(Exception e) {
            System.out.println("No identifiers");
        }
        try {
          String request = "?_format=json";
          if (identifiers.size() > 0) {
              for (String identifier: identifiers) {
                  request += "&identifier="+identifier;
              }
          }
          if (!given.equals("")) {
              request += "&given="+given;
          }
          if (!family.equals("")) {
              request += "&family="+family;
          }
          if (!bdate.equals("")) {
              request += "&birthdate="+bdate;
          }
          String patientResponse = requestHandler.sendGet(serverAddress+"/Patient", request);

          JSONWrapper responseBundle = new JSONWrapper(parser.parse(patientResponse));
          if ((long)responseBundle.get("total").getValue() == 1) {
              return responseBundle.get("entry").get(0);
          } else if ((long)responseBundle.get("total").getValue() > 1) {
              // Found multiple patients. More info required
              theResponse.setStatus(413);
              return new JSONWrapper("413");
          } else {
              theResponse.setStatus(404);
              return new JSONWrapper("404");
          }
        } catch (Exception e) {
            System.out.println("Error making communication " + e);
            theResponse.setStatus(404);
            return new JSONWrapper("404");
        }
    }
}
