package de.turing85.camel.mail;

import jakarta.mail.internet.AddressException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.platformHttp;

@SuppressWarnings("unused")
public class MailSendRoute extends RouteBuilder {
  public static final String HTTP_ENDPOINT = "/send";
  public static final String SEND_MAIL_ROUTE_ID = "send-mail";
  private static final AggregationStrategy NOOP_AGGREGATION_STRATEGY =
      (Exchange original, Exchange resource) -> original;

  @Override
  public void configure() {
    // @formatter:off
    from(platformHttp(HTTP_ENDPOINT).httpMethodRestrict("POST"))
        .id("http-to-mail")
        .onException(AddressException.class)
            .log("Bad request")
            .setHeader(
                Exchange.HTTP_RESPONSE_CODE,
                constant(Response.Status.BAD_REQUEST.getStatusCode()))
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
            .setBody(constant("Address is malformed"))
            .logStackTrace(true)
            .handled(true)
        .end()
        .onException(Exception.class)
            .log(LoggingLevel.ERROR, "${exception.stacktrace}")
            .setHeader(
                Exchange.HTTP_RESPONSE_CODE,
                constant(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
            .setBody(constant("ISE"))
            .handled(true)
        .end()
        .to(direct(SEND_MAIL_ROUTE_ID))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.OK.getStatusCode()))
        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
        .setBody(constant("mail sent"));

    from(direct(SEND_MAIL_ROUTE_ID))
        .id(SEND_MAIL_ROUTE_ID)
        .doTry()
            .setHeader("from", constant("foo@bar.baz"))
            .setHeader("to", bodyAs(String.class))
            .setHeader("subject", constant("important"))
            .setBody(constant("Hello"))
            .log("Sending mail to ${header.to}")
            .to("smtp://{{smtp.host}}")
            .log("Mail sent to ${header.to}")
        .doFinally()
            .removeHeaders("from|to|subject");
    // @formatter:on
  }
}
