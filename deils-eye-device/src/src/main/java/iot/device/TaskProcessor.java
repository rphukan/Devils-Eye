package iot.device;

import iot.device.tasks.BaseTask;
import iot.device.tasks.DetectMotionTask;
import iot.device.tasks.ShutDownTask;


public class TaskProcessor {
	
	private static final String DETECT_MOTION_TASK = "DETECT-MOTION";
	private static final String SHUT_DOWN_TASK = "SHUT-DOWN";
	private static final String CAPTURE_VIDEO_TASK = "CAPTURE_VIDEO_TASK";
	
	public BaseTask processCommand(final Object command){
		BaseTask task = null;
		if(command.toString().equals(DETECT_MOTION_TASK)){
			task = new DetectMotionTask();
			task.setTaskType(BaseTask.SCHEDULED_TASK);
		}
		if(command.toString().equals(SHUT_DOWN_TASK)){
			task = new ShutDownTask();
			task.setTaskType(BaseTask.IMMEDIATE_TASK);
		}
		System.out.println("Executing :" + task.getTaskType());
		return task;
	}

}
