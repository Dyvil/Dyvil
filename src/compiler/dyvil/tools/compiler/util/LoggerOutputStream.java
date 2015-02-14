package dyvil.tools.compiler.util;

import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerOutputStream extends PrintStream
{
	private Level	level;
	private Logger	logger;
	
	public LoggerOutputStream(Logger logger, String name)
	{
		super(System.out, false);
		this.logger = logger;
		
		this.level = new Level(name, 1000)
		{
			private static final long	serialVersionUID	= -8829261068161314749L;
		};
	}
	
	private void write(String s)
	{
		this.logger.log(this.level, s);
	}
	
	@Override
	public void print(boolean b)
	{
		write(b ? "true" : "false");
	}
	
	@Override
	public void print(char c)
	{
		write(String.valueOf(c));
	}
	
	@Override
	public void print(int i)
	{
		write(String.valueOf(i));
	}
	
	@Override
	public void print(long l)
	{
		write(String.valueOf(l));
	}
	
	@Override
	public void print(float f)
	{
		write(String.valueOf(f));
	}
	
	@Override
	public void print(double d)
	{
		write(String.valueOf(d));
	}
	
	@Override
	public void print(char s[])
	{
		write(new String(s));
	}
	
	@Override
	public void print(String s)
	{
		if (s == null)
		{
			s = "null";
		}
		write(s);
	}
	
	@Override
	public void print(Object obj)
	{
		write(String.valueOf(obj));
	}
	
	@Override
	public void println(boolean b)
	{
		write(b ? "true" : "false");
	}
	
	@Override
	public void println(char c)
	{
		write(String.valueOf(c));
	}
	
	@Override
	public void println(int i)
	{
		write(String.valueOf(i));
	}
	
	@Override
	public void println(long l)
	{
		write(String.valueOf(l));
	}
	
	@Override
	public void println(float f)
	{
		write(String.valueOf(f));
	}
	
	@Override
	public void println(double d)
	{
		write(String.valueOf(d));
	}
	
	@Override
	public void println(char s[])
	{
		write(new String(s));
	}
	
	@Override
	public void println(String s)
	{
		if (s == null)
		{
			s = "null";
		}
		write(s);
	}
	
	@Override
	public void println(Object obj)
	{
		write(String.valueOf(obj));
	}
	
	@Override
	public PrintStream printf(String format, Object... args)
	{
		write(String.format(format, args));
		return this;
	}
	
	@Override
	public PrintStream printf(Locale l, String format, Object... args)
	{
		write(String.format(l, format, args));
		return this;
	}
	
	@Override
	public PrintStream format(String format, Object... args)
	{
		write(String.format(format, args));
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
			write("null");
		else
			write(csq.toString());
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence csq, int start, int end)
	{
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}
	
	@Override
	public PrintStream append(char c)
	{
		print(c);
		return this;
	}
}
