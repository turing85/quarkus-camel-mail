package de.turing85.camel.mail;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.platformHttp;

import javax.enterprise.context.ApplicationScoped;
import javax.mail.internet.AddressException;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MailSendRoute extends RouteBuilder {
  private final String smtpHost;

  public MailSendRoute(@ConfigProperty(name = "smtp.host") String smtpHost) {
    this.smtpHost = smtpHost;
  }

  @Override
  public void configure() {
    onException(AddressException.class)
        .log("Ouchie: ${exception}")
        .handled(false);

    from(
        platformHttp("/send")
            .httpMethodRestrict("POST"))
        .setProperty("receiver", body())
        .setHeader("to", simple("${exchangeProperty.receiver}"))
        .log("Sending mail to ${header.to}")
        .setHeader("subject", constant("important"))
        .setHeader("from", constant("foo@bar.baz"))
        .setBody(constant("Hello"))
        .to("smtp://%s".formatted(smtpHost))
        .log("Mail sent");
  }
}