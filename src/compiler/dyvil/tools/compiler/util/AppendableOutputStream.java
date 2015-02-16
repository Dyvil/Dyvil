package dyvil.tools.compiler.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public class AppendableOutputStream extends PrintStream
{
	private Appendable	appendable;
	
	public AppendableOutputStream(Appendable appendable)
	{
		super(System.out, false);
		this.appendable = appendable;
	}
	
	private void write(String s)
	{
		try
		{
			this.appendable.append(s);
		}
		catch (IOException ex)
		{
		}
	}
	
	private void writeln(String s)
	{
		try
		{
			this.appendable.append(s).append('\n');
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public void print(boolean b)
	{
		this.write(b ? "true" : "false");
	}
	
	@Override
	public void print(char c)
	{
		try
		{
			this.appendable.append(c);
		}
		catch (IOException ex)
		{
		}
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
	public void print(char[] s)
	{
		try
		{
			for (char c : s)
			{
				this.appendable.append(c);
			}
		}
		catch (IOException ex)
		{
		}
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
	public void println(boolean b)
	{
		this.write(b ? "true" : "false");
	}
	
	@Override
	public void println(char c)
	{
		try
		{
			this.appendable.append(c).append('\n');
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void println(int i)
	{
		this.writeln(String.valueOf(i));
	}
	
	@Override
	public void println(long l)
	{
		this.writeln(String.valueOf(l));
	}
	
	@Override
	public void println(float f)
	{
		this.writeln(String.valueOf(f));
	}
	
	@Override
	public void println(double d)
	{
		this.writeln(String.valueOf(d));
	}
	
	@Override
	public void println(char[] s)
	{
		try
		{
			for (char c : s)
			{
				this.appendable.append(c);
			}
			this.appendable.append('\n');
		}
		catch (IOException ex)
		{
		}
	}
	
	@Override
	public void println(String s)
	{
		if (s == null)
		{
			s = "null";
		}
		this.writeln(s);
	}
	
	@Override
	public void println(Object obj)
	{
		this.writeln(String.valueOf(obj));
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
		try
		{
			this.appendable.append(c);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}
}
