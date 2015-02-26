package dyvil.strings;

import dyvil.lang.annotation.infix;

public interface StringConversions
{
	public static final String[]	ROMANDIGIT	= { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
	public static final int[]		ROMANNUMBER	= { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	
	public static final String[]	ROMANCACHE	= {
			"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX" };
	
	public static @infix boolean toBoolean(String s)
	{
		return s != null && s.equalsIgnoreCase("true");
	}
	
	public static @infix byte toByte(String s)
	{
		try
		{
			return Byte.parseByte(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public static @infix byte toByte(String s, byte _default)
	{
		try
		{
			return Byte.parseByte(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix byte toByte(String s, byte _default, int radix)
	{
		try
		{
			return Byte.parseByte(s, radix);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix short toShort(String s)
	{
		try
		{
			return Short.parseShort(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public static @infix short toShort(String s, short _default)
	{
		try
		{
			return Short.parseShort(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix short toShort(String s, short _default, int radix)
	{
		try
		{
			return Short.parseShort(s, radix);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix int toInt(String s)
	{
		try
		{
			return Integer.parseInt(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public static @infix int toInt(String s, int _default)
	{
		try
		{
			return Integer.parseInt(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix int toInt(String s, int _default, int radix)
	{
		try
		{
			return Integer.parseInt(s, radix);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix float toFloat(String s)
	{
		try
		{
			return Float.parseFloat(s);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public static @infix float toFloat(String s, float _default)
	{
		try
		{
			return Float.parseFloat(s);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix double toDouble(String s)
	{
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public static @infix double toDouble(String s, double _default)
	{
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}
	
	public static @infix String toRomanString(int number)
	{
		if (number < 0)
		{
			return "-" + toRomanString(-number);
		}
		else if (number < ROMANCACHE.length)
		{
			return ROMANCACHE[number];
		}
		else if (number >= 4000)
		{
			throw new NumberFormatException("Exception while converting to Roman: Value outside roman numeral range.");
		}
		
		StringBuilder roman = new StringBuilder();
		
		for (int i = 0; i < ROMANDIGIT.length; i++)
		{
			while (number >= ROMANNUMBER[i])
			{
				number -= ROMANNUMBER[i];
				roman.append(ROMANDIGIT[i]);
			}
		}
		return roman.toString();
	}
	
	public static @infix String toRomanString(long number)
	{
		if (number < 0L)
		{
			return "-" + toRomanString(-number);
		}
		else if (number < ROMANCACHE.length)
		{
			return ROMANCACHE[(int) number];
		}
		else if (number >= 4000L)
		{
			throw new NumberFormatException("Exception while converting to Roman: Value outside roman numeral range.");
		}
		
		StringBuilder roman = new StringBuilder();
		
		for (int i = 0; i < ROMANDIGIT.length; i++)
		{
			while (number >= ROMANNUMBER[i])
			{
				number -= ROMANNUMBER[i];
				roman.append(ROMANDIGIT[i]);
			}
		}
		return roman.toString();
	}
}
