package com.bookstuf.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.apphosting.api.ApiProxy.OverQuotaException;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

public class Mail {
	// SendGrid API Key: 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
	
	private static final SendGrid sendgrid =
		new SendGrid("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");
	
	private static final Logger log =
		Logger.getLogger(Mail.class.getCanonicalName());
	
	public static void mail(
		final String fromAddress,
		final String fromName,
		final String toAddress,
		final String toName,
		final String subject,
		final String body
	) {
		try {
			try {
				appEngineMail(fromAddress, fromName, toAddress, toName, subject, body);
				
			} catch (OverQuotaException e) {
				sendGridMail(fromAddress, fromName, toAddress, toName, subject, body);
			}
			
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Could not send email due to exception", e);
			throw new RuntimeException(e);
		}
	}
	
	private static void sendGridMail(
		final String fromAddress,
		final String fromName,
		final String toAddress,
		final String toName,
		final String subject,
		final String body
	) throws 
		SendGridException
	{
		final SendGrid.Email email = 
			new SendGrid.Email();
		
	    email.addTo(toAddress, toName);
	    email.setFrom(fromAddress);
	    email.setFromName(fromName);
	    email.setSubject(subject);
	    email.setText(body);
		    
	   sendgrid.send(email);
	}
	
	private static void appEngineMail(
		final String fromAddress,
		final String fromName,
		final String toAddress,
		final String toName,
		final String subject,
		final String body
	) throws 
		UnsupportedEncodingException, 
		MessagingException 
	{
		final Properties props = new Properties();
		final Session session = Session.getDefaultInstance(props, null);
			
	    final Message msg = 
	    	new MimeMessage(session);
	    
	    msg.setFrom(new InternetAddress(fromAddress, fromName));
	    
	    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress, toName));
	    
	    msg.setSubject(subject);
	    
	    msg.setText(body);
	    
	    Transport.send(msg);
	}
}
