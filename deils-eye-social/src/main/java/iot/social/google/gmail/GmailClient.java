package iot.social.google.gmail;

import iot.social.google.config.GoogleConfig;
import iot.social.google.config.GoogleConfigManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Observable;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages.Send;
import com.google.api.services.gmail.model.Message;
import com.sun.mail.imap.IMAPStore;

public class GmailClient extends Observable{

	private static GmailClient gmail;

	private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	private final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	private final GoogleConfig googleConfig = GoogleConfigManager.getInstance().getGoogleConfig();	
	
	private final Session session;

	private GmailClient() throws GeneralSecurityException, IOException {
		Properties sessionProps = new Properties();
		sessionProps.load(getClass().getResourceAsStream("/gmail.properties"));
		this.session = Session.getInstance(sessionProps, new UserAuthenticator(this.googleConfig) );
	}

	public static GmailClient getInstance() {
		if (null == gmail) {
			try {
				gmail = new GmailClient();
			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
		}
		return gmail;
	}

	public Gmail getGmailService(final Credential credential) {
		Gmail service = new Gmail.Builder(this.httpTransport, this.jsonFactory,credential)
							.setApplicationName(this.googleConfig.getApplicationName())
							.build();
		return service;
	}

	public String sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		email.writeTo(bytes);
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		
		Send send = service.users().messages().send(userId, message);
		message = send.execute();
		return message.getId();
	}
	
	public String sendMessage(MimeMessage email) throws MessagingException  {
		Transport.send(email);
		return "Mail sent";
	}

	
	public MimeMessage createEmail(final String to, final String from, final String subject, final String bodyText)	throws MessagingException {

		MimeMessage email = new MimeMessage(this.session);
		InternetAddress tAddress = new InternetAddress(to);
		InternetAddress fAddress = new InternetAddress(from);

		email.setFrom(fAddress);
		email.addRecipient(RecipientType.TO, tAddress);
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}

	public MimeMessage createEmailWithAttachment(String to, String from,
			String subject, String bodyText, String fileDir, String filename)
			throws MessagingException, IOException {

		MimeMessage email = new MimeMessage(this.session);
		InternetAddress tAddress = new InternetAddress(to);
		InternetAddress fAddress = new InternetAddress(from);

		email.setFrom(fAddress);
		email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
		email.setSubject(subject);

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(bodyText, "text/plain");
		mimeBodyPart.setHeader("Content-Type", "text/plain; charset=\"UTF-8\"");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);

		mimeBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(fileDir + filename);

		mimeBodyPart.setDataHandler(new DataHandler(source));
		mimeBodyPart.setFileName(filename);
		String contentType = Files.probeContentType(FileSystems.getDefault()
				.getPath(fileDir, filename));
		mimeBodyPart.setHeader("Content-Type", contentType + "; name=\""
				+ filename + "\"");
		mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");

		multipart.addBodyPart(mimeBodyPart);

		email.setContent(multipart);

		return email;
	}
	
	public void checkMail(){
		IMAPStore store = null;
		Folder inbox = null;
		
		try {
			store = (IMAPStore)this.session.getStore("imaps");
			store.connect(this.googleConfig.getUserName(), this.googleConfig.getPassword());
			inbox = store.getFolder("INBOX");
			inbox.addMessageCountListener(new MessageCountAdapter() {
				@Override
				public void messagesAdded(MessageCountEvent evt) {
					System.out.println("New message received ......");
					javax.mail.Message[] messages= evt.getMessages();
					for (javax.mail.Message message : messages) {
						String subject;
						try {
							subject = message.getSubject();
							processComand(subject);
							System.out.println("Mail subject :" + subject);
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					}
				
				};
			});
			Thread mailChecker = new Thread(new MailChecker(inbox, this.googleConfig.getUserName(), this.googleConfig.getPassword()));
			mailChecker.setDaemon(false);
			mailChecker.start();

		} catch (MessagingException e) {
			e.printStackTrace();
		} 
		
	}
	
	private void processComand(String subject){
		this.setChanged();
		this.notifyObservers(subject);
	}

}
