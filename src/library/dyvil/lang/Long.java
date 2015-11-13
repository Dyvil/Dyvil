package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.LongConvertible;

import dyvil.annotation.*;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.annotation._internal.postfix;
import dyvil.annotation._internal.prefix;

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
	public long longValue() { return this.value; }
	
	@Override
	public float floatValue() { return this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	public static @prefix long $plus(long v) { return v; }
	
	@Intrinsic({ LOAD_0, LNEG })
	public static @prefix long $minus(long v) { return -v; }
	
	@Intrinsic({ LOAD_0, LCONST_M1, LXOR })
	public static @prefix long $tilde(long v) { return ~v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public static @infix boolean $eq$eq(long v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public static @infix boolean $bang$eq(long v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public static @infix boolean $lt(long v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public static @infix boolean $lt$eq(long v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public static @infix boolean $gt(long v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public static @infix boolean $gt$eq(long v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public static @infix long $plus(long v1, byte v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public static @infix long $minus(long v1, byte v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public static @infix long $times(long v1, byte v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public static @infix double $div(long v1, byte v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public static @infix long $percent(long v1, byte v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public static @infix long $bslash(long v1, byte v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public static @infix long $amp(long v1, byte v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public static @infix long $bar(long v1, byte v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public static @infix long $up(long v1, byte v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public static @infix long $lt$lt(long v1, byte v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public static @infix long $gt$gt(long v1, byte v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public static @infix long $gt$gt$gt(long v1, byte v2) { return v1 >>> v2; }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public static @infix boolean $eq$eq(long v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public static @infix boolean $bang$eq(long v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public static @infix boolean $lt(long v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public static @infix boolean $lt$eq(long v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public static @infix boolean $gt(long v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public static @infix boolean $gt$eq(long v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public static @infix long $plus(long v1, short v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public static @infix long $minus(long v1, short v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public static @infix long $times(long v1, short v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public static @infix double $div(long v1, short v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public static @infix long $percent(long v1, short v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public static @infix long $bslash(long v1, short v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public static @infix long $amp(long v1, short v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public static @infix long $bar(long v1, short v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public static @infix long $up(long v1, short v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public static @infix long $lt$lt(long v1, short v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public static @infix long $gt$gt(long v1, short v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public static @infix long $gt$gt$gt(long v1, short v2) { return v1 >>> v2; }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public static @infix boolean $eq$eq(long v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public static @infix boolean $bang$eq(long v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public static @infix boolean $lt(long v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public static @infix boolean $lt$eq(long v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public static @infix boolean $gt(long v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public static @infix boolean $gt$eq(long v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public static @infix long $plus(long v1, char v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public static @infix long $minus(long v1, char v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public static @infix long $times(long v1, char v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public static @infix double $div(long v1, char v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public static @infix long $percent(long v1, char v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public static @infix long $bslash(long v1, char v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public static @infix long $amp(long v1, char v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public static @infix long $bar(long v1, char v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public static @infix long $up(long v1, char v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	public static @infix long $lt$lt(long v1, char v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	public static @infix long $gt$gt(long v1, char v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	public static @infix long $gt$gt$gt(long v1, char v2) { return v1 >>> v2; }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	public static @infix boolean $eq$eq(long v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	public static @infix boolean $bang$eq(long v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	public static @infix boolean $lt(long v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	public static @infix boolean $lt$eq(long v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	public static @infix boolean $gt(long v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	public static @infix boolean $gt$eq(long v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	public static @infix long $plus(long v1, int v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	public static @infix long $minus(long v1, int v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	public static @infix long $times(long v1, int v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	public static @infix double $div(long v1, int v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	public static @infix long $percent(long v1, int v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	public static @infix long $bslash(long v1, int v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	public static @infix long $amp(long v1, int v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	public static @infix long $bar(long v1, int v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	public static @infix long $up(long v1, int v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSHL })
	public static @infix long $lt$lt(long v1, int v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSHR })
	public static @infix long $gt$gt(long v1, int v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LUSHR })
	public static @infix long $gt$gt$gt(long v1, int v2) { return v1 >>> v2; }
	
	// long operators
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPEQ })
	public static @infix boolean $eq$eq(long v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPNE })
	public static @infix boolean $bang$eq(long v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLT })
	public static @infix boolean $lt(long v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLE })
	public static @infix boolean $lt$eq(long v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGT })
	public static @infix boolean $gt(long v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGE })
	public static @infix boolean $gt$eq(long v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LADD })
	public static @infix long $plus(long v1, long v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSUB })
	public static @infix long $minus(long v1, long v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LMUL })
	public static @infix long $times(long v1, long v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, L2D, DDIV })
	public static @infix double $div(long v1, long v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LREM })
	public static @infix long $percent(long v1, long v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LDIV })
	public static @infix long $bslash(long v1, long v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LAND })
	public static @infix long $amp(long v1, long v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LOR })
	public static @infix long $bar(long v1, long v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LXOR })
	public static @infix long $up(long v1, long v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHL })
	public static @infix long $lt$lt(long v1, long v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHR })
	public static @infix long $gt$gt(long v1, long v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LUSHR })
	public static @infix long $gt$gt$gt(long v1, long v2) { return v1 >>> v2; }
	
	// float operators
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPEQ })
	public static @infix boolean $eq$eq(long v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPNE })
	public static @infix boolean $bang$eq(long v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLT })
	public static @infix boolean $lt(long v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLE })
	public static @infix boolean $lt$eq(long v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGT })
	public static @infix boolean $gt(long v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGE })
	public static @infix boolean $gt$eq(long v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FADD })
	public static @infix float $plus(long v1, float v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FSUB })
	public static @infix float $minus(long v1, float v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FMUL })
	public static @infix float $times(long v1, float v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FDIV })
	public static @infix float $div(long v1, float v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FREM })
	public static @infix float $percent(long v1, float v2) { return v1 % v2; }
	
	// double operators
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPEQ })
	public static @infix boolean $eq$eq(long v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPNE })
	public static @infix boolean $bang$eq(long v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLT })
	public static @infix boolean $lt(long v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLE })
	public static @infix boolean $lt$eq(long v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGT })
	public static @infix boolean $gt(long v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGE })
	public static @infix boolean $gt$eq(long v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DADD })
	public static @infix double $plus(long v1, double v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DSUB })
	public static @infix double $minus(long v1, double v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DMUL })
	public static @infix double $times(long v1, double v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DDIV })
	public static @infix double $div(long v1, double v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DREM })
	public static @infix double $percent(long v1, double v2) { return v1 % v2; }
	
	// generic operators
	
	@Override
	public @prefix Long $plus() { return this; };
	
	@Override
	public @prefix Long $minus() { return Long.apply(-this.value); }
	
	@Override
	public @prefix Long $tilde() { return Long.apply(~this.value); }
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.longValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.longValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.longValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.longValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.longValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.longValue(); }
	
	@Override
	public Long $plus(Number v) { return Long.apply(this.value + v.longValue()); }
	
	@Override
	public Long $minus(Number v) { return Long.apply(this.value - v.longValue()); }
	
	@Override
	public Long $times(Number v) { return Long.apply(this.value * v.longValue()); }
	
	@Override
	public Double $div(Number v) { return Double.apply(this.value / v.doubleValue()); }
	
	@Override
	public Long $percent(Number v) { return Long.apply(this.value % v.longValue()); }
	
	@Override
	public Long $bslash(Integer v) { return Long.apply(this.value / v.longValue()); }
	
	@Override
	public Long $bar(Integer v) { return Long.apply(this.value | v.longValue()); }
	
	@Override
	public Long $amp(Integer v) { return Long.apply(this.value & v.longValue()); }
	
	@Override
	public Long $up(Integer v) { return Long.apply(this.value ^ v.longValue()); }
	
	@Override
	public Long $lt$lt(Integer v) { return Long.apply(this.value << v.longValue()); }
	
	@Override
	public Long $gt$gt(Integer v) { return Long.apply(this.value >> v.longValue()); }
	
	@Override
	public Long $gt$gt$gt(Integer v) { return Long.apply(this.value >>> v.longValue()); }
	
	@Override
	public int compareTo(Number o) { return java.lang.Long.compare(this.value, o.longValue()); }
	
	@Override
	public Long next() { return Long.apply(this.value + 1L); }
	
	@Override
	public Long previous() { return Long.apply(this.value - 1L); }
	
	// @formatter:on
	
	// Object methods
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Long", "toString", "(J)Ljava/lang/String;" })
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
	
	@Intrinsic({ LOAD_0, DUP2, BIPUSH, 32, LUSHR, LXOR, L2I })
	public static @postfix int $hash$hash(long v)
	{
		return (int) (v >>> 32 ^ v);
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
