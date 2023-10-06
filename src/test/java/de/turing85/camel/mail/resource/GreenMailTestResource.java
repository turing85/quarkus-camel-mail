package de.turing85.camel.mail.resource;

import java.util.Map;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class GreenMailTestResource implements QuarkusTestResourceLifecycleManager {
  private final GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);

  @Override
  public Map<String, String> start() {
    greenMail.start();
    return Map.of("smtp.host", "localhost:%d".formatted(ServerSetupTest.SMTP.getPort()));
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(greenMail,
        new TestInjector.AnnotatedAndMatchesType(InjectGreenMail.class, GreenMail.class));
  }

  @Override
  public void stop() {
    greenMail.stop();
  }
}
