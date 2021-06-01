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

import ca.uhn.fhir.jpa.starter.utils.*;

/**
 *  Interceptor for processing tasks related to PCDE
 */
@Interceptor
public class TaskInterceptor {

   private String serverAddress;

   /**
    * Override the incomingRequestPreProcessed method, which is called
    * for each incoming request before any processing is done
    * @param  theRequest  the request containing the task
    * @param  theResponse the response to send
    * @return             whether to continue
    */
   @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
   public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
      String endPoint = theRequest.getRequestURL().substring(theRequest.getRequestURL().lastIndexOf("/")+1);
      boolean putTask = theRequest.getMethod().equals("PUT")
         && theRequest.getRequestURL().substring(theRequest.getRequestURL().lastIndexOf("/") - 4, theRequest.getRequestURL().lastIndexOf("/")).equals("Task")
         && theRequest.getRequestURL().lastIndexOf("PCDE") >= 0;
      boolean postTask = endPoint.equals("Task")
         && theRequest.getRequestURL().substring(theRequest.getRequestURL().lastIndexOf("/") - 4, theRequest.getRequestURL().lastIndexOf("/")).equals("PCDE");
      if (putTask || postTask) {
         String requestString = parseRequest(theRequest);
         try {
           JSONParser parser = new JSONParser();
           JSONWrapper json = new JSONWrapper((JSONObject) parser.parse(requestString));
           if (json.get("resourceType").getValue().equals("Task")) {
               // At this point we have the task
               handleTask(json, theResponse, theRequest.getMethod());
           } else {
               return true;
           }
         } catch (Exception e) {System.out.println("Exception: " + e.getMessage());}
         return false;
      }
      return true;
   }
   /**
    * Handle the task and update accordingly
    * @param  json        the Task
    * @param  theResponse the reponse that will be updated
    * @param  type        the request type
    * @throws IOException exception from the print writer
    */
   public void handleTask(JSONWrapper json, HttpServletResponse theResponse, String type) throws IOException {
       TaskHandler th = new TaskHandler(serverAddress);
       String originalTask = json.toString();
       String newTaskID = th.handleTask(json, type);
       theResponse.setStatus(201);
       PrintWriter out = theResponse.getWriter();
       theResponse.setContentType("application/json");
       theResponse.setCharacterEncoding("UTF-8");
       if (!newTaskID.equals("")) {
           try {
             JSONParser parser = new JSONParser();
             JSONWrapper taskJson = new JSONWrapper((JSONObject) parser.parse(originalTask));
             taskJson.put("id", newTaskID);
             originalTask = taskJson.toString();
            } catch (Exception e) {System.out.println("Exception: " + e.getMessage());}

        }
       out.print(originalTask);
       out.flush();
   }
   /**
    * Set the address of the server
    * @param address the address
    */
   public void setAddress(String address) {
       serverAddress = address;
   }
   /**
    * Parse the request and return the body data
    * @param  r the request
    * @return   the data from the request
    */
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
