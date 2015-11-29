package iot.device.tasks;

import iot.device.Communicator;

import java.io.IOException;

import javax.mail.MessagingException;

public class DetectMotionTask extends BaseTask {
	
	private static final String DIR = "D:\\Projects\\IoT\\Home Surveillance\\";
	
	private final Communicator communicator = Communicator.getInstance();
	
	@Override
	public void run() {
		String[] command= {"C:\\Windows\\System32\\cmd.exe", "test.cmd"};
		//String[] command= {};
		ProcessBuilder process = new ProcessBuilder(command);
		try {
			process.start();
			this.mailImage();
		} catch (IOException | MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private void mailImage() throws MessagingException, IOException{
		this.communicator.sendMailWithAttachment("Motion detected", "Some activity have been  detected at your home. Check the attachment.", DIR, "image.jpg");
	}
}
