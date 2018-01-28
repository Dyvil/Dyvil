package dyvil.math;

import dyvil.annotation.internal.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PowImpl
{
	private static final byte[] HIGHEST_SET_BIT = { 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
		6, 6, 6 };

	private PowImpl()
	{
		// No instances
	}

	public static long pow(long base, int exponent)
	{
		if (base == 0)
		{
			if (exponent <= 0)
			{
				throw new ArithmeticException("0 ** " + exponent);
			}
			return 0;
		}
		if (base == 1)
		{
			return 1;
		}
		if (base == -1)
		{
			return 1 - ((exponent & 1) << 1); // 1 if even, -1 if odd
		}
		if (exponent < 0)
		{
			return 0; // always rounded down to 0
		}

		if (exponent > 63)
		{
			// this will overflow anyway, but we at least want the "correct" overflow value
			return powRec(base, exponent);
		}

		long result = 1;
		switch (HIGHEST_SET_BIT[exponent])
		{
		case 6:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			exponent >>= 1;
			base *= base;
			// Fallthrough
		case 5:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			exponent >>= 1;
			base *= base;
			// Fallthrough
		case 4:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			exponent >>= 1;
			base *= base;
			// Fallthrough
		case 3:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			exponent >>= 1;
			base *= base;
			// Fallthrough
		case 2:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			exponent >>= 1;
			base *= base;
			// Fallthrough
		case 1:
			if ((exponent & 1) != 0)
			{
				result *= base;
			}
			// Fallthrough
		default:
			return result;
		}
	}

	private static long powRec(long base, int exponent) /* where |base| > 1, exponent >= 0 */
	{
		switch (exponent)
		{
		case 0:
			return 1;
		case 1:
			return base;
		}
		// Use recursive pow definition, with time complexity O(log n)
		return ((exponent & 1) == 0 ? 1 : base) * powRec(base * base, exponent >> 1);
	}

	// Implementation note:
	// Both BigDecimal pow algorithms calculate base^(A+B) as base^A * base^B,
	// where A is the integer part of the exponent, and B is the decimal part.

	public static BigDecimal pow(@NonNull BigDecimal base, double exponent)
	{
		final boolean negativeExponent;

		if (exponent < 0)
		{
			negativeExponent = true;
			// Ensure exponent is positive
			exponent = -exponent;
		}
		else
		{
			negativeExponent = false;
		}

		final double exponentRemainder = exponent % 1;
		// approximate as int
		final int exponentInt = (int) (exponent - exponentRemainder);

		final BigDecimal intPow = base.pow(exponentInt);
		final BigDecimal doublePow = new BigDecimal(Math.pow(base.doubleValue(), exponentRemainder));
		final BigDecimal result = intPow.multiply(doublePow);

		// Fix negative power
		if (negativeExponent)
		{
			return BigDecimal.ONE.divide(result, RoundingMode.HALF_EVEN);
		}
		return result;
	}

	public static BigDecimal pow(@NonNull BigDecimal base, @NonNull BigDecimal exponent)
	{
		final int exponentSign = exponent.signum();
		if (exponentSign < 0)
		{
			// Ensure exponent is positive
			exponent = exponent.negate();
		}

		final BigDecimal exponentRemainder = exponent.remainder(BigDecimal.ONE);
		final BigDecimal exponentInt = exponent.subtract(exponentRemainder);
		final BigDecimal intPow = base.pow(exponentInt.intValueExact());
		final BigDecimal doublePow = new BigDecimal(Math.pow(base.doubleValue(), exponentRemainder.doubleValue()));
		final BigDecimal result = intPow.multiply(doublePow);

		// Fix negative power
		if (exponentSign < 0)
		{
			return BigDecimal.ONE.divide(result, RoundingMode.HALF_EVEN);
		}
		return result;
	}
}
