package de.turing85.camel.mail.resource;

import org.junit.jupiter.api.BeforeEach;

public interface ResetGreenMailAfterEachTest extends HasGreenMail {
  @BeforeEach
  default void resetGreenMail() {
    if (!getGreenMail().isRunning()) {
      getGreenMail().start();
    }
    getGreenMail().reset();
  }
}
