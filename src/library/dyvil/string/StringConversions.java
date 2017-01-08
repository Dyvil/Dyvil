package dyvil.string;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.reflect.Modifiers;

/**
 * The <b>StringConversions</b> class can be used for several String-conversions such as
 * parsing {@code boolean}s, {@code int}s or {@code float}s or converting integers to roman numeral representations.
 */
public final class StringConversions
{
	private static final String[] ROMANDIGIT  = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV",
		"I" };
	private static final int[]    ROMANNUMBER = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };

	private static final String[] ROMANCACHE = { "0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
		"XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX" };

	private StringConversions()
	{
		// no instances
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static boolean toBoolean(@Nullable String s)
	{
		return s != null && s.equalsIgnoreCase("true");
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte toByte(@NonNull String s)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static byte toByte(@NonNull String s, byte _default)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static byte toByte(@NonNull String s, byte _default, int radix)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static short toShort(@NonNull String s)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static short toShort(@NonNull String s, short _default)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static short toShort(@NonNull String s, short _default, int radix)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static int toInt(@NonNull String s)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static int toInt(@NonNull String s, int _default)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static int toInt(@NonNull String s, int _default, int radix)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static long toLong(@NonNull String s)
	{
		try
		{
			return Long.parseLong(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long toLong(@NonNull String s, long _default)
	{
		try
		{
			return Long.parseLong(s, 10);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long toLong(@NonNull String s, long _default, int radix)
	{
		try
		{
			return Long.parseLong(s, radix);
		}
		catch (NumberFormatException ex)
		{
			return _default;
		}
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float toFloat(@NonNull String s)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static float toFloat(@NonNull String s, float _default)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static double toDouble(@NonNull String s)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static double toDouble(@NonNull String s, double _default)
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

	@DyvilModifiers(Modifiers.INFIX)
	public static String toRomanString(int number)
	{
		if (number < 0)
		{
			return "-" + toRomanString(-number);
		}
		else if (number < 20)
		{
			return ROMANCACHE[number];
		}
		else if (number >= 4000)
		{
			throw new NumberFormatException("Invalid Roman Conversion: Value outside Roman numeral range: " + number
				                                + " >= 4000");
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < 13; i++)
		{
			while (number >= ROMANNUMBER[i])
			{
				number -= ROMANNUMBER[i];
				builder.append(ROMANDIGIT[i]);
			}
		}
		return builder.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toRomanString(long number)
	{
		if (number < 0L)
		{
			return "-" + toRomanString(-number);
		}
		else if (number < 20L)
		{
			return ROMANCACHE[(int) number];
		}
		else if (number >= 4000L)
		{
			throw new NumberFormatException("Invalid Roman Conversion: Value outside Roman numeral range: " + number
				                                + " >= 4000");
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < 13; i++)
		{
			while (number >= ROMANNUMBER[i])
			{
				number -= ROMANNUMBER[i];
				builder.append(ROMANDIGIT[i]);
			}
		}
		return builder.toString();
	}
}
