package dyvil.math;

class UnitUtils
{
	static final int			METER_MASK			= 0x0000000F;
	static final int			SECOND_MASK			= 0x000000F0;
	static final int			KILOGRAM_MASK		= 0x00000F00;
	static final int			MOL_MASK			= 0x0000F000;
	static final int			CANDELA_MASK		= 0x000F0000;
	static final int			AMPERE_MASK			= 0x00F00000;
	static final int			KELVIN_MASK			= 0x0F000000;
	
	static final int			METER_SHIFT			= 0;
	static final int			SECOND_SHIFT		= 4;
	static final int			KILOGRAM_SHIFT		= 8;
	static final int			MOL_SHIFT			= 12;
	static final int			CANDELA_SHIFT		= 16;
	static final int			AMPERE_SHIFT		= 20;
	static final int			KELVIN_SHIFT		= 24;
	
	private static final char[]	superscriptDigits	= { '\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078', '\u2079' };
	
	private static final char	superscriptMinus	= '\u207b';
	
	static void superscriptToString(StringBuilder buf, int i)
	{
		if (i == 1)
		{
			return;
		}
		// check if the number is negative
		if (i > 7)
		{
			buf.append(superscriptMinus);
			i &= 0x7;
			i++;
		}
		if (i >= 10)
		{
			buf.append(superscriptDigits[i / 10]);
		}
		buf.append(superscriptDigits[i % 10]);
	}
	
	static String toString(int unit)
	{
		int kg = (unit & KILOGRAM_MASK) >> KILOGRAM_SHIFT;
		int m = (unit & METER_MASK) >> METER_SHIFT;
		int s = (unit & SECOND_MASK) >> SECOND_SHIFT;
		int mol = (unit & MOL_MASK) >> MOL_SHIFT;
		int cd = (unit & CANDELA_MASK) >> CANDELA_SHIFT;
		int a = (unit & AMPERE_MASK) >> AMPERE_SHIFT;
		int k = (unit & KELVIN_MASK) >> KELVIN_SHIFT;
		StringBuilder buf = new StringBuilder();
		if (kg != 0)
		{
			buf.append("kg");
			superscriptToString(buf, kg);
		}
		if (m != 0)
		{
			buf.append('m');
			superscriptToString(buf, m);
		}
		if (s != 0)
		{
			buf.append('s');
			superscriptToString(buf, s);
		}
		if (mol != 0)
		{
			buf.append("mol");
			superscriptToString(buf, mol);
		}
		if (cd != 0)
		{
			buf.append("cd");
			superscriptToString(buf, cd);
		}
		if (a != 0)
		{
			buf.append('A');
			superscriptToString(buf, a);
		}
		if (k != 0)
		{
			buf.append('K');
			superscriptToString(buf, k);
		}
		return buf.toString();
	}
}
