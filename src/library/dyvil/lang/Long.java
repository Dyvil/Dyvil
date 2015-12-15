package dyvil.lang;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.LongConvertible;
import dyvil.reflect.Modifiers;

import java.io.Serializable;

import static dyvil.reflect.Opcodes.*;

@LongConvertible
public class Long implements Integer, Serializable
{
	private static final long serialVersionUID = 4495480142241309185L;
	
	public static final long min  = java.lang.Long.MIN_VALUE;
	public static final long max  = java.lang.Long.MAX_VALUE;
	public static final byte size = java.lang.Long.SIZE;
	
	protected long value;
	
	private static final class ConstantPool
	{
		protected static final int TABLE_MIN  = -128;
		protected static final int TABLE_SIZE = 256;
		protected static final int TABLE_MAX  = TABLE_MIN + TABLE_SIZE;
		
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
	
	@DyvilModifiers(Modifiers.INFIX) public static
	long unapply(Long v)
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
	@DyvilModifiers(Modifiers.PREFIX) public static long $plus(long v) { return v; }
	
	@Intrinsic({ LOAD_0, LNEG })
	@DyvilModifiers(Modifiers.PREFIX) public static long $minus(long v) { return -v; }
	
	@Intrinsic({ LOAD_0, LCONST_M1, LXOR })
	@DyvilModifiers(Modifiers.PREFIX) public static long $tilde(long v) { return ~v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(long v1, byte v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(long v1, byte v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(long v1, byte v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, byte v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(long v1, byte v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(long v1, byte v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(long v1, byte v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(long v1, byte v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(long v1, byte v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static long $lt$lt(long v1, byte v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt(long v1, byte v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt$gt(long v1, byte v2) { return v1 >>> v2; }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(long v1, short v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(long v1, short v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(long v1, short v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, short v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(long v1, short v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(long v1, short v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(long v1, short v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(long v1, short v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(long v1, short v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static long $lt$lt(long v1, short v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt(long v1, short v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt$gt(long v1, short v2) { return v1 >>> v2; }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(long v1, char v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(long v1, char v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(long v1, char v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, char v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(long v1, char v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(long v1, char v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(long v1, char v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(long v1, char v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(long v1, char v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static long $lt$lt(long v1, char v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt(long v1, char v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt$gt(long v1, char v2) { return v1 >>> v2; }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(long v1, int v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(long v1, int v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(long v1, int v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, I2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, int v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(long v1, int v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(long v1, int v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(long v1, int v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(long v1, int v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2L, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(long v1, int v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static long $lt$lt(long v1, int v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt(long v1, int v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt$gt(long v1, int v2) { return v1 >>> v2; }
	
	// long operators
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(long v1, long v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(long v1, long v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(long v1, long v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, L2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, long v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(long v1, long v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(long v1, long v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(long v1, long v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(long v1, long v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(long v1, long v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static long $lt$lt(long v1, long v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt(long v1, long v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static long $gt$gt$gt(long v1, long v2) { return v1 >>> v2; }
	
	// float operators
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(long v1, float v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(long v1, float v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(long v1, float v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(long v1, float v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, L2F, LOAD_1, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(long v1, float v2) { return v1 % v2; }
	
	// double operators
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(long v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(long v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(long v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(long v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(long v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(long v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DADD })
	@DyvilModifiers(Modifiers.INFIX) public static double $plus(long v1, double v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DSUB })
	@DyvilModifiers(Modifiers.INFIX) public static double $minus(long v1, double v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DMUL })
	@DyvilModifiers(Modifiers.INFIX) public static double $times(long v1, double v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(long v1, double v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, L2D, LOAD_1, DREM })
	@DyvilModifiers(Modifiers.INFIX) public static double $percent(long v1, double v2) { return v1 % v2; }
	
	// generic operators
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Long $plus() { return this; }
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Long $minus() { return Long.apply(-this.value); }
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Long $tilde() { return Long.apply(~this.value); }
	
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
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Long", "toString",
			"(J)Ljava/lang/String;" })
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE) public static
	String toString(long value)
	{
		return java.lang.Long.toString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE) public static
	String toBinaryString(long value)
	{
		return java.lang.Long.toBinaryString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE) public static
	String toHexString(long value)
	{
		return java.lang.Long.toHexString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE) public static
	String toOctalString(long value)
	{
		return java.lang.Long.toOctalString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX) public static
	String toString(long value, int radix)
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
	
	@Intrinsic( { LOAD_0, DUP2, BIPUSH, 32, LUSHR, LXOR, L2I })
	@DyvilModifiers(Modifiers.INFIX) public static
	int $hash$hash(long v)
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
