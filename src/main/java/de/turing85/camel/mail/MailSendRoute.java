package de.turing85.camel.mail;

import jakarta.mail.internet.AddressException;
import jakarta.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.platformHttp;

@SuppressWarnings("unused")
public class MailSendRoute extends RouteBuilder {
  public static final String SEND_MAIL_ROUTE_ID = "send-mail";

  @Override
  public void configure() {
    onException(AddressException.class)
        .setHeader(
            Exchange.HTTP_RESPONSE_CODE,
            constant(Response.Status.BAD_REQUEST.getStatusCode()))
        .setBody(constant("address is malformed"))
        .handled(true);
    onException(Exception.class)
        .log(LoggingLevel.ERROR, "Ouchie: ${exception}")
        .handled(false);

    // @formatter:off
    from(platformHttp("/send").httpMethodRestrict("POST"))
        .id("http-to-mail")
        .multicast()
            .aggregationStrategy((Exchange original, Exchange resource) -> original)
            .stopOnException()
            .synchronous()
            .to(direct(SEND_MAIL_ROUTE_ID))
        .end()
        .setBody(constant("mail sent"));

    from(direct(SEND_MAIL_ROUTE_ID))
        .id(SEND_MAIL_ROUTE_ID)
        .doTry()
            .setHeader("from", constant("foo@bar.baz"))
            .setHeader("subject", constant("important"))
            .setHeader("to", bodyAs(String.class))
            .setBody(constant("Hello"))
            .log("Sending mail to ${header.to}")
            .to("smtp://{{smtp.host}}")
            .log("Mail sent to ${header.to}")
        .doFinally()
            .removeHeader("to")
            .removeHeader("subject")
            .removeHeader("from");
    // @formatter:on
  }
}