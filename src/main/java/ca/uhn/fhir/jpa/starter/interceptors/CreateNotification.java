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
            + "{\"resource\":"
            + str
            + "}]"
            + "}";
        return notification;
    }
}
