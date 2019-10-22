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

public class CommunicationInterceptor extends InterceptorAdapter {

   private int myRequestCount;

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
      System.out.println(endPoint);
      if (endPoint.equals("PCDE")) {
          String requestString = parseRequest(theRequest);
          System.out.println(requestString);
          try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestString);
            System.out.println(((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONArray)((JSONObject)((JSONArray)json.get("payload")).get(0)).get("extension")).get(0)).get("valueCodeableConcept")).get("coding")).get(0)).get("code").toString());
            if (((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONArray)((JSONObject)((JSONArray)json.get("payload")).get(0)).get("extension")).get(0)).get("valueCodeableConcept")).get("coding")).get(0)).get("code").toString().equals("pcde")) {
                System.out.println(json);
                CommunicationGenerator cg = new CommunicationGenerator(json);
                String com = cg.makeCommunication();
                System.out.println(com);
                PrintWriter out = theResponse.getWriter();
                theResponse.setContentType("application/json");
                theResponse.setCharacterEncoding("UTF-8");
                out.print(com);
                out.flush();
            } else {
                return true;
            }
          } catch (Exception e) {System.out.println("Exception:"+e);}

          return false;
      }
      return true;
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

      } catch (Exception e) { System.out.println("Found Exception" + e.toString());/*report an error*/ }
      return targetString;
   }

}
