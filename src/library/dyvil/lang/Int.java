package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.prefix;

public class Int implements Integer
{
	protected int	value;
	
	protected Int(int value)
	{
		this.value = value;
	}
	
	public static Int apply(int v)
	{
		if (v >= 0 && v < ConstPool.tableSize)
		{
			return ConstPool.INTS[v];
		}
		return new Int(v);
	}
	
	@Intrinsic({ INSTANCE })
	public int unapply()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L })
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS })
	public @prefix Int $plus()
	{
		return Int.apply(this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, INEG })
	public @prefix Int $minus()
	{
		return Int.apply((byte) -this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ICONST_M1, IXOR })
	public @prefix Int $tilde()
	{
		return Int.apply((byte) ~this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(byte v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(byte v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(byte v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, I2F, FDIV })
	public Float $div(byte v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(byte v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $bslash(byte v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(byte v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(byte v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(byte v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $lt$lt(byte v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $gt$gt(byte v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $gt$gt$gt(byte v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(short v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(short v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(short v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, I2F, FDIV })
	public Float $div(short v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(short v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $bslash(short v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(short v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(short v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(short v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $lt$lt(short v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $gt$gt(short v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $gt$gt$gt(short v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(char v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(char v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(char v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, I2F, FDIV })
	public Float $div(char v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(char v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $bslash(char v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(char v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(char v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(char v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $lt$lt(char v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $gt$gt(char v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $gt$gt$gt(char v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(int v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(int v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(int v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, I2F, FDIV })
	public Float $div(int v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(int v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $bslash(int v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(int v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(int v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(int v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $lt$lt(int v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $gt$gt(int v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $gt$gt$gt(int v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPEQ })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LADD })
	public Long $plus(long v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LSUB })
	public Long $minus(long v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LMUL })
	public Long $times(long v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LREM })
	public Long $percent(long v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LDIV })
	public Long $bslash(long v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LAND })
	public Long $amp(long v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LOR })
	public Long $bar(long v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LXOR })
	public Long $up(long v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LSHL })
	public Int $lt$lt(long v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LSHR })
	public Int $gt$gt(long v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LUSHR })
	public Int $gt$gt$gt(long v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPEQ })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPEQ })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.intValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.intValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.intValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.intValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.intValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.intValue();
	}
	
	@Override
	public Int $plus(Number v)
	{
		return Int.apply(this.value + v.intValue());
	}
	
	@Override
	public Int $minus(Number v)
	{
		return Int.apply(this.value - v.intValue());
	}
	
	@Override
	public Int $times(Number v)
	{
		return Int.apply(this.value * v.intValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return Float.apply(this.value / v.floatValue());
	}
	
	@Override
	public Int $percent(Number v)
	{
		return Int.apply(this.value % v.intValue());
	}
	
	@Override
	public Integer $bslash(Integer v)
	{
		return Int.apply(this.value / v.intValue());
	}
	
	@Override
	public Int $bar(Integer v)
	{
		return Int.apply(this.value | v.intValue());
	}
	
	@Override
	public Int $amp(Integer v)
	{
		return Int.apply(this.value & v.intValue());
	}
	
	@Override
	public Int $up(Integer v)
	{
		return Int.apply(this.value ^ v.intValue());
	}
	
	@Override
	public Int $lt$lt(Integer v)
	{
		return Int.apply(this.value << v.intValue());
	}
	
	@Override
	public Int $gt$gt(Integer v)
	{
		return Int.apply(this.value >> v.intValue());
	}
	
	@Override
	public Int $gt$gt$gt(Integer v)
	{
		return Int.apply(this.value >>> v.intValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Integer.toString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return this.value;
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
		return this.value == other.intValue();
	}
}
