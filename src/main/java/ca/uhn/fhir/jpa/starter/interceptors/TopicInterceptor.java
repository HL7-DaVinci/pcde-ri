package ca.uhn.fhir.jpa.starter.interceptors;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Date;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.*;
import java.io.*;


@Interceptor
public class TopicInterceptor {
   private final Logger myLogger;

   /**
    * Constructor using a specific logger
    */
   public TopicInterceptor() {
       myLogger = LoggerFactory.getLogger(TopicInterceptor.class.getName());
   }

   /**
    * Override the incomingRequestPreProcessed method, which is called
    * for each incoming request before any processing is done
    */
   @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
   public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
     String[] parts = theRequest.getRequestURL().toString().split("/");
     if (parts[parts.length - 1].equals("$topic-list") && parts[parts.length - 2].equals("Subscription") && theRequest.getMethod().equals("GET")) {
        myLogger.info("Request received for $topic-list");
         try {
            handleTopicList(theResponse);
         } catch (Exception e) {System.out.println("Exception: " + e.getMessage());}
         return false;
    }
     return true;
  }
  public void handleTopicList(HttpServletResponse theResponse) throws IOException {
      theResponse.setStatus(200);
      PrintWriter out = theResponse.getWriter();
      theResponse.setContentType("application/json");
      theResponse.setCharacterEncoding("UTF-8");
      String outputString = ResourceLoader.loadResource("topic-list.json");
      out.print(outputString);
      out.flush();
  }

}
