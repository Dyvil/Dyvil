package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;

public class Double implements Number
{
	protected double	value;
	
	protected Double(double value)
	{
		this.value = value;
	}
	
	public static Double create(double v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.DOUBLES[i];
		}
		return new Double(v);
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
	@Intrinsic({ INSTANCE })
	public Double $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ INSTANCE, DNEG })
	public Double $minus()
	{
		return Double.create(-this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, DUP2, DMUL })
	public Double sqr()
	{
		return Double.create(this.value * this.value);
	}
	
	@Override
	@Intrinsic({ DCONST_1, INSTANCE, DDIV })
	public Double rec()
	{
		return Double.create(1 / this.value);
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
	public boolean $less(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $less$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $greater(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $greater$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(byte v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(byte v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(byte v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(byte v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(byte v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $less$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $greater(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $greater$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(short v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(short v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(short v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(short v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(short v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $less$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $greater(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $greater$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(char v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(char v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(char v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(char v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(char v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPLE })
	public boolean $less$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGT })
	public boolean $greater(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, IF_DCMPGE })
	public boolean $greater$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DADD })
	public Double $plus(int v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DSUB })
	public Double $minus(int v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DMUL })
	public Double $times(int v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DDIV })
	public Double $div(int v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2D, DREM })
	public Double $percent(int v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPLE })
	public boolean $less$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPGT })
	public boolean $greater(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, IF_DCMPGE })
	public boolean $greater$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DADD })
	public Double $plus(long v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DSUB })
	public Double $minus(long v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DMUL })
	public Double $times(long v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2D, DREM })
	public Double $percent(long v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPLE })
	public boolean $less$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPGT })
	public boolean $greater(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, IF_DCMPGE })
	public boolean $greater$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DADD })
	public Double $plus(float v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DSUB })
	public Double $minus(float v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DMUL })
	public Double $times(float v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DDIV })
	public Double $div(float v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, F2D, DREM })
	public Double $percent(float v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPLE })
	public boolean $less$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPGT })
	public boolean $greater(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_DCMPGE })
	public boolean $greater$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(Number v)
	{
		return this.value < v.doubleValue();
	}
	
	@Override
	public boolean $less$eq(Number v)
	{
		return this.value <= v.doubleValue();
	}
	
	@Override
	public boolean $greater(Number v)
	{
		return this.value > v.doubleValue();
	}
	
	@Override
	public boolean $greater$eq(Number v)
	{
		return this.value >= v.doubleValue();
	}
	
	@Override
	public Double $plus(Number v)
	{
		return Double.create(this.value + v.doubleValue());
	}
	
	@Override
	public Double $minus(Number v)
	{
		return Double.create(this.value - v.doubleValue());
	}
	
	@Override
	public Double $times(Number v)
	{
		return Double.create(this.value * v.doubleValue());
	}
	
	@Override
	public Double $div(Number v)
	{
		return Double.create(this.value / v.doubleValue());
	}
	
	@Override
	public Double $percent(Number v)
	{
		return Double.create(this.value % v.doubleValue());
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
