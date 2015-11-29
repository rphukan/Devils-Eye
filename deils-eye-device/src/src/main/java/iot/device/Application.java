package iot.device;

import javax.mail.MessagingException;

public class Application {

	private final TaskExecutor taskExecutor = TaskExecutor.getInstance();
	private final Communicator communicator = Communicator.getInstance();

	public void initialize() throws MessagingException{
		this.communicator.startMailChecker();
		this.communicator.sendConfigurationMail();
		this.communicator.addMailListener(this.taskExecutor);
	}

	public static void main(String[] args)  {
		Application secureHome = new Application();
		try {
			secureHome.initialize();
			
			secureHome.taskExecutor.execute();

		} catch (MessagingException e) {
			secureHome.taskExecutor.shutDown();
			e.printStackTrace();
		}
	}

}
