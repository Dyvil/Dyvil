package dyvil.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A Thread implementation that processes a queue of tasks.
 * 
 * @author Clashsoft
 */
public class TaskThread extends Thread
{
	public static boolean		DEBUG		= true;
	
	private List<Runnable>			tasks		= new ArrayList();
	private ListIterator<Runnable>	iterator	= this.tasks.listIterator();
	
	public TaskThread(String name)
	{
		this.setName(name);
	}
	
	@Override
	public void run()
	{
		while (this.iterator.hasNext())
		{
			this.iterator.next().run();
			this.iterator.remove();
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
		this.iterator.add(task);
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
