package dyvil.io;

import java.io.IOException;
import java.io.PrintStream;

/**
 * A {@link PrintStream} implementation that delegates calls to {@code print()}
 * and {@code println()} methods to an underlying {@link Appendable}.
 *
 * @author Clashsoft
 * @version 1.0
 */
public class AppendablePrintStream extends BasicPrintStream
{
	private Appendable appendable;
	
	public AppendablePrintStream(Appendable appendable)
	{
		super(System.out);
		this.appendable = appendable;
	}
	
	@Override
	protected void write(String s)
	{
		try
		{
			this.appendable.append(s);
		}
		catch (IOException ignored)
		{
		}
	}
	
	@Override
	protected void writeln(String s)
	{
		try
		{
			this.appendable.append(s).append('\n');
		}
		catch (IOException ignored)
		{
		}
	}
	
	@Override
	protected void write(char c)
	{
		try
		{
			this.appendable.append(c);
		}
		catch (IOException ignored) {
		}
	}
}
