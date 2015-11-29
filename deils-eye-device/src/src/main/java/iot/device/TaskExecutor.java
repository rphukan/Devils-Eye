package iot.device;

import iot.device.tasks.BaseTask;
import iot.social.google.gmail.GmailClient;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskExecutor implements Observer{
	
	private AtomicBoolean shutDown;

	private static TaskExecutor taskExecutor;
	
	private final ExecutorService currentTaskExecutor = Executors.newFixedThreadPool(5);
	private final ScheduledExecutorService scheduledTaskExecutor = Executors.newScheduledThreadPool(5);
	
	private final BlockingQueue<Runnable> currentTaskQueue = new LinkedBlockingQueue<Runnable>();
	private final BlockingQueue<Runnable> scheduledTaskQueue = new LinkedBlockingQueue<Runnable>();
	
	private final TaskProcessor taskProcessor;
	
	private TaskExecutor(){
		this.taskProcessor = new TaskProcessor();
		this.shutDown = new AtomicBoolean(true);
	}
	
	public static TaskExecutor getInstance(){
		if(null==taskExecutor){
			taskExecutor = new TaskExecutor();
		}
		return taskExecutor;
	}
	
	public void execute(){
		while (this.shutDown.get()) {
			Runnable currentTask = this.currentTaskQueue.poll();
			Runnable scheduledTask = this.scheduledTaskQueue.poll();
			if (null != scheduledTask) {
				this.scheduledTaskExecutor.scheduleAtFixedRate(scheduledTask, 1, 10, TimeUnit.SECONDS);
			}
			if (null != currentTask) {
				this.currentTaskExecutor.execute(currentTask);
			}
		}
		System.out.println("Shutting down the application .... ");
	}
	
	public void shutDown(){
		if(!this.currentTaskExecutor.isShutdown()){
			this.currentTaskExecutor.shutdown();
		}
		if(!this.scheduledTaskExecutor.isShutdown()){
			this.scheduledTaskExecutor.shutdown();
		}		
	}

	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof GmailClient){
			this.onMailReceived(arg);
		}
	}
	
	private void onMailReceived(final Object command){
		System.out.println("Executing Command :" + command);
		BaseTask task = this.taskProcessor.processCommand(command);
		if(null!=task && BaseTask.SCHEDULED_TASK.equals(task.getTaskType())){
			this.scheduledTaskQueue.add(task);
		}
		if(null!=task && BaseTask.IMMEDIATE_TASK.equals(task.getTaskType())){
			this.currentTaskQueue.add(task);
		}
	}
	
	public void addCurrentTask(final Runnable task){
		this.currentTaskQueue.add(task);
	}
	
	public void addScheduledTask(final Runnable task){
		this.scheduledTaskQueue.add(task);
	}
	
	public void setShutDown(AtomicBoolean shutDown) {
		this.shutDown = shutDown;
	}

	public AtomicBoolean getShutDown() {
		return shutDown;
	}
	
}
