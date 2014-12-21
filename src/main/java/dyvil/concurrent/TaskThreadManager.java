package dyvil.concurrent;

public class TaskThreadManager
{
	public final String		name;
	public final int		cores;
	private TaskThread[]	threads;
	
	/**
	 * Creates a new {@link TaskThreadManager} with the current amount of
	 * available processors.
	 * 
	 * @param name
	 *            the name
	 */
	public TaskThreadManager(String name)
	{
		this(name, Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Creates a new {@link TaskThreadManager} with the given amount of
	 * available processors. For each available processor, it creates an
	 * instance of {@link TaskThread}.
	 * 
	 * @param name
	 *            the name
	 * @param cores
	 *            the amount of available processors
	 */
	public TaskThreadManager(String name, int cores)
	{
		this.name = name;
		this.cores = cores;
		this.threads = new TaskThread[cores];
		
		for (int i = 0; i < cores; i++)
		{
			this.threads[i] = new TaskThread(name + "-" + i);
		}
	}
	
	/**
	 * Adds a task to the least occupied thread.
	 * 
	 * @param task
	 *            the task
	 */
	public void addTask(Runnable task)
	{
		TaskThread thread = this.getLeastOccupiedThread();
		thread.addTask(task);
	}
	
	/**
	 * Returns the thread that currently has the least amount of tasks in it's
	 * queue.
	 * 
	 * @return the least occupied thread
	 */
	public TaskThread getLeastOccupiedThread()
	{
		TaskThread[] threads = this.threads;
		int cores = this.cores;
		int min = Integer.MAX_VALUE;
		int thread = 0;
		
		for (int i = 0; i < cores; i++)
		{
			TaskThread t = threads[i];
			int taskCount = t.getTaskCount();
			if (taskCount < min)
			{
				min = taskCount;
				thread = i;
			}
		}
		
		return threads[thread];
	}
}
