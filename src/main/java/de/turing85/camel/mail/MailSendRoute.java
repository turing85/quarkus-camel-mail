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
  public static final String SEND_MAIL_ROUTE_ID = "send-mail";
  public static final AggregationStrategy NOOP_AGGREGATION_STRATEGY =
      (Exchange original, Exchange resource) -> original;

  @Override
  public void configure() {
    // @formatter:off
    onException(AddressException.class)
        .setHeader(
            Exchange.HTTP_RESPONSE_CODE,
            constant(Response.Status.BAD_REQUEST.getStatusCode()))
        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
        .setBody(constant("address is malformed"))
        .handled(true);
    onException(Exception.class)
        .log(LoggingLevel.ERROR, "Ouchie: ${exception}")
        .handled(false);

    from(platformHttp("/send").httpMethodRestrict("POST"))
        .id("http-to-mail")
        .multicast()
            .aggregationStrategy(NOOP_AGGREGATION_STRATEGY)
            .stopOnException()
            .synchronous()
            .to(direct(SEND_MAIL_ROUTE_ID))
        .end()
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
            .removeHeader("subject")
            .removeHeader("to")
            .removeHeader("from");
    // @formatter:on
  }
}
