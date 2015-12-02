package dyvil.io;

import java.io.*;
import java.util.function.BooleanSupplier;

public final class Console
{
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	private static PrintStream    out    = System.out;
	
	private Console()
	{

	}
	
	public static void setOut(PrintStream out)
	{
		Console.out = out;
	}
	
	public static void setIn(InputStream in)
	{
		Console.reader = new BufferedReader(new InputStreamReader(in));
	}
	
	// Write
	
	public static void writeLine()
	{
		out.println();
	}
	
	public static void writeLine(Object o)
	{
		out.println(o);
	}
	
	public static void writeLine(String s)
	{
		out.println(s);
	}
	
	// Read
	
	public static String readLine()
	{
		try
		{
			return reader.readLine();
		}
		catch (IOException ex)
		{
			return null;
		}
	}
	
	public static String read(int count)
	{
		char[] chars = new char[count];
		try
		{
			reader.read(chars);
		}
		catch (IOException ex)
		{
			return null;
		}
		return new String(chars);
	}
	
	public static String read(BooleanSupplier predicate)
	{
		try
		{
			StringBuilder buf = new StringBuilder();
			while (predicate.getAsBoolean())
			{
				buf.appendCodePoint(reader.read());
			}
			return buf.toString();
		}
		catch (IOException ex)
		{
			return null;
		}
	}
	
	public static byte readByte()
	{
		return (byte) java.lang.Integer.parseInt(readLine());
	}
	
	public static short readShort()
	{
		return (short) java.lang.Integer.parseInt(readLine());
	}
	
	public static char readChar()
	{
		try
		{
			return (char) reader.read();
		}
		catch (IOException ex)
		{
			return 0;
		}
	}
	
	public static int readCodePoint()
	{
		try
		{
			return reader.read();
		}
		catch (IOException ex)
		{
			return 0;
		}
	}
	
	public static int readInt()
	{
		return java.lang.Integer.parseInt(readLine());
	}
	
	public static long readLong()
	{
		return java.lang.Long.parseLong(readLine());
	}
	
	public static float readFloat()
	{
		return java.lang.Float.parseFloat(readLine());
	}
	
	public static double readDouble()
	{
		return java.lang.Double.parseDouble(readLine());
	}
}
