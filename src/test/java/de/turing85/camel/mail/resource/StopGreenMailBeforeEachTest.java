package de.turing85.camel.mail.resource;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

public interface StopGreenMailBeforeEachTest extends HasGreenMail {
  @BeforeEach
  default void stopGreenMail() {
    if (getGreenMail().isRunning()) {
      LoggerFactory.getLogger(StopGreenMailBeforeEachTest.class)
          .debug("Stopping smtp server on port {} before test", getSmtpPort());
      getGreenMail().stop();
    }
  }
}
