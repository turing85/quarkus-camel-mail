package de.turing85.camel.mail;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.platformHttp;

import javax.mail.internet.AddressException;
import org.apache.camel.builder.RouteBuilder;

@SuppressWarnings("unused")
public class MailSendRoute extends RouteBuilder {
  @Override
  public void configure() {
    onException(AddressException.class)
        .log("Ouchie: ${exception}")
        .handled(false);

    from(
        platformHttp("/send")
            .httpMethodRestrict("POST"))
        .setHeader("to", body())
        .log("Sending mail to ${header.to}")
        .setHeader("subject", constant("important"))
        .setHeader("from", constant("foo@bar.baz"))
        .setBody(constant("Hello"))
        .to("smtp://{{smtp.host}}")
        .log("Mail sent");
  }
}