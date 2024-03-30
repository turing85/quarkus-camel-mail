package de.turing85.camel.mail.resource;

import com.icegreen.greenmail.util.GreenMail;

interface HasGreenMail {
  int getSmtpPort();

  GreenMail getGreenMail();
}
