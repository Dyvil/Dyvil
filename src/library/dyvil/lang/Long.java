package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.annotation.Intrinsic;
import dyvil.annotation.prefix;
import dyvil.lang.literal.LongConvertible;

@LongConvertible
public class Long implements Integer
{
	protected long	value;
	
	public static Long apply(long value)
	{
		if (value >= 0 && value < ConstPool.tableSize)
		{
			return ConstPool.LONGS[(int) value];
		}
		return new Long(value);
	}
	
	protected Long(long value)
	{
		this.value = value;
	}
	
	@Intrinsic({ INSTANCE })
	public long unapply()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS })
	public @prefix Long $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LNEG })
	public @prefix Long $minus()
	{
		return Long.apply(-this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LCONST_M1, LXOR })
	public @prefix Long $tilde()
	{
		return Long.apply(~this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(byte v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(byte v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(byte v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, I2D, DDIV })
	public Double $div(byte v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(byte v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $bslash(byte v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(byte v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(byte v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(byte v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $lt$lt(byte v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $gt$gt(byte v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $gt$gt$gt(byte v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(short v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(short v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(short v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, I2D, DDIV })
	public Double $div(short v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(short v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $bslash(short v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(short v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(short v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(short v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $lt$lt(short v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $gt$gt(short v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $gt$gt$gt(short v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(char v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(char v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(char v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, I2D, DDIV })
	public Double $div(char v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(char v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $bslash(char v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(char v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(char v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(char v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $lt$lt(char v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $gt$gt(char v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $gt$gt$gt(char v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(int v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(int v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(int v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, I2D, DDIV })
	public Double $div(int v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(int v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $bslash(int v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(int v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(int v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(int v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $lt$lt(int v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $gt$gt(int v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $gt$gt$gt(int v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LADD })
	public Long $plus(long v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSUB })
	public Long $minus(long v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LMUL })
	public Long $times(long v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LREM })
	public Long $percent(long v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LDIV })
	public Long $bslash(long v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LAND })
	public Long $amp(long v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LOR })
	public Long $bar(long v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LXOR })
	public Long $up(long v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSHL })
	public Long $lt$lt(long v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSHR })
	public Long $gt$gt(long v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LUSHR })
	public Long $gt$gt$gt(long v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.longValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.longValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.longValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.longValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.longValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.longValue();
	}
	
	@Override
	public Long $plus(Number v)
	{
		return Long.apply(this.value + v.longValue());
	}
	
	@Override
	public Long $minus(Number v)
	{
		return Long.apply(this.value - v.longValue());
	}
	
	@Override
	public Long $times(Number v)
	{
		return Long.apply(this.value * v.longValue());
	}
	
	@Override
	public Double $div(Number v)
	{
		return Double.apply(this.value / v.doubleValue());
	}
	
	@Override
	public Long $percent(Number v)
	{
		return Long.apply(this.value % v.longValue());
	}
	
	@Override
	public Long $bslash(Integer v)
	{
		return Long.apply(this.value / v.longValue());
	}
	
	@Override
	public Long $bar(Integer v)
	{
		return Long.apply(this.value | v.longValue());
	}
	
	@Override
	public Long $amp(Integer v)
	{
		return Long.apply(this.value & v.longValue());
	}
	
	@Override
	public Long $up(Integer v)
	{
		return Long.apply(this.value ^ v.longValue());
	}
	
	@Override
	public Long $lt$lt(Integer v)
	{
		return Long.apply(this.value << v.longValue());
	}
	
	@Override
	public Long $gt$gt(Integer v)
	{
		return Long.apply(this.value >> v.longValue());
	}
	
	@Override
	public Long $gt$gt$gt(Integer v)
	{
		return Long.apply(this.value >>> v.longValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Long.toString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return (int) (this.value >>> 32 ^ this.value);
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
		return this.value == other.longValue();
	}
}
