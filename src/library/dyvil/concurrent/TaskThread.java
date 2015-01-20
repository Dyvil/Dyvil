package dyvil.concurrent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A Thread implementation that processes a queue of tasks.
 * 
 * @author Clashsoft
 */
public class TaskThread extends Thread
{
	public static boolean			DEBUG		= true;
	
	private Queue<Runnable>			tasks		= new LinkedList();
	
	public TaskThread(String name)
	{
		this.setName(name);
	}
	
	@Override
	public void run()
	{
		while (!this.tasks.isEmpty())
		{
			Runnable r = this.tasks.remove();
			r.run();
		}
	}
	
	/**
	 * Adds a task to this thread. The task is added to the end of the queue,
	 * and thus it becomes the last task to be processed.
	 * 
	 * @param task
	 *            the task.
	 */
	public void addTask(Runnable task)
	{
		this.tasks.add(task);
	}
	
	/**
	 * Returns the amount of tasks this {@link TaskThread} thread has to process
	 * before finishing.
	 * 
	 * @return the amount of tasks
	 */
	public int getTaskCount()
	{
		return this.tasks.size();
	}
}
