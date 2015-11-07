package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.LongConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@LongConvertible
public class Long implements Integer, Serializable
{
	private static final long serialVersionUID = 4495480142241309185L;
	
	public static final long	min		= java.lang.Long.MIN_VALUE;
	public static final long	max		= java.lang.Long.MAX_VALUE;
	public static final byte	size	= java.lang.Long.SIZE;
	
	protected long value;
	
	private static final class ConstantPool
	{
		protected static final int	TABLE_MIN	= -128;
		protected static final int	TABLE_SIZE	= 256;
		protected static final int	TABLE_MAX	= TABLE_MIN + TABLE_SIZE;
		
		protected static final Long[] TABLE = new Long[TABLE_SIZE];
		
		static
		{
			for (int i = 0; i < TABLE_SIZE; i++)
			{
				TABLE[i] = new Long(i + TABLE_MIN);
			}
		}
	}
	
	public static Long apply(long v)
	{
		if (v >= ConstantPool.TABLE_MIN && v < ConstantPool.TABLE_MAX)
		{
			return ConstantPool.TABLE[(int) (v - ConstantPool.TABLE_MIN)];
		}
		return new Long(v);
	}
	
	public static @infix long unapply(Long v)
	{
		return v == null ? 0L : v.value;
	}
	
	protected Long(long value)
	{
		this.value = value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2I })
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0 })
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ LOAD_0 })
	public @prefix Long $plus()
	{
		return this;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LNEG })
	public @prefix Long $minus()
	{
		return Long.apply(-this.value);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LCONST_M1, LXOR })
	public @prefix Long $tilde()
	{
		return Long.apply(~this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public Long $plus(byte v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public Long $minus(byte v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public Long $times(byte v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public Double $div(byte v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public Long $percent(byte v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public Long $bslash(byte v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public Long $amp(byte v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public Long $bar(byte v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public Long $up(byte v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public Long $lt$lt(byte v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public Long $gt$gt(byte v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public Long $gt$gt$gt(byte v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public Long $plus(short v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public Long $minus(short v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public Long $times(short v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public Double $div(short v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public Long $percent(short v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public Long $bslash(short v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public Long $amp(short v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public Long $bar(short v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public Long $up(short v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public Long $lt$lt(short v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public Long $gt$gt(short v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public Long $gt$gt$gt(short v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public Long $plus(char v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public Long $minus(char v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public Long $times(char v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public Double $div(char v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public Long $percent(char v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public Long $bslash(char v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public Long $amp(char v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public Long $bar(char v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public Long $up(char v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public Long $lt$lt(char v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public Long $gt$gt(char v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public Long $gt$gt$gt(char v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public Long $plus(int v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public Long $minus(int v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public Long $times(int v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public Double $div(int v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public Long $percent(int v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public Long $bslash(int v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public Long $amp(int v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public Long $bar(int v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public Long $up(int v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LSHL })
	public Long $lt$lt(int v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LSHR })
	public Long $gt$gt(int v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LUSHR })
	public Long $gt$gt$gt(int v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LADD })
	public Long $plus(long v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LSUB })
	public Long $minus(long v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LMUL })
	public Long $times(long v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LREM })
	public Long $percent(long v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LDIV })
	public Long $bslash(long v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LAND })
	public Long $amp(long v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LOR })
	public Long $bar(long v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, LXOR })
	public Long $up(long v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHL })
	public Long $lt$lt(long v)
	{
		return Long.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHR })
	public Long $gt$gt(long v)
	{
		return Long.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LUSHR })
	public Long $gt$gt$gt(long v)
	{
		return Long.apply(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DREM })
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
	
	@Override
	public int compareTo(Number o)
	{
		return java.lang.Long.compare(this.value, o.longValue());
	}
	
	@Override
	public Long next()
	{
		return Long.apply(this.value + 1L);
	}
	
	@Override
	public Long previous()
	{
		return Long.apply(this.value - 1L);
	}
	
	// Object methods
	
	public static @infix @inline String toString(long value)
	{
		return java.lang.Long.toString(value);
	}
	
	public static @infix @inline String toBinaryString(long value)
	{
		return java.lang.Long.toBinaryString(value);
	}
	
	public static @infix @inline String toHexString(long value)
	{
		return java.lang.Long.toHexString(value);
	}
	
	public static @infix @inline String toOctalString(long value)
	{
		return java.lang.Long.toOctalString(value);
	}
	
	public static @infix String toString(long value, int radix)
	{
		switch (radix)
		{
		case 2:
			return java.lang.Long.toBinaryString(value);
		case 8:
			return java.lang.Long.toOctalString(value);
		case 10:
			return java.lang.Long.toString(value);
		case 16:
			return java.lang.Long.toHexString(value);
		}
		return java.lang.Long.toString(value, radix);
	}
	
	@Override
	public String toString()
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
