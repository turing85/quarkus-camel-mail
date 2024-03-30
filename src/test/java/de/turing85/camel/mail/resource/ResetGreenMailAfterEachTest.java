package de.turing85.camel.mail.resource;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

public interface ResetGreenMailAfterEachTest extends HasGreenMail {

  @BeforeEach
  default void resetGreenMail() {
    if (!getGreenMail().isRunning()) {
      LoggerFactory.getLogger(ResetGreenMailAfterEachTest.class)
          .debug("(Re-)Starting smtp server on port {} after test", getSmtpPort());
      getGreenMail().start();
    }
    getGreenMail().reset();
  }
}
