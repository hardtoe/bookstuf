package com.bookstuf.mail;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.github.mustachejava.Mustache;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

public class Mail {
	private static final SendGrid sendgrid =
		new SendGrid("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");
	
	private static final Logger log =
		Logger.getLogger(Mail.class.getCanonicalName());
	
	public static MailBuilder from(final String address, final String name) {
		return new MailBuilder().from(address, name);
	}
	
	public static MailBuilder from(final String address) {
		return new MailBuilder().from(address);
	}
	
	private static class Recipient {
		public final String address;
		public final String name;
		
		public Recipient(
			final String address, 
			final String name
		) {
			this.address = address;
			this.name = name;
		}
		
		public Recipient(
			final String address
		) {
			this.address = address;
			this.name = address;
		}
	}
	
	public static class MailBuilder {
		private boolean flag = false;
		
		private Recipient from;
		private final ArrayList<Recipient> toList;
		private final ArrayList<Recipient> ccList;
		private final ArrayList<Recipient> bccList;
		
		private String subject;
		
		private final HashMap<String, Object> templateParameters;
		private Mustache bodyTemplate;
		
		private String bodyText;
		
		private MailBuilder() {
			this.toList = new ArrayList<Mail.Recipient>();
			this.ccList = new ArrayList<Mail.Recipient>();
			this.bccList = new ArrayList<Mail.Recipient>();
			this.templateParameters = new HashMap<String, Object>();
		}
		
		public MailBuilder flag() {
			this.flag = true;
			return this;
		}
		
		public MailBuilder from(final String address, final String name) {
			from = new Recipient(address, name);
			return this;
		}
		
		public MailBuilder from(final String address) {
			from = new Recipient(address);
			return this;
		}
		
		public MailBuilder to(final String address, final String name) {
			toList.add(new Recipient(address, name));
			return this;
		}
		
		public MailBuilder to(final String address) {
			toList.add(new Recipient(address));
			return this;
		}
		
		public MailBuilder cc(final String address, final String name) {
			ccList.add(new Recipient(address, name));
			return this;
		}
		
		public MailBuilder cc(final String address) {
			ccList.add(new Recipient(address));
			return this;
		}
		
		public MailBuilder bcc(final String address, final String name) {
			bccList.add(new Recipient(address, name));
			return this;
		}
		
		public MailBuilder bcc(final String address) {
			bccList.add(new Recipient(address));
			return this;
		}
		
		public MailBuilder subject(final String subject) {
			this.subject = subject;
			return this;
		}
		
		public MailBuilder param(final String key, final Object value) {
			this.templateParameters.put(key, value);
			return this;
		}
		
		public MailBuilder body(final Mustache body) {
			this.bodyTemplate = body;
			return this;
		}
		
		public MailBuilder body(final String body) {
			this.bodyText = body;
			return this;
		}
		
		public void send() {
			try {
				try {
					sendWithAppEngine();
					
				} catch (final Exception e) {
					sendWithSendgrid();
				}
				
			} catch (final Exception e) {
				log.log(Level.SEVERE, "Could not send email due to exception", e);
				throw new RuntimeException(e);
			}
		}

		private void sendWithSendgrid() throws SendGridException {
			final SendGrid.Email email = 
				new SendGrid.Email();

		    email.setFrom(from.address);
		    email.setFromName(from.name);
		    
		    for (final Recipient to : toList) {
			    email.addTo(to.address);
			    //email.addToName(to.name);
		    }
		    
		    for (final Recipient cc : ccList) {
			    email.addCc(cc.address);
		    }
		    
		    for (final Recipient bcc : bccList) {
			    email.addBcc(bcc.address);
		    }
		    
		    email.setSubject(subject);

		    
		    if (bodyText != null) {
		    	email.setText(bodyText);
		    	
		    } else {
		    	final StringWriter stringWriter =
		    		new StringWriter();
		    	
		    	bodyTemplate.execute(stringWriter, templateParameters);
		    	
		    	final String body = 
		    		stringWriter.toString();
		    	
				email.setText(body);
		    }
		   
		    sendgrid.send(email);
		}

		private void sendWithAppEngine() throws Exception {
			final Properties props = new Properties();
			final Session session = Session.getDefaultInstance(props, null);
				
		    final Message msg = 
		    	new MimeMessage(session);
		    
		    msg.setFrom(new InternetAddress(from.address, from.name));
		    
		    for (final Recipient to : toList) {
		    	msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to.address, to.name));
		    }
		    
		    for (final Recipient cc : ccList) {
		    	msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc.address, cc.name));
		    }
		    
		    for (final Recipient bcc : bccList) {
		    	msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc.address, bcc.name));
		    }
		    
		    msg.setSubject(subject);
		    
		    if (bodyText != null) {
		    	msg.setText(bodyText);
		    	
		    } else {
		    	final StringWriter stringWriter =
		    		new StringWriter();
		    	
		    	bodyTemplate.execute(stringWriter, templateParameters);
		    	
		    	msg.setText(stringWriter.toString());
		    }
		    
		    if (flag) {
		    	msg.setFlag(Flag.FLAGGED, true);
		    }
		    
		    Transport.send(msg);
		}
	}
}
