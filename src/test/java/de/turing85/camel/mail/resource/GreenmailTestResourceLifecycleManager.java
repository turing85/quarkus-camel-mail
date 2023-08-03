package de.turing85.camel.mail.resource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class GreenmailTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
  private static final String DEFAULT_IMAGE = "docker.io/greenmail/standalone:2.0.0";
  private static final int CONTAINER_SMTP_PORT = 3025;

  private static final String IMAGE = System.getProperty("greenmail.image", DEFAULT_IMAGE);

  private GenericContainer<?> greenmailContainer;

  @Override
  public Map<String, String> start() {
    greenmailContainer = new GenericContainer<>(IMAGE)
        .withExposedPorts(CONTAINER_SMTP_PORT);
    greenmailContainer.start();

    int mappedSmtpPort = greenmailContainer.getMappedPort(CONTAINER_SMTP_PORT);
    return Map.of("smtp.host", "localhost:%d".formatted(mappedSmtpPort));
  }

  @Override
  public void stop() {
    greenmailContainer.stop();
  }
}
