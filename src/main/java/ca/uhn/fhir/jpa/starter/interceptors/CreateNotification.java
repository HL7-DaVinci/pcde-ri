package ca.uhn.fhir.jpa.starter.interceptors;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

public class CreateNotification {
    /**
     *
     * @return empty notification in string form NOTE: change document to history once complete
     */
    public static String createEmptyNotification(String subscription, String url) {
          String notification = "{\"resourceType\" : \"Bundle\",\"id\" : \"notification-empty\","
            + "\"meta\" : {\"profile\" : [\"http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification\"]},"
            + "\"type\" : \"document\","
            + "\"timestamp\" : \"2020-05-29T11:44:13.1882432-05:00\","
            + "\"entry\" : [{"
            + "\"fullUrl\" : \"urn:uuid:b21e4fae-ce73-45cb-8e37-1e203362b2ae\","
            + "\"resource\" : {"
            + "\"resourceType\" : \"Parameters\","
            + "\"id\" : \"b21e4fae-ce73-45cb-8e37-1e203362b2ae\","
            + "\"meta\" : {\"profile\" : ["
            + "\"http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscriptionstatus\"]},"
            + "\"parameter\" : [{"
            + "\"name\" : \"subscription\","
            + "\"valueReference\" : {"
            + "\"reference\" : \"https://example.org/fhir/r4/Subscription/admission\"}},{"
            + "\"name\" : \"topic\","
            + "\"valueCanonical\" : \"http://hl7.org/SubscriptionTopic/admission\"},{"
            + "\"name\" : \"type\","
            + "\"valueCode\" : \"event-notification\"},"
            + "{\"name\" : \"status\",\"valueCode\" : \"active\"},"
            + "{\"name\" : \"events-since-subscription-start\",\"valueUnsignedInt\" : 310},"
            + "{\"name\" : \"events-in-notification\",\"valueUnsignedInt\" : 1}]},"
            + "\"request\" : {\"method\" : \"GET\",\"url\" : \"" + url + "\""
            + "},\"response\" : {\"status\" : \"200\"}}]}";
        return notification;

    }
    public static String createResourceNotification(String subscription, List<String> resources, String url) {
          if (resources.size() == 0) {
              return createEmptyNotification(subscription, url);
          }
          String str = "";
          for (String r: resources) {
              str += (r + ",");
          }
          if (resources.size() > 0) {
              str = str.substring(0, str.length() - 1);
          }
          String notification = "{"
            + "\"resourceType\" : \"Bundle\","
            + "\"id\" : \"notification-full-resource\","
            + "\"meta\" : {"
            + "\"profile\" : ["
            + "\"http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification\""
            + "]"
            + "},"
            + "\"type\" : \"document\","
            + "\"timestamp\" : \"2020-05-29T11:44:13.1882432-05:00\","
            + "\"entry\" : ["
            + "{"
            + "\"fullUrl\" : \"urn:uuid:b21e4fae-ce73-45cb-8e37-1e203362b2ae\","
            + "\"resource\" : {"
            + "\"resourceType\" : \"Parameters\","
            + "\"id\" : \"b21e4fae-ce73-45cb-8e37-1e203362b2ae\","
            + "\"meta\" : {"
            + "\"profile\" : ["
            + "\"http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscriptionstatus\""
            + "]"
            + "},"
            + "\"parameter\" : ["
            + "{"
            + "\"name\" : \"subscription\","
            + "\"valueReference\" : {"
            + "\"reference\" : \"https://example.org/fhir/r4/Subscription/admission\""
            + "}"
            + "},"
            + "{"
            + "\"name\" : \"topic\","
            + "\"valueCanonical\" : \"http://hl7.org/SubscriptionTopic/admission\""
            + "},"
            + "{"
            + "\"name\" : \"type\","
            + "\"valueCode\" : \"event-notification\""
            + "},"
            + "{"
            + "\"name\" : \"status\","
            + "\"valueCode\" : \"active\""
            + "},"
            + "{"
            + "\"name\" : \"events-since-subscription-start\","
            + "\"valueUnsignedInt\" : 310"
            + "},"
            + "{"
            + "\"name\" : \"events-in-notification\","
            + "\"valueUnsignedInt\" : 1"
            + "}"
            + "]"
            + "},"
            + "\"request\" : {"
            + "\"method\" : \"GET\","
            + "\"url\" : \"" + url + "\""
            + "},"
            + "\"response\" : {"
            + "\"status\" : \"200\""
            + "}"
            + "},"
            + str
            + "]"
            + "}";
        return notification;
    }

    // Below is a full notification
    // {
    //   "resourceType" : "Bundle",
    //   "id" : "notification-full-resource",
    //   "meta" : {
    //     "profile" : [
    //       "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification\"
    //     ]
    //   },
    //   "type" : "history",
    //   "timestamp" : "2020-05-29T11:44:13.1882432-05:00",
    //   "entry" : [
    //     {
    //       "fullUrl" : "urn:uuid:b21e4fae-ce73-45cb-8e37-1e203362b2ae",
    //       "resource" : {
    //         "resourceType" : "Parameters",
    //         "id" : "b21e4fae-ce73-45cb-8e37-1e203362b2ae",
    //         "meta" : {
    //           "profile" : [
    //             "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscriptionstatus"
    //           ]
    //         },
    //         "parameter" : [
    //           {
    //             "name" : "subscription",
    //             "valueReference" : {
    //               "reference" : "https://example.org/fhir/r4/Subscription/admission"
    //             }
    //           },
    //           {
    //             "name" : "topic",
    //             "valueCanonical" : "http://hl7.org/SubscriptionTopic/admission"
    //           },
    //           {
    //             "name" : "type",
    //             "valueCode" : "event-notification"
    //           },
    //           {
    //             "name" : "status",
    //             "valueCode" : "active"
    //           },
    //           {
    //             "name" : "events-since-subscription-start",
    //             "valueUnsignedInt" : 310
    //           },
    //           {
    //             "name" : "events-in-notification",
    //             "valueUnsignedInt" : 1
    //           }
    //         ]
    //       },
    //       "request" : {
    //         "method" : "GET",
    //         "url" : "https://example.org/fhir/r4/Subscription/admission/$status"
    //       },
    //       "response" : {
    //         "status" : "200"
    //       }
    //     },
    //     {
    //       "fullUrl" : "https://example.org/fhir/r4/Encounter/551683b3-1477-41d1-b58e-32fe8b0047b0",
    //       "resource" : {
    //         "resourceType" : "Encounter",
    //         "id" : "551683b3-1477-41d1-b58e-32fe8b0047b0",
    //         "text" : {
    //           "status" : "generated",
    //           "div" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p><p><b>status</b>: in-progress</p><p><b>class</b>: <span title=\"{http://terminology.hl7.org/CodeSystem/v3-ActCode VR}\">virtual</span></p><p><b>subject</b>: <a href=\"Bundle-notification-multi-resource.html#https-//example.org/fhir/r4/Patient/46db3334-dbf5-43f3-868f-93ae0883cce1\">Example Patient. Generated Summary: type: history; timestamp: May 29, 2020, 4:44:13 PM</a></p></div>"
    //         },
    //         "status" : "in-progress",
    //         "class" : {
    //           "system" : "http://terminology.hl7.org/CodeSystem/v3-ActCode",
    //           "code" : "VR"
    //         },
    //         "subject" : {
    //           "reference" : "https://example.org/fhir/r4/Patient/46db3334-dbf5-43f3-868f-93ae0883cce1",
    //           "display" : "Example Patient"
    //         }
    //       },
    //       "request" : {
    //         "method" : "POST",
    //         "url" : "Encounter"
    //       },
    //       "response" : {
    //         "status" : "201"
    //       }
    //     }
    //   ]
    // }



    // Below is an empty notification
    //
    // {
    //   "resourceType" : "Bundle",
    //   "id" : "notification-empty",
    //   "meta" : {
    //     "profile" : [
    //       "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification"
    //     ]
    //   },
    //   "type" : "history",
    //   "timestamp" : "2020-05-29T11:44:13.1882432-05:00",
    //   "entry" : [
    //     {
    //       "fullUrl" : "urn:uuid:b21e4fae-ce73-45cb-8e37-1e203362b2ae",
    //       "resource" : {
    //         "resourceType" : "Parameters",
    //         "id" : "b21e4fae-ce73-45cb-8e37-1e203362b2ae",
    //         "meta" : {
    //           "profile" : [
    //             "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscriptionstatus"
    //           ]
    //         },
    //         "parameter" : [
    //           {
    //             "name" : "subscription",
    //             "valueReference" : {
    //               "reference" : "https://example.org/fhir/r4/Subscription/admission"
    //             }
    //           },
    //           {
    //             "name" : "topic",
    //             "valueCanonical" : "http://hl7.org/SubscriptionTopic/admission"
    //           },
    //           {
    //             "name" : "type",
    //             "valueCode" : "event-notification"
    //           },
    //           {
    //             "name" : "status",
    //             "valueCode" : "active"
    //           },
    //           {
    //             "name" : "events-since-subscription-start",
    //             "valueUnsignedInt" : 310
    //           },
    //           {
    //             "name" : "events-in-notification",
    //             "valueUnsignedInt" : 1
    //           }
    //         ]
    //       },
    //       "request" : {
    //         "method" : "GET",
    //         "url" : "https://example.org/fhir/r4/Subscription/admission/$status"
    //       },
    //       "response" : {
    //         "status" : "200"
    //       }
    //     }
    //   ]
    // }

}
