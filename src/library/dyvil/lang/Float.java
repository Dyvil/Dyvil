package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.prefix;

public class Float implements Number
{
	protected float	value;
	
	protected Float(float value)
	{
		this.value = value;
	}
	
	public static Float create(float v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.FLOATS[i];
		}
		return new Float(v);
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
		return Float.create(-this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, DUP, FMUL })
	public Float sqr()
	{
		return Float.create(this.value * this.value);
	}
	
	@Override
	@Intrinsic({ FCONST_1, INSTANCE, FDIV })
	public Float rec()
	{
		return Float.create(1 / this.value);
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
	public boolean $less(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $less$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $greater(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $greater$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(byte v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(byte v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(byte v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(byte v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(byte v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $less$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $greater(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $greater$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(short v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(short v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(short v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(short v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(short v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $less$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $greater(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $greater$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(char v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(char v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(char v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(char v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(char v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPLE })
	public boolean $less$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGT })
	public boolean $greater(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, IF_FCMPGE })
	public boolean $greater$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FADD })
	public Float $plus(int v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FSUB })
	public Float $minus(int v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FMUL })
	public Float $times(int v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FDIV })
	public Float $div(int v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2F, FREM })
	public Float $percent(int v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPLE })
	public boolean $less$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPGT })
	public boolean $greater(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, IF_FCMPGE })
	public boolean $greater$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FADD })
	public Float $plus(long v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FSUB })
	public Float $minus(long v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FMUL })
	public Float $times(long v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FDIV })
	public Float $div(long v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2F, FREM })
	public Float $percent(long v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPLE })
	public boolean $less$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPGT })
	public boolean $greater(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_FCMPGE })
	public boolean $greater$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPLE })
	public boolean $less$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPGT })
	public boolean $greater(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, IF_DCMPGE })
	public boolean $greater$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, F2D, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(Number v)
	{
		return this.value < v.floatValue();
	}
	
	@Override
	public boolean $less$eq(Number v)
	{
		return this.value <= v.floatValue();
	}
	
	@Override
	public boolean $greater(Number v)
	{
		return this.value > v.floatValue();
	}
	
	@Override
	public boolean $greater$eq(Number v)
	{
		return this.value >= v.floatValue();
	}
	
	@Override
	public Float $plus(Number v)
	{
		return Float.create(this.value + v.floatValue());
	}
	
	@Override
	public Float $minus(Number v)
	{
		return Float.create(this.value - v.floatValue());
	}
	
	@Override
	public Float $times(Number v)
	{
		return Float.create(this.value * v.floatValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return Float.create(this.value / v.floatValue());
	}
	
	@Override
	public Float $percent(Number v)
	{
		return Float.create(this.value % v.floatValue());
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
