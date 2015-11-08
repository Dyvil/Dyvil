package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.FloatConvertible;

import dyvil.annotation.*;

import static dyvil.reflect.Opcodes.*;

import sun.misc.FloatingDecimal;

@FloatConvertible
public class Float implements Number, Serializable
{
	private static final long serialVersionUID = 2128649158072690759L;
	
	public static final float	min					= java.lang.Float.MIN_VALUE;
	public static final float	max					= java.lang.Float.MAX_VALUE;
	public static final float	NaN					= java.lang.Float.NaN;
	public static final float	infinity			= java.lang.Float.POSITIVE_INFINITY;
	public static final float	negative_infinity	= java.lang.Float.NEGATIVE_INFINITY;
	public static final byte	size				= java.lang.Float.SIZE;
	
	protected float value;
	
	public static Float apply(float v)
	{
		return new Float(v);
	}
	
	public static @infix float unapply(Float v)
	{
		return v == null ? 0F : v.value;
	}
	
	protected Float(float value)
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
	public float floatValue() { return this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	public static @prefix float $plus(float v) { return v; }
	
	@Intrinsic({ LOAD_0, FNEG })
	public static @prefix float $minus(float v) { return -v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public static @infix boolean $bang$eq(float v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public static @infix boolean $lt(float v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public static @infix boolean $lt$eq(float v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public static @infix boolean $gt(float v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public static @infix boolean $gt$eq(float v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public static @infix float $plus(float v1, byte v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public static @infix float $minus(float v1, byte v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public static @infix float $times(float v1, byte v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public static @infix float $div(float v1, byte v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public static @infix float $percent(float v1, byte v2) { return (v1 % v2); }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public static @infix boolean $bang$eq(float v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public static @infix boolean $lt(float v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public static @infix boolean $lt$eq(float v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public static @infix boolean $gt(float v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public static @infix boolean $gt$eq(float v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public static @infix float $plus(float v1, short v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public static @infix float $minus(float v1, short v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public static @infix float $times(float v1, short v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public static @infix float $div(float v1, short v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public static @infix float $percent(float v1, short v2) { return (v1 % v2); }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public static @infix boolean $bang$eq(float v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public static @infix boolean $lt(float v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public static @infix boolean $lt$eq(float v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public static @infix boolean $gt(float v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public static @infix boolean $gt$eq(float v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public static @infix float $plus(float v1, char v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public static @infix float $minus(float v1, char v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public static @infix float $times(float v1, char v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public static @infix float $div(float v1, char v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public static @infix float $percent(float v1, char v2) { return (v1 % v2); }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public static @infix boolean $bang$eq(float v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public static @infix boolean $lt(float v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public static @infix boolean $lt$eq(float v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public static @infix boolean $gt(float v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public static @infix boolean $gt$eq(float v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public static @infix float $plus(float v1, int v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public static @infix float $minus(float v1, int v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public static @infix float $times(float v1, int v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public static @infix float $div(float v1, int v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public static @infix float $percent(float v1, int v2) { return (v1 % v2); }
	
	// long operators
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPNE })
	public static @infix boolean $bang$eq(float v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLT })
	public static @infix boolean $lt(float v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLE })
	public static @infix boolean $lt$eq(float v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGT })
	public static @infix boolean $gt(float v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGE })
	public static @infix boolean $gt$eq(float v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FADD })
	public static @infix float $plus(float v1, long v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FSUB })
	public static @infix float $minus(float v1, long v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FMUL })
	public static @infix float $times(float v1, long v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FDIV })
	public static @infix float $div(float v1, long v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FREM })
	public static @infix float $percent(float v1, long v2) { return (v1 % v2); }
	
	// float operators
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPEQ })
	public static @infix boolean $eq$eq(float v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPNE })
	public static @infix boolean $bang$eq(float v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLT })
	public static @infix boolean $lt(float v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLE })
	public static @infix boolean $lt$eq(float v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGT })
	public static @infix boolean $gt(float v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGE })
	public static @infix boolean $gt$eq(float v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FADD })
	public static @infix float $plus(float v1, float v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FSUB })
	public static @infix float $minus(float v1, float v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FMUL })
	public static @infix float $times(float v1, float v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FDIV })
	public static @infix float $div(float v1, float v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FREM })
	public static @infix float $percent(float v1, float v2) { return (v1 % v2); }
	
	// double operators
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPEQ })
	public static @infix boolean $eq$eq(float v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPNE })
	public static @infix boolean $bang$eq(float v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLT })
	public static @infix boolean $lt(float v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLE })
	public static @infix boolean $lt$eq(float v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGT })
	public static @infix boolean $gt(float v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGE })
	public static @infix boolean $gt$eq(float v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DADD })
	public static @infix double $plus(float v1, double v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DSUB })
	public static @infix double $minus(float v1, double v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DMUL })
	public static @infix double $times(float v1, double v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DDIV })
	public static @infix double $div(float v1, double v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DREM })
	public static @infix double $percent(float v1, double v2) { return (v1 % v2); }
	
	// generic operators
	
	@Override
	public @prefix Float $plus() { return this; }
	
	@Override
	public @prefix Float $minus() { return Float.apply(-this.value); }
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.floatValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.floatValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.floatValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.floatValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.floatValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.floatValue(); }
	
	@Override
	public Float $plus(Number v) { return Float.apply(this.value + v.floatValue()); }
	
	@Override
	public Float $minus(Number v) { return Float.apply(this.value - v.floatValue()); }
	
	@Override
	public Float $times(Number v) { return Float.apply(this.value * v.floatValue()); }
	
	@Override
	public Float $div(Number v) { return Float.apply(this.value / v.floatValue()); }
	
	@Override
	public Float $percent(Number v) { return Float.apply(this.value % v.floatValue()); }
	
	@Override
	public int compareTo(Number o) { return java.lang.Double.compare(this.value, o.doubleValue()); }
	
	@Override
	public Float next() { return Float.apply(this.value + 1F); }
	
	@Override
	public Float previous() { return Float.apply(this.value - 1F); }
	
	// @formatter:on
	
	// Object methods
	
	public static @infix @inline String toString(float value)
	{
		return FloatingDecimal.toJavaFormatString(value);
	}
	
	public static @infix @inline String toHexString(float value)
	{
		return java.lang.Double.toHexString(value);
	}
	
	@Override
	public String toString()
	{
		return FloatingDecimal.toJavaFormatString(this.value);
	}
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Float", "hashCode", "(F)I" })
	public static @postfix int $hash$hash(float f)
	{
		return java.lang.Float.floatToIntBits(f);
	}
	
	@Override
	public int hashCode()
	{
		return java.lang.Float.floatToIntBits(this.value);
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
		return this.value == other.floatValue();
	}
}
