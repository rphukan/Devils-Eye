package iot.social.google.gmail;

import iot.social.google.config.GoogleConfig;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class UserAuthenticator extends Authenticator{
	
	private final GoogleConfig googleConfig;
	
	public UserAuthenticator(final GoogleConfig config) {
		this.googleConfig = config;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.googleConfig.getUserName(), this.googleConfig.getPassword());
	}
}
