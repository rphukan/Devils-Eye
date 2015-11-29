package iot.social.google.gmail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

public class MailChecker implements Runnable {

	private final Folder folder;

	private final String userId;

	private final String password;

	public MailChecker(final Folder folder, final String userId,
			final String password) {
		this.folder = folder;
		this.userId = userId;
		this.password = password;
	}

	@Override
	public void run() {
		Store store = this.folder.getStore();
		while (true) {
			// TODO: is store.isConnected() thread safe, visibility ?
			try {
				if (!store.isConnected()) {
					store.connect(this.userId, this.password);
					System.out.println("Setting the IMAP connection again with ........");
				}
				if (!this.folder.isOpen()) {
					this.folder.open(Folder.READ_ONLY);
					System.out.println("Opening the folder again.");
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}
}
