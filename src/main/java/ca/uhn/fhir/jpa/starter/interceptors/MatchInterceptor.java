package ca.uhn.fhir.jpa.starter.interceptors;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import java.util.*;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ca.uhn.fhir.jpa.starter.utils.ParameterGenerator;
import ca.uhn.fhir.jpa.starter.utils.PatientFinder;
import ca.uhn.fhir.jpa.starter.utils.JSONWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Interceptor
public class MatchInterceptor {

   private String serverAddress;
   private final Logger myLogger = LoggerFactory.getLogger(MatchInterceptor.class.getName());
   /**
    * Override the incomingRequestPostProcessed method, which is called
    * for each incoming request before any processing is done
    */
   @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
   public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
      String[] parts = theRequest.getPathInfo().split("/");
      if (parts.length == 3 && parts[1].equals("Patient") && parts[2].equals("$member-match")) {
         String requestString = parseRequest(theRequest);
         try {
           JSONParser parser = new JSONParser();
           JSONWrapper json = new JSONWrapper((JSONObject) parser.parse(requestString));
           if (json.get("resourceType").getValue().equals("Parameters")) {
               if (json.get("parameter").get(1).get("resource").get("resourceType").getValue().equals("Patient")) {
                   buildReturnParameters(json, theResponse);
               }
           } else {
               return true;
           }
         } catch (Exception e) {System.out.println("Exception: " + e.getMessage());}
         return false;
     }
      return true;
   }
   public void setAddress(String address) {
       serverAddress = address;
   }
   public String parseRequest(HttpServletRequest r) {
      String targetString = "";
      try {
        Reader initialReader =  r.getReader();
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        int count = 0;
        while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        initialReader.close();
        targetString = buffer.toString();

      } catch (Exception e) { System.out.println("Found Exception" + e.getMessage());/*report an error*/ }
      return targetString;
   }
   public void buildReturnParameters(JSONWrapper json, HttpServletResponse theResponse) throws IOException {
       ParameterGenerator pg = new ParameterGenerator(serverAddress);
       String returnParameters = pg.getReturnParameters(theResponse, json);
       PrintWriter out = theResponse.getWriter();
       theResponse.setContentType("application/json");
       theResponse.setCharacterEncoding("UTF-8");
       out.print(returnParameters);
       out.flush();
   }


}
