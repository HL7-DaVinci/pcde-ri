package ca.uhn.fhir.jpa.starter.interceptors;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;

import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryResourceMatcher;

import ca.uhn.fhir.rest.annotation.Search;

import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryMatchResult;
import ca.uhn.fhir.jpa.searchparam.matcher.IndexedSearchParamExtractor;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;

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
// import org.hl7.fhir.dstu2.model.BaseDateTimeType;
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

/**
 * Class for intercepting and handling the subsciptions
 */
@Interceptor
public class SubscriptionInterceptor {
   private final Logger myLogger = LoggerFactory.getLogger(SubscriptionInterceptor.class.getName());

 	 private FhirContext myCtx;

   private String baseUrl;

   private IGenericClient client;

   private RequestHandler requestHandler;
   private IParser jparser;
   private JSONParser parser;

   private InMemoryResourceMatcher matcher;
   private IndexedSearchParamExtractor extractor;

   /**
    * Constructor using a specific logger
    */
   public SubscriptionInterceptor() {
       configure("https://davinci-pcde-ri.logicahealth.org", null);
   }

   public SubscriptionInterceptor(String url, FhirContext ctx) {
      configure(url, ctx);
   }
   /**
    * Used for constructors to initilize variables
    * @param url The url for the fhir server
    * @param ctx the fhir context
    */
   private void configure(String url, FhirContext ctx) {
        baseUrl = url;
        myCtx = ctx;
        client = myCtx.newRestfulGenericClient(baseUrl + "/fhir");
        requestHandler = new RequestHandler();
        jparser = myCtx.newJsonParser();
        parser = new JSONParser();
   }
   /**
    * Set the base url
    * @param url the url
    */
   public void setBaseUrl(String url) {
      baseUrl = url;
   }
   /**
    * Override the incomingRequestPostProcessed method, which is called
    * for each request after it has been processed. If it is a Task resource then the subscriptions
    * will be checked
    * @param theRequest
    * @param theResponse
    * @param theResource this is the resource that was posted to the server
    * @param theRequest
    * @return whether to continue
    */
   @Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
   public boolean incomingRequestPostProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse, IBaseResource theResource, RequestDetails theDetails) {
     String[] parts = theRequest.getRequestURL().toString().split("/");
     // Here is where the Subscription Topic should be evaluated
     if (theRequest.getMethod().equals("PUT")
        || theRequest.getMethod().equals("POST")
        && !parts[parts.length - 1].equals("Subscription")
        && !parts[parts.length - 2].equals("Subscription")
        && parts[parts.length - 1].equals("Task")) { // Server only checking Task resources
         myLogger.info("Checking active subscriptions for potential matches");
         myLogger.info(jparser.encodeResourceToString(theResource));
         myLogger.info(theResource.getIdElement().getValue());
         for (JSONWrapper subscription: getAllSubscriptions()) {
            String notification = getNotification(subscription, theResource, theDetails);
            if (!notification.equals("")) {
                sendNotification(subscription, notification);
            }
         }
     }
     return true;
  }
  /**
   * Returns a list of active subscriptions
   * @return all active subscriptions
   */
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
  /**
   * Gets the notification of the resource for the subscription
   * @param  subscription the subscription
   * @param  theResource  the resource just that was just updated
   * @param  theRequest
   * @return              the notification
   */
  private String getNotification(JSONWrapper subscription, IBaseResource theResource, RequestDetails theRequest) {
      myLogger.info(subscription.toString());
      List<String> criteriaList = getCriteria(subscription);
      List<String> resources = new ArrayList<>();
      for (String c : criteriaList) {
          myLogger.info(theResource.fhirType());
          String criteriaWithId = c + "&_id=" + theResource.getIdElement().getValue().split("/")[1];
          myLogger.info(criteriaWithId);
          Bundle r = searchOnCriteria(criteriaWithId);

          for (Bundle.BundleEntryComponent e: r.getEntry()) {
              String resource = jparser.encodeResourceToString((IBaseResource)e.getResource());
              myLogger.info(resource);
              resources.add(resource);
          }
      }
      String notification = "";
      if (resources.size() > 0) {
          notification = CreateNotification.createResourceNotification(subscription.toString(), resources, baseUrl + "/fhir/Subscription/admission/$status");
      }
      return notification;
  }
  /**
   * Send the notification to the endpoint in the subscription
   * @param  subscription the active subscription
   * @param  notification the notification to be sent
   * @return              the result from sending the notification
   */
  private String sendNotification(JSONWrapper subscription, String notification) {
      String endpoint = subscription.get("channel").get("endpoint").toString() + "/Bundle";
      myLogger.info(endpoint);
      //TODO:  Add headers from the subscription
      String result = "";
      try {
          myLogger.info(notification);
          result = requestHandler.sendPost(endpoint, notification);
      } catch(Exception e) {
          myLogger.info("Error delivering notification");
      }
      return result;
  }
  /**
   * Get all the criteria from the subscription
   * @param  sub the subscription
   * @return     a list of criteria
   */
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
  /**
   * Search based on a criteria
   * @param  criteria the string criteria
   * @return          the bundle result
   */
  public Bundle searchOnCriteria(String criteria) {
      Bundle results = client.search().byUrl(criteria)
        .returnBundle(Bundle.class)
        .execute();
      return results;
  }

}
