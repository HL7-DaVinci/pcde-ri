package ca.uhn.fhir.jpa.starter;
import ca.uhn.fhir.rest.server.interceptor.*;
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
import ca.uhn.fhir.jpa.starter.CommunicationGenerator;
import ca.uhn.fhir.jpa.starter.JSONWrapper;

public class CommunicationInterceptor extends InterceptorAdapter {

   private int myRequestCount;
   private String serverAddress;

   public int getRequestCount() {
      return myRequestCount;
   }

   /**
    * Override the incomingRequestPostProcessed method, which is called
    * for each incoming request before any processing is done
    */
   @Override
   public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
      // Need to detect where the request is being posted. If to PCDE then can parse
      String endPoint = theRequest.getRequestURL().substring(theRequest.getRequestURL().lastIndexOf("/")+1);
      if (endPoint.equals("PCDE")) {
          String requestString = parseRequest(theRequest);
          try {
            JSONParser parser = new JSONParser();
            JSONWrapper json = new JSONWrapper((JSONObject) parser.parse(requestString));
            if (json.get("resourceType").getValue().equals("Bundle")) {
                if (json.get("entry").get(0).get("resource").get("resourceType").getValue().equals("CommunicationRequest")) {
                    sendCommunicationResponse(json, theResponse);
                }
            } else if (json.get("resourceType").getValue().equals("CommunicationRequest")) {
                if (json.get("payload").get(0).get("extension").get(0).get("valueCodeableConcept").get("coding").get(0).get("code").getValue().equals("pcde")) {
                    sendCommunicationResponse(json, theResponse);
                }
            } else {
                return true;
            }
          } catch (Exception e) {System.out.println("Exception: " + e.getMessage());}

          return false;
      }
      return true;
   }
   public void sendCommunicationResponse(JSONWrapper json, HttpServletResponse theResponse) throws IOException {
       CommunicationGenerator cg = new CommunicationGenerator((JSONObject)json.getValue(), serverAddress);
       String com = cg.makeCommunication(theResponse);
       PrintWriter out = theResponse.getWriter();
       theResponse.setContentType("application/json");
       theResponse.setCharacterEncoding("UTF-8");
       out.print(com);
       out.flush();
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

}
