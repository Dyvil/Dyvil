package dyvil.concurrent;

public class TaskThreadManager
{
	public final String		name;
	public final int		paralledThreads;
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
	 * @param parallelThreads
	 *            the amount of available processors
	 */
	public TaskThreadManager(String name, int parallelThreads)
	{
		this.name = name;
		this.paralledThreads = parallelThreads;
		this.threads = new TaskThread[parallelThreads];
		
		for (int i = 0; i < parallelThreads; i++)
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
		int cores = this.paralledThreads;
		int min = Integer.MAX_VALUE;
		TaskThread thread = null;
		
		for (int i = 0; i < cores; i++)
		{
			TaskThread t = threads[i];
			int taskCount = t.getTaskCount();
			if (taskCount < min)
			{
				min = taskCount;
				thread = t;
			}
		}
		
		return thread;
	}
}
