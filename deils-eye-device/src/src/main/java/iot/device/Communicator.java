package iot.device;

import java.io.IOException;
import java.util.Observer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import iot.social.google.config.GoogleConfig;
import iot.social.google.config.GoogleConfigManager;
import iot.social.google.gmail.GmailClient;

public class Communicator {

	private static Communicator communicator;
	
	private final GoogleConfigManager googleConfigManager = GoogleConfigManager.getInstance();
	private final GmailClient gmailClient = GmailClient.getInstance();
	private final GoogleConfig googleConfig;
	
	private Communicator() {
		this.googleConfigManager.addScope(GmailScopes.GMAIL_COMPOSE);
		this.googleConfig = this.googleConfigManager.getGoogleConfig();
	}
	
	public static Communicator getInstance(){
		if(null==communicator){
			communicator = new Communicator();
		}
		return communicator;
	}	
	
	public void addMailListener(Observer listener){
		this.gmailClient.addObserver(listener);
	}

	public void sendConfigurationMail() throws MessagingException {
		String subject = "Configure home security.";
		StringBuilder body = new StringBuilder();
		body.append("Click the link below and perform the authorization. Once authorized send back the authorization code with subject line as \"GOOGLE-CODE\".");
		String url = this.googleConfig.getAuthorizationUrl();
		body.append(url);
		this.sendMail(subject, body.toString());
	}

	public void startMailChecker() {
		gmailClient.checkMail();
	}

	public void sendMailAfterAuth(final String subject, final String bodyText) throws MessagingException, IOException {
		String to = this.googleConfig.getMailTo();
		String from = this.googleConfig.getMailFrom();
		String userId = this.googleConfig.getUserName();

		MimeMessage email = gmailClient.createEmail(to, from, subject, bodyText);
		Gmail service = gmailClient.getGmailService(this.googleConfigManager.authorise());
		this.gmailClient.sendMessage(service, userId, email);
	}

	public void sendMail(final String subject, final String bodyText) throws MessagingException {
		String to = this.googleConfig.getMailTo();
		String from = this.googleConfig.getMailFrom();
		MimeMessage email = gmailClient.createEmail(to, from, subject, bodyText);
		gmailClient.sendMessage(email);
	}

	public void sendMailWithAttachment(final String subject, final String bodyText, final String fileDir, final String filename)
			throws MessagingException, IOException {
		String to = this.googleConfig.getMailTo();
		String from = this.googleConfig.getMailFrom();

		MimeMessage email = gmailClient.createEmailWithAttachment(to, from,	subject, bodyText, fileDir, filename);
		gmailClient.sendMessage(email);
	}

}
