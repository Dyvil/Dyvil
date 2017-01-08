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
		if (exponent > 63)
		{
			if (base == 1)
			{
				return 1;
			}

			if (base == -1)
			{
				// -1 if exponent is even, 1 if it's odd
				return 1 - 2 * (exponent & 1);
			}

			return 0;
		}

		int result = 1;
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

	public static long pow_rec(long base, int exponent)
	{
		switch (exponent)
		{
		case 0:
			return 1;
		case 1:
			return base;
		}
		if (exponent < 0) // always rounded down to 0, except when base=1
		{
			return base == 1 ? 1 : 0;
		}

		// Use recursive pow definition, with time complexity O(log n)
		if ((exponent & 1) == 0) // the exponent is even
		{
			return pow_rec(base * base, exponent / 2);
		}
		else
		{
			return base * pow_rec(base * base, exponent / 2);
		}
	}

	public static BigDecimal pow(@NonNull BigDecimal base, double exponent)
	{
		// Performs X^(A+B)=X^A*X^B (B = remainder)

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
		final int exponentInt = (int) (exponent - exponentRemainder);

		final BigDecimal intPow = base.pow(exponentInt);
		final BigDecimal doublePow = new BigDecimal(Math.pow(base.doubleValue(), exponentRemainder));
		final BigDecimal result = intPow.multiply(doublePow);

		// Fix negative power
		if (negativeExponent)
		{
			return BigDecimal.ONE.divide(result, RoundingMode.HALF_UP);
		}
		return result;
	}

	public static BigDecimal pow(@NonNull BigDecimal base, @NonNull BigDecimal exponent)
	{
		// Performs X^(A+B)=X^A*X^B (B = remainder)

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
			return BigDecimal.ONE.divide(result, RoundingMode.HALF_UP);
		}
		return result;
	}
}
