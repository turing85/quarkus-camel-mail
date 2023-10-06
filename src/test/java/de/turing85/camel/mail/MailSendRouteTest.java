package de.turing85.camel.mail;

import com.icegreen.greenmail.util.GreenMail;
import de.turing85.camel.mail.resource.GreenMailTestResource;
import de.turing85.camel.mail.resource.InjectGreenMail;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(GreenMailTestResource.class)
class MailSendRouteTest {
  @InjectGreenMail
  GreenMail greenMail;

  @BeforeEach
  void resetGreenMail() {
    greenMail.reset();
  }

  @Test
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
}