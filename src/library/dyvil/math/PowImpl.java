package dyvil.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PowImpl
{
	private PowImpl()
	{
		// No instances
	}

	public static long pow(long base, int exponent)
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
			return pow(base * base, exponent / 2);
		}
		else
		{
			return base * pow(base * base, exponent / 2);
		}
	}

	public static BigDecimal pow(BigDecimal base, double exponent)
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

	public static BigDecimal pow(BigDecimal base, BigDecimal exponent)
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
