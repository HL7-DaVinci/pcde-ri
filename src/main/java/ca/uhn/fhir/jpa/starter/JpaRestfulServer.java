package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;
import ca.uhn.fhir.jpa.starter.interceptors.TopicInterceptor;
import ca.uhn.fhir.jpa.starter.interceptors.StatusInterceptor;
import ca.uhn.fhir.jpa.starter.interceptors.SubscriptionChecker;
import ca.uhn.fhir.jpa.starter.MetadataProvider;

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

  }

}
