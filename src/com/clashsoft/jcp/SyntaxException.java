package com.clashsoft.jcp;

import java.io.PrintStream;
import java.util.Arrays;

public class SyntaxException extends Exception
{
	private static final long	serialVersionUID	= -2234451954260010124L;

	public SyntaxException()
	{
		super();
	}

	public SyntaxException(String message)
	{
		super(message);
	}
	
	public SyntaxException(String message, Object... args)
	{
		this(message);
	}

	public SyntaxException(Throwable cause)
	{
		super(cause);
	}
	
	public void print(PrintStream out, String code, Token token)
	{
		out.println("Syntax error at token " + token);
		
		int prevNL = code.lastIndexOf('\n', token.start) + 1;
		int nextNL = code.indexOf('\n', token.end);
		
		if (prevNL < 0)
		{
			prevNL = 0;
		}
		if (nextNL < 0)
		{
			nextNL = code.length();
		}
		
		String line = code.substring(prevNL, nextNL);
		
		out.println(line);
		
		char[] chars = new char[token.start];
		Arrays.fill(chars, ' ');
		out.print(chars);
		out.print('^');
	}
}
