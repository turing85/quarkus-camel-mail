package de.turing85.camel.mail.resource;

import java.util.Map;
import java.util.Random;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreenMailTestResource implements QuarkusTestResourceLifecycleManager {
  private static final Random RANDOM = new Random();
  private final int smtpPort = RANDOM.nextInt(1024, 65536);
  private final GreenMail greenMail = new GreenMail(new ServerSetup(smtpPort, "0.0.0.0", "smtp"));

  @Override
  public Map<String, String> start() {
    log.debug("Starting smtp server on port {}", smtpPort);
    greenMail.start();
    return Map.of("smtp.host", "localhost:%d".formatted(smtpPort));
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(greenMail,
        new TestInjector.AnnotatedAndMatchesType(InjectGreenMail.class, GreenMail.class));
    testInjector.injectIntoFields(smtpPort,
        new TestInjector.AnnotatedAndMatchesType(InjectGreenMail.class, int.class));
  }

  @Override
  public void stop() {
    log.debug("Stopping smtp server on port {}", smtpPort);
    greenMail.stop();
  }
}
