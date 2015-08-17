package com.bookstuf;

import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.geronimo.mail.util.StringBufferOutputStream;

public class Luke {
	private static final Logger log = 
		Logger.getLogger(Luke.class.getCanonicalName());
	
	public static void sendEmail(
		final String subject, 
		final String message, 
		final Throwable t
	) {
		final Properties props = new Properties();
		final Session session = Session.getDefaultInstance(props, null);
		
		try {
			final StringBuffer stackTrace =
				new StringBuffer();
			
			final StringBufferOutputStream stackTraceStream =
				new StringBufferOutputStream(stackTrace);
			
			t.printStackTrace(new PrintStream(stackTraceStream));
			
		    final Message msg = 
		    	new MimeMessage(session);
		    
		    msg.setFrom(new InternetAddress("noreply@bookstuf.com", "bookstuf.com"));
		    
		    msg.addRecipient(Message.RecipientType.TO, new InternetAddress("lvalenty@gmail.com"));
		    
		    msg.setFlag(Flag.FLAGGED, true);
		    
		    msg.setSubject(subject);
		    
		    msg.setText(
	    		message + 
	    		"\n\n" +
	    		stackTrace.toString());
		    
		    Transport.send(msg);

		} catch (final Exception e) {
		    log.log(Level.SEVERE, "FATAL - EMAIL: unable to send admin failure email", e);
		}
	}
}
