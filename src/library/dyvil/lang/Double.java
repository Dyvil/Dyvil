package dyvil.lang;

import dyvil.lang.literal.DoubleConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@DoubleConvertible
public class Double implements Number
{
	public static final double	min					= java.lang.Double.MIN_VALUE;
	public static final double	max					= java.lang.Double.MAX_VALUE;
	public static final double	NaN					= java.lang.Double.NaN;
	public static final double	infinity			= java.lang.Double.POSITIVE_INFINITY;
	public static final double	negative_infinity	= java.lang.Double.NEGATIVE_INFINITY;
	public static final byte	size				= java.lang.Double.SIZE;
	
	protected double value;
	
	public static Double apply(double v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.DOUBLES[i];
		}
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
	
	@Override
	@Intrinsic({ INSTANCE, D2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, D2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, D2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, D2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, D2L })
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, D2F })
	public float floatValue()
	{
		return (float) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS })
	public @prefix Double $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DNEG })
	public @prefix Double $minus()
	{
		return Double.apply(-this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(byte v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(byte v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(byte v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(byte v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(byte v)
	{
		return Double.apply(this.value % v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(short v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(short v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(short v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(short v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(short v)
	{
		return Double.apply(this.value % v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(char v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(char v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(char v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(char v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(char v)
	{
		return Double.apply(this.value % v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(int v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(int v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(int v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(int v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(int v)
	{
		return Double.apply(this.value % v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DADD })
	public Double $plus(long v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DSUB })
	public Double $minus(long v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DMUL })
	public Double $times(long v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DREM })
	public Double $percent(long v)
	{
		return Double.apply(this.value % v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DADD })
	public Double $plus(float v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DSUB })
	public Double $minus(float v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DMUL })
	public Double $times(float v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DDIV })
	public Double $div(float v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DREM })
	public Double $percent(float v)
	{
		return Double.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.doubleValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.doubleValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.doubleValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.doubleValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.doubleValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.doubleValue();
	}
	
	@Override
	public Double $plus(Number v)
	{
		return Double.apply(this.value + v.doubleValue());
	}
	
	@Override
	public Double $minus(Number v)
	{
		return Double.apply(this.value - v.doubleValue());
	}
	
	@Override
	public Double $times(Number v)
	{
		return Double.apply(this.value * v.doubleValue());
	}
	
	@Override
	public Double $div(Number v)
	{
		return Double.apply(this.value / v.doubleValue());
	}
	
	@Override
	public Double $percent(Number v)
	{
		return Double.apply(this.value % v.doubleValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Double.toString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		long bits = java.lang.Double.doubleToLongBits(this.value);
		return (int) (bits ^ bits >>> 32);
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
