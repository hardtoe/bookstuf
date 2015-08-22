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

import com.bookstuf.mail.Mail;

public class Luke {
	private static final Logger log = 
		Logger.getLogger(Luke.class.getCanonicalName());
	
	public static void email(
		final String subject, 
		final String message, 
		final Throwable t
	) {
		try {
			final StringBuffer stackTrace =
				new StringBuffer();
			
			final StringBufferOutputStream stackTraceStream =
				new StringBufferOutputStream(stackTrace);
			
			t.printStackTrace(new PrintStream(stackTraceStream));

		    Mail.mail(
		    	"noreply@bookstuf.com", "bookstuf.com", 
		    	
		    	"lvalenty@gmail.com", "lvalenty@gmail.com", 
		    	
		    	subject, 
		    	
		    	message + 
	    		"\n\n" +
	    		stackTrace.toString());    

		} catch (final Exception e) {
		    log.log(Level.SEVERE, "FATAL - EMAIL: unable to send admin failure email", e);
		}
	}
}
