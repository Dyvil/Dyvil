package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;

public class Long implements Integer
{
	protected long	value;
	
	protected Long(long value)
	{
		this.value = value;
	}
	
	public static Long create(long value)
	{
		if (value >= 0 && value < ConstPool.tableSize)
		{
			return ConstPool.LONGS[(int) value];
		}
		return new Long(value);
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
	@Intrinsic({ INSTANCE })
	public Long $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ INSTANCE, LNEG })
	public Long $minus()
	{
		return Long.create(-this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, LCONST_M1, LXOR })
	public Long $tilde()
	{
		return Long.create(~this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, DUP2, LMUL })
	public Long sqr()
	{
		return Long.create(this.value * this.value);
	}
	
	@Override
	@Intrinsic({ LCONST_1, INSTANCE, LDIV })
	public Long rec()
	{
		return Long.create(1L / this.value);
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
	public boolean $less(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $less$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $greater(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $greater$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(byte v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(byte v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(byte v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $div(byte v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(byte v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(byte v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(byte v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(byte v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $less$less(byte v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $greater$greater(byte v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $greater$greater$greater(byte v)
	{
		return Long.create(this.value >>> v);
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
	public boolean $less(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $less$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $greater(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $greater$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(short v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(short v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(short v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $div(short v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(short v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(short v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(short v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(short v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $less$less(short v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $greater$greater(short v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $greater$greater$greater(short v)
	{
		return Long.create(this.value >>> v);
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
	public boolean $less(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $less$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $greater(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $greater$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(char v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(char v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(char v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $div(char v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(char v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(char v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(char v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(char v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $less$less(char v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $greater$greater(char v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $greater$greater$greater(char v)
	{
		return Long.create(this.value >>> v);
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
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLE })
	public boolean $less(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPLT })
	public boolean $less$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGT })
	public boolean $greater(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, IF_LCMPGE })
	public boolean $greater$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LADD })
	public Long $plus(int v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSUB })
	public Long $minus(int v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LMUL })
	public Long $times(int v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LDIV })
	public Long $div(int v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LREM })
	public Long $percent(int v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LAND })
	public Long $amp(int v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LOR })
	public Long $bar(int v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LXOR })
	public Long $up(int v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHL })
	public Long $less$less(int v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LSHR })
	public Long $greater$greater(int v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, I2L, LUSHR })
	public Long $greater$greater$greater(int v)
	{
		return Long.create(this.value >>> v);
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
	public boolean $less(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPLE })
	public boolean $less$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPGT })
	public boolean $greater(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_LCMPGE })
	public boolean $greater$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LADD })
	public Long $plus(long v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSUB })
	public Long $minus(long v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LMUL })
	public Long $times(long v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LDIV })
	public Long $div(long v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LREM })
	public Long $percent(long v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LAND })
	public Long $amp(long v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LOR })
	public Long $bar(long v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LXOR })
	public Long $up(long v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSHL })
	public Long $less$less(long v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LSHR })
	public Long $greater$greater(long v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, LUSHR })
	public Long $greater$greater$greater(long v)
	{
		return Long.create(this.value >>> v);
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
	public boolean $less(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPLE })
	public boolean $less$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPGT })
	public boolean $greater(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, IF_FCMPGE })
	public boolean $greater$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2F, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
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
	public boolean $less(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPLE })
	public boolean $less$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPGT })
	public boolean $greater(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, IF_DCMPGE })
	public boolean $greater$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, L2D, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
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
	public boolean $less(Number v)
	{
		return this.value < v.longValue();
	}
	
	@Override
	public boolean $less$eq(Number v)
	{
		return this.value <= v.longValue();
	}
	
	@Override
	public boolean $greater(Number v)
	{
		return this.value > v.longValue();
	}
	
	@Override
	public boolean $greater$eq(Number v)
	{
		return this.value >= v.longValue();
	}
	
	@Override
	public Long $plus(Number v)
	{
		return Long.create(this.value + v.longValue());
	}
	
	@Override
	public Long $minus(Number v)
	{
		return Long.create(this.value - v.longValue());
	}
	
	@Override
	public Long $times(Number v)
	{
		return Long.create(this.value * v.longValue());
	}
	
	@Override
	public Long $div(Number v)
	{
		return Long.create(this.value / v.longValue());
	}
	
	@Override
	public Long $percent(Number v)
	{
		return Long.create(this.value % v.longValue());
	}
	
	@Override
	public Long $bar(Integer v)
	{
		return Long.create(this.value | v.longValue());
	}
	
	@Override
	public Long $amp(Integer v)
	{
		return Long.create(this.value & v.longValue());
	}
	
	@Override
	public Long $up(Integer v)
	{
		return Long.create(this.value ^ v.longValue());
	}
	
	@Override
	public Long $less$less(Integer v)
	{
		return Long.create(this.value << v.longValue());
	}
	
	@Override
	public Long $greater$greater(Integer v)
	{
		return Long.create(this.value >> v.longValue());
	}
	
	@Override
	public Long $greater$greater$greater(Integer v)
	{
		return Long.create(this.value >>> v.longValue());
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
