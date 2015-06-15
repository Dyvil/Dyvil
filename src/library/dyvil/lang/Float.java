package dyvil.lang;

import dyvil.lang.literal.FloatConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@FloatConvertible
public class Float implements Number
{
	public static final float	min					= java.lang.Float.MIN_VALUE;
	public static final float	max					= java.lang.Float.MAX_VALUE;
	public static final float	NaN					= java.lang.Float.NaN;
	public static final float	infinity			= java.lang.Float.POSITIVE_INFINITY;
	public static final float	negative_infinity	= java.lang.Float.NEGATIVE_INFINITY;
	public static final byte	size				= java.lang.Float.SIZE;
	
	protected float				value;
	
	public static Float apply(float v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.FLOATS[i];
		}
		return new Float(v);
	}
	
	protected Float(float value)
	{
		this.value = value;
	}
	
	@Intrinsic({ INSTANCE })
	public float unapply()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2L })
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS })
	public @prefix Float $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FNEG })
	public @prefix Float $minus()
	{
		return Float.apply(-this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(byte v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(byte v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(byte v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(byte v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(byte v)
	{
		return Float.apply(this.value % v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(short v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(short v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(short v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(short v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(short v)
	{
		return Float.apply(this.value % v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(char v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(char v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(char v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(char v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(char v)
	{
		return Float.apply(this.value % v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(int v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(int v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(int v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(int v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(int v)
	{
		return Float.apply(this.value % v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FADD })
	public Float $plus(long v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FSUB })
	public Float $minus(long v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FMUL })
	public Float $times(long v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FDIV })
	public Float $div(long v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FREM })
	public Float $percent(long v)
	{
		return Float.apply(this.value % v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DREM })
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
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Float.toString(this.value);
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
