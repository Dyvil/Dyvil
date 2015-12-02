package dyvil.io;

import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link PrintStream} implementation that delegates calls to {@code print()}
 * and {@code println()} methods to an underlying {@link Logger} using the
 * loggers {@link Logger#log(Level, String)} method. It is possible to specify a
 * custom level with a custom name, which will be used as the logging level of
 * any log records produced by output stream.
 *
 * @author Clashsoft
 * @version 1.0
 */
public class LoggerOutputStream extends PrintStream
{
	private Level  level;
	private Logger logger;
	
	public LoggerOutputStream(Logger logger)
	{
		super(System.out, false);
		this.logger = logger;
		this.level = Level.INFO;
	}
	
	public LoggerOutputStream(Logger logger, Level level)
	{
		super(System.out, false);
		this.logger = logger;
		this.level = level;
	}
	
	public LoggerOutputStream(Logger logger, String name)
	{
		super(System.out, false);
		this.logger = logger;
		
		this.level = new Level(name, 1000)
		{
			private static final long serialVersionUID = -8829261068161314749L;
		};
	}
	
	private void write(String s)
	{
		this.logger.log(this.level, s);
	}
	
	@Override
	public void print(boolean b)
	{
		this.write(b ? "true" : "false");
	}
	
	@Override
	public void print(char c)
	{
		this.write(String.valueOf(c));
	}
	
	@Override
	public void print(int i)
	{
		this.write(String.valueOf(i));
	}
	
	@Override
	public void print(long l)
	{
		this.write(String.valueOf(l));
	}
	
	@Override
	public void print(float f)
	{
		this.write(String.valueOf(f));
	}
	
	@Override
	public void print(double d)
	{
		this.write(String.valueOf(d));
	}
	
	@Override
	public void print(char s[])
	{
		this.write(new String(s));
	}
	
	@Override
	public void print(String s)
	{
		if (s == null)
		{
			s = "null";
		}
		this.write(s);
	}
	
	@Override
	public void print(Object obj)
	{
		this.write(String.valueOf(obj));
	}
	
	@Override
	public void println()
	{
		this.logger.log(this.level, "");
	}
	
	@Override
	public void println(boolean b)
	{
		this.write(b ? "true" : "false");
	}
	
	@Override
	public void println(char c)
	{
		this.write(String.valueOf(c));
	}
	
	@Override
	public void println(int i)
	{
		this.write(String.valueOf(i));
	}
	
	@Override
	public void println(long l)
	{
		this.write(String.valueOf(l));
	}
	
	@Override
	public void println(float f)
	{
		this.write(String.valueOf(f));
	}
	
	@Override
	public void println(double d)
	{
		this.write(String.valueOf(d));
	}
	
	@Override
	public void println(char s[])
	{
		this.write(new String(s));
	}
	
	@Override
	public void println(String s)
	{
		if (s == null)
		{
			s = "null";
		}
		this.write(s);
	}
	
	@Override
	public void println(Object obj)
	{
		this.write(String.valueOf(obj));
	}
	
	@Override
	public PrintStream printf(String format, Object... args)
	{
		this.write(String.format(format, args));
		return this;
	}
	
	@Override
	public PrintStream printf(Locale l, String format, Object... args)
	{
		this.write(String.format(l, format, args));
		return this;
	}
	
	@Override
	public PrintStream format(String format, Object... args)
	{
		this.write(String.format(format, args));
		return this;
	}
	
	@Override
	public PrintStream format(Locale l, String format, Object... args)
	{
		String.format(l, format, args);
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence csq)
	{
		if (csq == null)
		{
			this.write("null");
		}
		else
		{
			this.write(csq.toString());
		}
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence csq, int start, int end)
	{
		CharSequence cs = csq == null ? "null" : csq;
		this.write(cs.subSequence(start, end).toString());
		return this;
	}
	
	@Override
	public PrintStream append(char c)
	{
		this.print(c);
		return this;
	}
}
