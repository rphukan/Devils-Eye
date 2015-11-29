package iot.device.tasks;

public abstract class BaseTask implements Runnable{
	
	public static final String IMMEDIATE_TASK = "IMMEDIATE";
	public static final String SCHEDULED_TASK = "SCHEDULED";
	
	private String taskType;

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
}
