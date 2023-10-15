package de.turing85.camel.mail;

import java.io.IOException;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.google.common.truth.Truth;
import com.icegreen.greenmail.util.GreenMail;
import de.turing85.camel.mail.resource.GreenMailTestResource;
import de.turing85.camel.mail.resource.InjectGreenMail;
import de.turing85.camel.mail.resource.ResetGreenMailAfterEachTest;
import de.turing85.camel.mail.resource.StopGreenMailBeforeEachTest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(GreenMailTestResource.class)
@Getter
@DisplayName("Test: Sending mails")
class MailSendRouteTest {
  @InjectGreenMail
  GreenMail greenMail;

  @Nested
  @DisplayName("SMTP is available")
  class SmtpAvailable implements ResetGreenMailAfterEachTest {
    @Test
    @DisplayName("valid email -> send mail")
    void testSendMail() throws MessagingException, IOException {
      final String expectedRecipient = "foo@bar.baz";
      // @formatter:off
      RestAssured
          .given()
              .contentType(MediaType.TEXT_PLAIN)
              .body(expectedRecipient)
          .when()
              .post("/send")
          .then()
              .statusCode(is(HttpResponseStatus.OK.code()))
              .contentType(MediaType.TEXT_PLAIN)
              .header("from", is(nullValue()))
              .header("to", is(nullValue()))
              .header("subject", is(nullValue()))
              .body(is("mail sent"));
      // @formatter:on
      final MimeMessage[] messages = getGreenMail().getReceivedMessages();
      Truth.assertThat(messages).hasLength(1);
      final MimeMessage message = messages[0];
      Truth.assertThat(message.getFrom()).hasLength(1);
      Truth.assertThat(message.getFrom()[0].toString()).isEqualTo("foo@bar.baz");
      Truth.assertThat(message.getRecipients(Message.RecipientType.TO)).hasLength(1);
      Truth.assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString())
          .isEqualTo(expectedRecipient);
      Truth.assertThat(message.getSubject()).isEqualTo("important");
      Truth.assertThat(message.getContent()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("invalid email -> bad request")
    void testSendMailWithInvalidAddress() {
      // @formatter:off
      RestAssured
          .given()
              .contentType(MediaType.TEXT_PLAIN)
              .body("foo@bar.baz broken")
          .when()
              .post("/send")
          .then()
              .statusCode(is(Response.Status.BAD_REQUEST.getStatusCode()))
              .contentType(MediaType.TEXT_PLAIN)
              .header("from", is(nullValue()))
              .header("to", is(nullValue()))
              .header("subject", is(nullValue()))
              .body(is("address is malformed"));
      // @formatter:on
      Truth.assertThat(getGreenMail().getReceivedMessages()).hasLength(0);
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
    @DisplayName("invalid email -> bad request")
    void testSendMailWithInvalidAddress() {
      // @formatter:off
      RestAssured
          .given()
              .contentType(MediaType.TEXT_PLAIN)
              .body("foo@bar.baz broken")
          .when()
              .post("/send")
          .then()
              .statusCode(is(Response.Status.BAD_REQUEST.getStatusCode()))
              .contentType(MediaType.TEXT_PLAIN)
              .header("from", is(nullValue()))
              .header("to", is(nullValue()))
              .header("subject", is(nullValue()))
              .body(is("address is malformed"));
      // @formatter:on
      Truth.assertThat(getGreenMail().getReceivedMessages()).hasLength(0);
    }

    @Test
    @DisplayName("valid email -> error")
    void testSendMail() {
      // @formatter:off
      RestAssured
          .given()
              .contentType(MediaType.TEXT_PLAIN)
              .body("foo@bar.baz")
          .when()
              .post("/send")
          .then()
              .statusCode(is(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()))
              .contentType(MediaType.TEXT_PLAIN)
              .header("from", is(nullValue()))
              .header("to", is(nullValue()))
              .header("subject", is(nullValue()))
              .body(is(emptyString()));
      // @formatter:on
    }

    @Override
    public GreenMail getGreenMail() {
      return MailSendRouteTest.this.getGreenMail();
    }
  }
}
