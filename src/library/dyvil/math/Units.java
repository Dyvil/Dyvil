package dyvil.math;

import static dyvil.math.UnitUtils.*;

public final class Units
{
	public static final int		METER		= 0x0000001;
	public static final int		SECOND		= 0x0000010;
	public static final int		KILOGRAM	= 0x0000100;
	public static final int		MOL			= 0x0001000;
	public static final int		CANDELA		= 0x0010000;
	public static final int		AMPERE		= 0x0100000;
	public static final int		KELVIN		= 0x1000000;
	
	private static final int	M1			= 0x8;
	private static final int	M2			= 0x9;
	private static final int	M3			= 0xA;
	
	public static final int		NEWTON		= METER + KILOGRAM + SECOND * M2;
	public static final int		HERTZ		= SECOND * M1;
	public static final int		PASCAL		= KILOGRAM + METER * M1 + SECOND * M2;
	public static final int		JOULE		= KILOGRAM + METER * 2 + SECOND * M2;
	public static final int		WATT		= KILOGRAM + METER * 2 + SECOND * M3;
	public static final int		COULOMB		= SECOND + AMPERE;
	public static final int		VOLT		= KILOGRAM + METER * 2 + SECOND * M3 + AMPERE * M1;
	public static final int		FARAD		= KILOGRAM * M1 + METER * M1 + SECOND * 4 + AMPERE * 2;
	public static final int		OHM			= KILOGRAM + METER * 2 + SECOND * M3 + AMPERE * M2;
	public static final int		SIEMENS		= KILOGRAM * M1 + METER * M2 + SECOND * 3 + AMPERE * 2;
	public static final int		WEBER		= KILOGRAM + METER * 2 + SECOND * M2 + AMPERE * M1;
	public static final int		TESLA		= KILOGRAM + SECOND * M2 + AMPERE;
	public static final int		HENRY		= KILOGRAM + METER * 2 + SECOND * M2 + AMPERE * M2;
	public static final int		LUX			= METER * M2 + CANDELA;
	public static final int		SIEVERT		= METER * 2 + SECOND * M2;
	public static final int		KATAL		= SECOND * M1 + MOL;
	
	public static String toString(int unit)
	{
		switch (unit)
		{
		case METER:
			return "m";
		case SECOND:
			return "s";
		case KILOGRAM:
			return "kg";
		case MOL:
			return "mol";
		case CANDELA:
			return "cd";
		case AMPERE:
			return "A";
		case KELVIN:
			return "K";
		case NEWTON:
			return "N";
		case HERTZ:
			return "Hz";
		case PASCAL:
			return "Pa";
		case JOULE:
			return "J";
		case WATT:
			return "W";
		case COULOMB:
			return "C";
		case VOLT:
			return "V";
		case FARAD:
			return "F";
		case OHM:
			return "\u2126";
		case SIEMENS:
			return "S";
		case WEBER:
			return "Wb";
		case TESLA:
			return "T";
		case HENRY:
			return "H";
		case LUX:
			return "lx";
		case SIEVERT:
			return "Sv";
		case KATAL:
			return "kat";
		}
		
		return UnitUtils.toString(unit);
	}
	
	public static int $minus(int unit)
	{
		int m = (-(unit >> METER_SHIFT) & METER_MASK) << METER_SHIFT;
		int s = (-(unit >> SECOND_SHIFT) & SECOND_MASK) << SECOND_SHIFT;
		int kg = (-(unit >> KILOGRAM_SHIFT) & KILOGRAM_MASK) << KILOGRAM_SHIFT;
		int mol = (-(unit >> MOL_SHIFT) & MOL_MASK) << MOL_SHIFT;
		int cd = (-(unit >> CANDELA_SHIFT) & CANDELA_MASK) << CANDELA_SHIFT;
		int a = (-(unit >> AMPERE_SHIFT) & AMPERE_MASK) << AMPERE_SHIFT;
		int k = (-(unit >> KELVIN_SHIFT) & KELVIN_MASK) << KELVIN_SHIFT;
		return m | s | kg | mol | cd | a | k;
	}
	
	public static int $times(int unit1, int unit2)
	{
		int m = (unit1 >> METER_SHIFT + unit2 >> METER_SHIFT & METER_MASK) << METER_SHIFT;
		int s = (unit1 >> SECOND_SHIFT + unit2 >> SECOND_SHIFT & SECOND_MASK) << SECOND_SHIFT;
		int kg = (unit1 >> KILOGRAM_SHIFT + unit2 >> KILOGRAM_SHIFT & KILOGRAM_MASK) << KILOGRAM_SHIFT;
		int mol = (unit1 >> MOL_SHIFT + unit2 >> MOL_SHIFT & MOL_MASK) << MOL_SHIFT;
		int cd = (unit1 >> CANDELA_SHIFT + unit2 >> CANDELA_SHIFT & CANDELA_MASK) << CANDELA_SHIFT;
		int a = (unit1 >> AMPERE_SHIFT + unit2 >> AMPERE_SHIFT & AMPERE_MASK) << AMPERE_SHIFT;
		int k = (unit1 >> KELVIN_SHIFT + unit2 >> KELVIN_SHIFT & KELVIN_MASK) << KELVIN_SHIFT;
		return m | s | kg | mol | cd | a | k;
	}
	
	public static int $div(int unit1, int unit2)
	{
		int m = (unit1 >> METER_SHIFT - unit2 >> METER_SHIFT & METER_MASK) << METER_SHIFT;
		int s = (unit1 >> SECOND_SHIFT - unit2 >> SECOND_SHIFT & SECOND_MASK) << SECOND_SHIFT;
		int kg = (unit1 >> KILOGRAM_SHIFT - unit2 >> KILOGRAM_SHIFT & KILOGRAM_MASK) << KILOGRAM_SHIFT;
		int mol = (unit1 >> MOL_SHIFT - unit2 >> MOL_SHIFT & MOL_MASK) << MOL_SHIFT;
		int cd = (unit1 >> CANDELA_SHIFT - unit2 >> CANDELA_SHIFT & CANDELA_MASK) << CANDELA_SHIFT;
		int a = (unit1 >> AMPERE_SHIFT - unit2 >> AMPERE_SHIFT & AMPERE_MASK) << AMPERE_SHIFT;
		int k = (unit1 >> KELVIN_SHIFT - unit2 >> KELVIN_SHIFT & KELVIN_MASK) << KELVIN_SHIFT;
		return m | s | kg | mol | cd | a | k;
	}
}
