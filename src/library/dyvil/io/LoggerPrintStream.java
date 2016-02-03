package dyvil.io;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link PrintStream} implementation that delegates calls to {@code print()} and {@code println()} methods to an
 * underlying {@link Logger} using the logger's {@link Logger#log(Level, String)} method. It is possible to specify a
 * custom level with a custom name, which will be used as the logging level of any log records produced by this print
 * stream.
 *
 * @author Clashsoft
 */
public class LoggerPrintStream extends BasicPrintStream
{
	private Level  level;
	private Logger logger;

	private StringBuilder buffer = new StringBuilder();
	
	public LoggerPrintStream(Logger logger, Level level)
	{
		super(System.out);
		this.logger = logger;
		this.level = level;
	}

	public LoggerPrintStream(Logger logger)
	{
		this(logger, Level.INFO);
	}

	public LoggerPrintStream(Logger logger, String name)
	{
		this(logger, new LoggerLevel(name, 1000));
	}
	
	@Override
	protected void writeln(String s)
	{
		this.logger.log(this.level, s);
	}

	@Override
	protected void write(String s)
	{
		this.buffer.append(s);
	}

	@Override
	protected void write(char c)
	{
		this.buffer.append(c);

		if (c == '\n')
		{
			this.writeln(this.buffer.toString());
			this.buffer.delete(0, this.buffer.length());
		}
	}
}
