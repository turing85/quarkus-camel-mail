package de.turing85.camel.mail.resource;

import org.junit.jupiter.api.BeforeEach;

public interface StopGreenMailBeforeEachTest extends HasGreenMail {
  @BeforeEach
  default void stopGreenMail() {
    if (getGreenMail().isRunning()) {
      getGreenMail().stop();
    }
  }
}
