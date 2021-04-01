package ca.uhn.fhir.jpa.starter.interceptors;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;

import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryMatchResult;

import ca.uhn.fhir.rest.annotation.Search;

import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryMatchResult;
import ca.uhn.fhir.context.FhirContext;

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
import ca.uhn.fhir.jpa.provider.*;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.*;
// import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.rest.client.api.*;
import ca.uhn.fhir.parser.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ca.uhn.fhir.jpa.starter.utils.JSONWrapper;
import ca.uhn.fhir.jpa.starter.utils.RequestHandler;

@Interceptor
public class SubscriptionChecker {
   // private static final String SUBSCRIPTION_DEBUG_LOG_INTERCEPTOR_PRECHECK = "SubscriptionDebugLogInterceptor_precheck";
   private final Logger myLogger = LoggerFactory.getLogger(SubscriptionChecker.class.getName());

 	 private FhirContext myCtx;

   private String baseUrl;

   private IGenericClient client;

   private RequestHandler requestHandler;
   private IParser jparser;
   private JSONParser parser;

   /**
    * Constructor using a specific logger
    */
   public SubscriptionChecker() {
       configure("localhost:8080", null);
   }
   public SubscriptionChecker(String url, FhirContext ctx) {
      configure(url, ctx);
   }
   private void configure(String url, FhirContext ctx) {
        baseUrl = url;
        myCtx = ctx;
        client = myCtx.newRestfulGenericClient(baseUrl + "/fhir");
        requestHandler = new RequestHandler();
        jparser = myCtx.newJsonParser();
        parser = new JSONParser();
   }
   public void setBaseUrl(String url) {
      baseUrl = url;
   }

   /*
    *   Searches on Subscriptions based on the IDs that are provided. The status are then created
    *   based on those subscriptions
    */


   /**
    * Override the incomingRequestPreProcessed method, which is called
    * for each incoming request before any processing is done
    */
    //This probably needs to be changed to post processed since it relies on the updated resource
    // Need to actually check resource that was posted not the ones in the server
   @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
   public boolean incomingRequestPostProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
     String[] parts = theRequest.getRequestURL().toString().split("/");
     myLogger.info("SUBSCRIPTION CHECKER");
     // Here is where the Subscription Topic should be evaluated
     //
     if (theRequest.getMethod().equals("PUT")
        || theRequest.getMethod().equals("POST")
        && !parts[parts.length - 1].equals("Subscription")
        && !parts[parts.length - 2].equals("Subscription")
        && parts[parts.length - 1].equals("Task")) { // Server only checking Task resources
         myLogger.info("Checking active subscriptions for potential matches");
         for (JSONWrapper subscription: getAllSubscriptions()) {
            // NOTE: The criteria needs to be evaluated against what was sent
            sendNotification(subscription, getNotification(subscription));
         }
         return true;
     }
     return true;
  }
  public List<JSONWrapper> getAllSubscriptions() {
      myLogger.info("Checking all active subscriptions");
      // Only check the criteria on active subscriptions
      Bundle results = searchOnCriteria("/Subscription?status=active");
      List<Bundle.BundleEntryComponent> subs = results.getEntry();
      List<JSONWrapper> retVal=new ArrayList<JSONWrapper>(); // populate this
      for (Bundle.BundleEntryComponent sub: subs) {
          try {
            JSONWrapper subscription = new JSONWrapper(parser.parse(jparser.encodeResourceToString((IBaseResource)sub.getResource())));
            retVal.add(subscription);
          } catch (Exception ex) {
              myLogger.info("Failed to parse subscription");
          }
      }
      return retVal;
  }
  private String getNotification(JSONWrapper subscription) {
      myLogger.info(subscription.toString());
      List<String> criteriaList = getCriteria(subscription);
      List<String> resources = new ArrayList<>();
      for (String c : criteriaList) {
          Bundle r = searchOnCriteria(c);
          for (Bundle.BundleEntryComponent e: r.getEntry()) {
              resources.add(jparser.encodeResourceToString((IBaseResource)e.getResource()));
          }
      }
      String notification = CreateNotification.createResourceNotification(subscription.toString(), resources);
      myLogger.info(notification);
      return notification;
  }
  private String sendNotification(JSONWrapper subscription, String notification) {
      myLogger.info("SENDING STUFF");
      myLogger.info(subscription.toString());
      String endpoint = subscription.get("channel").get("endpoint").toString();
      myLogger.info(endpoint);
      // Add headers from the subscription
      String result = "";
      // requestHandler.setURL(endpoint);
      try {
          result = requestHandler.sendPost(endpoint, notification);
      } catch(Exception e) {
          myLogger.info("Error delivering notification");
      }
      return result;
  }
  private List<String> getCriteria(JSONWrapper sub) {
      List<String> criteria = new ArrayList<>();
      // put in the default criteria
      criteria.add(sub.get("criteria").getValue().toString());
      if (sub.hasKey("_criteria")) {
          // Add each additional criteria
          for (int i = 0; i < sub.get("_criteria").get("extension").size(); i++) {
              criteria.add(sub.get("_criteria").get("extension").get(i).get("valueString").getValue().toString());
          }
      }
      return criteria;
  }
  public Bundle searchOnCriteria(String criteria) {
      Bundle results = client.search().byUrl(criteria)
        .returnBundle(Bundle.class)
        .execute();
      return results;
  }

}
