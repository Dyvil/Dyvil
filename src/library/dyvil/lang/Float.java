package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.FloatConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

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
	
	@Override
	@Intrinsic({ LOAD_0, F2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2L })
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0 })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ LOAD_0 })
	public @prefix Float $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ LOAD_0, FNEG })
	public @prefix Float $minus()
	{
		return Float.apply(-this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public Float $plus(byte v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public Float $minus(byte v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public Float $times(byte v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public Float $div(byte v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public Float $percent(byte v)
	{
		return Float.apply(this.value % v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public Float $plus(short v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public Float $minus(short v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public Float $times(short v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public Float $div(short v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public Float $percent(short v)
	{
		return Float.apply(this.value % v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public Float $plus(char v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public Float $minus(char v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public Float $times(char v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public Float $div(char v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public Float $percent(char v)
	{
		return Float.apply(this.value % v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	public Float $plus(int v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	public Float $minus(int v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	public Float $times(int v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	public Float $div(int v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	public Float $percent(int v)
	{
		return Float.apply(this.value % v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FADD })
	public Float $plus(long v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FSUB })
	public Float $minus(long v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FMUL })
	public Float $times(long v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FDIV })
	public Float $div(long v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FREM })
	public Float $percent(long v)
	{
		return Float.apply(this.value % v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.floatValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.floatValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.floatValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.floatValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.floatValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.floatValue();
	}
	
	@Override
	public Float $plus(Number v)
	{
		return Float.apply(this.value + v.floatValue());
	}
	
	@Override
	public Float $minus(Number v)
	{
		return Float.apply(this.value - v.floatValue());
	}
	
	@Override
	public Float $times(Number v)
	{
		return Float.apply(this.value * v.floatValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return Float.apply(this.value / v.floatValue());
	}
	
	@Override
	public Float $percent(Number v)
	{
		return Float.apply(this.value % v.floatValue());
	}
	
	@Override
	public int compareTo(Number o)
	{
		return java.lang.Double.compare(this.value, o.doubleValue());
	}
	
	@Override
	public Float next()
	{
		return Float.apply(this.value + 1F);
	}
	
	@Override
	public Float previous()
	{
		return Float.apply(this.value - 1F);
	}
	
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
