package dyvil.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public abstract class BasicPrintStream extends PrintStream
{
	public static PrintStream apply(OutputStream outputStream, PrintStream defaultNull)
	{
		if (outputStream == null)
		{
			return defaultNull;
		}
		if (outputStream instanceof PrintStream)
		{
			return (PrintStream) outputStream;
		}
		return new PrintStream(outputStream);
	}

	public BasicPrintStream(OutputStream out)
	{
		super(out);
	}

	protected abstract void write(String s);

	protected abstract void writeln(String s);

	protected abstract void write(char c);

	@Override
	public void print(boolean b)
	{
		this.write(b ? "true" : "false");
	}

	@Override
	public void print(char c)
	{
		this.write(c);
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
		for (char c : s)
		{
			this.write(c);
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
	public void println()
	{
		this.write('\n');
	}

	@Override
	public void println(boolean b)
	{
		this.write(b ? "true" : "false");
	}

	@Override
	public void println(char c)
	{
		this.write(c);
		this.write('\n');
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
		for (char c : s)
		{
			this.write(c);
		}
		this.write('\n');
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
		this.write(c);
		return this;
	}
}
