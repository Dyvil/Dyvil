package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.DoubleConvertible;

import dyvil.annotation.*;

import static dyvil.reflect.Opcodes.*;

import sun.misc.FloatingDecimal;

@DoubleConvertible
public class Double implements Number, Serializable
{
	private static final long serialVersionUID = 4764381743913068148L;
	
	public static final double	min					= java.lang.Double.MIN_VALUE;
	public static final double	max					= java.lang.Double.MAX_VALUE;
	public static final double	NaN					= java.lang.Double.NaN;
	public static final double	infinity			= java.lang.Double.POSITIVE_INFINITY;
	public static final double	negative_infinity	= java.lang.Double.NEGATIVE_INFINITY;
	public static final byte	size				= java.lang.Double.SIZE;
	
	protected double value;
	
	public static Double apply(double v)
	{
		return new Double(v);
	}
	
	public static @infix double unapply(Double v)
	{
		return v == null ? 0D : v.value;
	}
	
	protected Double(double value)
	{
		this.value = value;
	}
	
	// @formatter:off
	
	@Override
	public byte byteValue() { return (byte) this.value; }
	
	@Override
	public short shortValue() { return (short) this.value; }
	
	@Override
	public char charValue() { return (char) this.value; }
	
	@Override
	public int intValue() { return (int) this.value; }
	
	@Override
	public long longValue() { return (long) this.value; }
	
	@Override
	public float floatValue() { return (float) this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	public static @prefix double $plus(double v) { return v; }
	
	@Intrinsic({ LOAD_0, DNEG })
	public static @prefix double $minus(double v) { return -v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public static @infix boolean $lt(double v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public static @infix boolean $gt(double v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public static @infix double $plus(double v1, byte v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public static @infix double $minus(double v1, byte v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public static @infix double $times(double v1, byte v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public static @infix double $div(double v1, byte v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public static @infix double $percent(double v1, byte v2) { return (v1 % v2); }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public static @infix boolean $lt(double v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public static @infix boolean $gt(double v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public static @infix double $plus(double v1, short v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public static @infix double $minus(double v1, short v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public static @infix double $times(double v1, short v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public static @infix double $div(double v1, short v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public static @infix double $percent(double v1, short v2) { return (v1 % v2); }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public static @infix boolean $lt(double v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public static @infix boolean $gt(double v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public static @infix double $plus(double v1, char v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public static @infix double $minus(double v1, char v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public static @infix double $times(double v1, char v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public static @infix double $div(double v1, char v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public static @infix double $percent(double v1, char v2) { return (v1 % v2); }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLT })
	public static @infix boolean $lt(double v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGT })
	public static @infix boolean $gt(double v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DADD })
	public static @infix double $plus(double v1, int v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DSUB })
	public static @infix double $minus(double v1, int v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DMUL })
	public static @infix double $times(double v1, int v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DDIV })
	public static @infix double $div(double v1, int v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2D, DREM })
	public static @infix double $percent(double v1, int v2) { return (v1 % v2); }
	
	// long operators
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPLT })
	public static @infix boolean $lt(double v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPGT })
	public static @infix boolean $gt(double v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DADD })
	public static @infix double $plus(double v1, long v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DSUB })
	public static @infix double $minus(double v1, long v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DMUL })
	public static @infix double $times(double v1, long v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DDIV })
	public static @infix double $div(double v1, long v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2D, DREM })
	public static @infix double $percent(double v1, long v2) { return (v1 % v2); }
	
	// float operators
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPNE })
	public static @infix boolean $bang$eq(double v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPLT })
	public static @infix boolean $lt(double v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPLE })
	public static @infix boolean $lt$eq(double v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPGT })
	public static @infix boolean $gt(double v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DCMPGE })
	public static @infix boolean $gt$eq(double v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DADD })
	public static @infix double $plus(double v1, float v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DSUB })
	public static @infix double $minus(double v1, float v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DMUL })
	public static @infix double $times(double v1, float v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DDIV })
	public static @infix double $div(double v1, float v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, F2D, DREM })
	public static @infix double $percent(double v1, float v2) { return (v1 % v2); }
	
	// double operators
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPEQ })
	public static @infix boolean $eq$eq(double v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPNE })
	public static @infix boolean $bang$eq(double v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPLT })
	public static @infix boolean $lt(double v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPLE })
	public static @infix boolean $lt$eq(double v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPGT })
	public static @infix boolean $gt(double v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DCMPGE })
	public static @infix boolean $gt$eq(double v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, DADD })
	public static @infix double $plus(double v1, double v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, DSUB })
	public static @infix double $minus(double v1, double v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, DMUL })
	public static @infix double $times(double v1, double v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, DDIV })
	public static @infix double $div(double v1, double v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, DREM })
	public static @infix double $percent(double v1, double v2) { return (v1 % v2); }
	
	// generic operators
	
	@Override
	public @prefix Double $plus() { return this; }
	
	@Override
	public @prefix Double $minus() { return Double.apply(-this.value); }
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.doubleValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.doubleValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.doubleValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.doubleValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.doubleValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.doubleValue(); }
	
	@Override
	public Double $plus(Number v) { return Double.apply(this.value + v.doubleValue()); }
	
	@Override
	public Double $minus(Number v) { return Double.apply(this.value - v.doubleValue()); }
	
	@Override
	public Double $times(Number v) { return Double.apply(this.value * v.doubleValue()); }
	
	@Override
	public Double $div(Number v) { return Double.apply(this.value / v.doubleValue()); }
	
	@Override
	public Double $percent(Number v) { return Double.apply(this.value % v.doubleValue()); }
	
	@Override
	public int compareTo(Number o) { return java.lang.Double.compare(this.value, o.doubleValue()); }
	
	@Override
	public Double next() { return Double.apply(this.value + 1D); }
	
	@Override
	public Double previous() { return Double.apply(this.value - 1D); }
	
	// @formatter:on
	
	// Object methods
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Double", "toString", "(D)Ljava/lang/String;" })
	public static @infix @inline String toString(double value)
	{
		return FloatingDecimal.toJavaFormatString(value);
	}
	
	public static @infix @inline String toHexString(double value)
	{
		return java.lang.Double.toHexString(value);
	}
	
	@Override
	public String toString()
	{
		return FloatingDecimal.toJavaFormatString(this.value);
	}
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Double", "hashCode", "(D)I" })
	public static @postfix int $hash$hash(double d)
	{
		return java.lang.Double.hashCode(d);
	}
	
	@Override
	public int hashCode()
	{
		return java.lang.Double.hashCode(this.value);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Number))
		{
			return false;
		}
		Number other = (Number) obj;
		return this.value == other.doubleValue();
	}
}
