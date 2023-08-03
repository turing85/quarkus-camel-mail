package de.turing85.camel.mail;

import de.turing85.camel.mail.resource.GreenmailTestResourceLifecycleManager;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(GreenmailTestResourceLifecycleManager.class)
class MailSendRouteTest {
  @Test
  void testSendMail() {
    // @formatter:off
    given()
        .contentType(ContentType.TEXT)
        .body("foo@bar.baz")
        .when()
          .post("/send")
        .then()
          .statusCode(is(HttpResponseStatus.OK.code()))
          .body(is("Hello"));
    // @formatter:on
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
  }
}