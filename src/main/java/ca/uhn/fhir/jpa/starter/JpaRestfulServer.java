package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;
import ca.uhn.fhir.jpa.starter.interceptors.TopicInterceptor;
import ca.uhn.fhir.jpa.starter.interceptors.StatusInterceptor;
import ca.uhn.fhir.jpa.starter.interceptors.SubscriptionChecker;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JpaRestfulServer.class);


  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();
    // String serverAddress = appProperties.getServer_address();
    // interceptorService.registerInterceptor(new TopicInterceptor());
    // interceptorService.registerInterceptor(new StatusInterceptor());
    // interceptorService.registerInterceptor(new SubscriptionChecker(serverAddress));
    // Add your own customization here
    // If subscriptions are enabled, we want to register the interceptor that
    // will activate them and match results against them
    ourLog.info("About the check the status of subscriptions again");
    if (appProperties.getSubscription() != null) {
      /*
       * Register SubscriptionTopicInterceptor for Subscriptions
       */


    }

  }

}
