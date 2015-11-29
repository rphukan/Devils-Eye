package iot.device.tasks;

import iot.device.TaskExecutor;

public class ShutDownTask extends BaseTask {

	private final TaskExecutor taskExecutor = TaskExecutor.getInstance();

	@Override
	public void run() {
		this.taskExecutor.shutDown();
		this.taskExecutor.getShutDown().set(false);
	}

}
