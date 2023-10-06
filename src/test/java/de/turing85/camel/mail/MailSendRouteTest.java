package de.turing85.camel.mail;

import java.io.IOException;
import java.util.List;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.icegreen.greenmail.util.GreenMail;
import de.turing85.camel.mail.resource.GreenMailTestResource;
import de.turing85.camel.mail.resource.InjectGreenMail;
import de.turing85.camel.mail.resource.ResetGreenMailAfterEachTest;
import de.turing85.camel.mail.resource.StopGreenMailBeforeEachTest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(GreenMailTestResource.class)
@Getter
@DisplayName("Test sending mails")
class MailSendRouteTest {
  @InjectGreenMail
  GreenMail greenMail;

  @Nested
  @DisplayName("SMTP is available")
  class SmtpAvailable implements ResetGreenMailAfterEachTest {
    @Test
    @DisplayName("valid email -> send mail")
    void testSendMail() throws MessagingException, IOException {
      String expectedRecipient = "foo@bar.baz";
      // @formatter:off
      given()
        .contentType(ContentType.TEXT)
        .body(expectedRecipient)
        .when()
          .post("/send")
        .then()
          .statusCode(is(HttpResponseStatus.OK.code()))
          .body(is("Hello"));
      // @formatter:on
      List<MimeMessage> messages = List.of(greenMail.getReceivedMessages());
      assertThat(messages).hasSize(1);
      MimeMessage message = messages.get(0);
      assertThat(message.getRecipients(Message.RecipientType.TO)).hasLength(1);
      assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString())
          .isEqualTo(expectedRecipient);
      assertThat(message.getSubject()).isEqualTo("important");
      assertThat(message.getContent()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("invalid email -> error")
    void testSendMailWithInvalidAddress() {
      // @formatter:off
      given()
          .contentType(ContentType.TEXT)
          .body("foo@bar.baz broken")
          .when()
            .post("/send")
          .then()
            .statusCode(is(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
      // @formatter:on

      List<MimeMessage> messages = List.of(greenMail.getReceivedMessages());
      assertThat(messages).hasSize(0);
    }

    @Override
    public GreenMail getGreenMail() {
      return MailSendRouteTest.this.getGreenMail();
    }
  }

  @Nested
  @DisplayName("SMTP is not available")
  class SmtpNotAvailable implements StopGreenMailBeforeEachTest {
    @Test
    @DisplayName("error")
    void testSendMail() {
      // @formatter:off
      given()
        .contentType(ContentType.TEXT)
        .body("foo@bar.baz")
        .when()
          .post("/send")
        .then()
          .statusCode(is(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
      // @formatter:on
    }

    @Override
    public GreenMail getGreenMail() {
      return MailSendRouteTest.this.getGreenMail();
    }
  }
}
