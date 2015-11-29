package iot.social.google.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

public class GoogleConfigManager {

	private static final String DATA_STORE_DIR = ".credentials/gmail-api-quickstart";

	private static GoogleConfigManager manager;

	private JsonFactory jsonFactory;
	private HttpTransport httpTransport;
	private FileDataStoreFactory dataStoreFactory;
	private List<String> scopes;
	
	private GoogleConfig googleConfig;

	public GoogleConfig getGoogleConfig() {
		return googleConfig;
	}

	private GoogleConfigManager() throws GeneralSecurityException, IOException {
		this.jsonFactory = JacksonFactory.getDefaultInstance();
		this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		this.dataStoreFactory = new FileDataStoreFactory(new File(
				System.getProperty("user.home"), DATA_STORE_DIR));
		this.scopes = new ArrayList<String>();
		this.googleConfig = new GoogleConfig();
	}
	
	private void populateConfig(){
		try {
			Properties prop = new Properties();
			prop.load(getClass().getResourceAsStream("/google.properties"));
			googleConfig.setApplicationName(prop.get("applicationName").toString());
			googleConfig.setAuthorizationUrl(prop.get("authorizationUrl").toString());
			googleConfig.setMailFrom(prop.get("mailFrom").toString());
			googleConfig.setMailTo(prop.get("mailTo").toString());
			googleConfig.setPassword(prop.get("password").toString());
			googleConfig.setUserName(prop.get("userName").toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addScope(final String... scopes) {
		this.scopes.addAll(Arrays.asList(scopes));
	}

	public static GoogleConfigManager getInstance() {
		if (null == manager) {
			try {
				manager = new GoogleConfigManager();
				manager.populateConfig();
			} catch (GeneralSecurityException | IOException e) {
				System.out.println("Could not instantiate GoogleConfigManager.");
				e.printStackTrace();
			}
		}
		return manager;
	}

	public Credential authorise() {
		if(this.scopes.size()<=0){
			throw new IllegalStateException("You need to set the scopes first.");
		}
		InputStream in = GoogleConfigManager.class
				.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = null;
		GoogleAuthorizationCodeFlow flow = null;
		Credential credantial = null;
		try {
			clientSecrets = GoogleClientSecrets.load(jsonFactory,
					new InputStreamReader(in));

			flow = new GoogleAuthorizationCodeFlow.Builder(this.httpTransport,
					this.jsonFactory, clientSecrets, this.scopes)
					.setDataStoreFactory(this.dataStoreFactory)
					.setAccessType("offline").build();

			credantial = new AuthorizationCodeInstalledApp(flow,
					new LocalServerReceiver()).authorize("user");
		} catch (IOException e) {
			System.out.println("failed to read the client secrets.");
			e.printStackTrace();
		}
		return credantial;
	}

}
