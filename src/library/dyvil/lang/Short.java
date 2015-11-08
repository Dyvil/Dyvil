package dyvil.lang;

import java.io.Serializable;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

public class Short implements Integer, Serializable
{
	private static final long serialVersionUID = -731503833337870116L;
	
	public static final int		min		= java.lang.Short.MIN_VALUE;
	public static final int		max		= java.lang.Short.MAX_VALUE;
	public static final byte	size	= java.lang.Short.SIZE;
	
	protected short value;
	
	private static final class ConstantPool
	{
		protected static final int	TABLE_MIN	= -128;
		protected static final int	TABLE_SIZE	= 256;
		protected static final int	TABLE_MAX	= TABLE_MIN + TABLE_SIZE;
		
		protected static final Short[] TABLE = new Short[TABLE_SIZE];
		
		static
		{
			for (int i = 0; i < TABLE_SIZE; i++)
			{
				TABLE[i] = new Short((short) (i + TABLE_MIN));
			}
		}
	}
	
	public static Short apply(short v)
	{
		if (v >= ConstantPool.TABLE_MIN && v < ConstantPool.TABLE_MAX)
		{
			return ConstantPool.TABLE[v - ConstantPool.TABLE_MIN];
		}
		return new Short(v);
	}
	
	public static @infix short unapply(Short v)
	{
		return v == null ? 0 : v.value;
	}
	
	protected Short(short value)
	{
		this.value = value;
	}
	
	// @formatter:off
	
	@Override
	public byte byteValue() { return (byte) this.value; }
	
	@Override
	public short shortValue() { return this.value; }
	
	@Override
	public char charValue() { return (char) this.value; }
	
	@Override
	public int intValue() { return this.value; }
	
	@Override
	public long longValue() { return this.value; }
	
	@Override
	public float floatValue() { return this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	public static @prefix int $plus(short v) { return v; }
	
	@Intrinsic({ LOAD_0, INEG })
	public static @prefix int $minus(short v) { return -v; }
	
	@Intrinsic({ LOAD_0, ICONST_M1, IXOR })
	public static @prefix int $tilde(short v) { return ~v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(short v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(short v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(short v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(short v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(short v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(short v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(short v1, byte v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(short v1, byte v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(short v1, byte v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(short v1, byte v2) { return (float) v1 / (float) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(short v1, byte v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(short v1, byte v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(short v1, byte v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(short v1, byte v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(short v1, byte v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(short v1, byte v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(short v1, byte v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(short v1, byte v2) { return v1 >>> v2; }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(short v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(short v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(short v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(short v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(short v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(short v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(short v1, short v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(short v1, short v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(short v1, short v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(short v1, short v2) { return (float) v1 / (float) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(short v1, short v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(short v1, short v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(short v1, short v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(short v1, short v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(short v1, short v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(short v1, short v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(short v1, short v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(short v1, short v2) { return v1 >>> v2; }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(short v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(short v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(short v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(short v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(short v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(short v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(short v1, char v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(short v1, char v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(short v1, char v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(short v1, char v2) { return (float) v1 / (float) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(short v1, char v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(short v1, char v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(short v1, char v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(short v1, char v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(short v1, char v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(short v1, char v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(short v1, char v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(short v1, char v2) { return v1 >>> v2; }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(short v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(short v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(short v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(short v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(short v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(short v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(short v1, int v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(short v1, int v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(short v1, int v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(short v1, int v2) { return (float) v1 / (float) v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(short v1, int v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(short v1, int v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(short v1, int v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(short v1, int v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(short v1, int v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(short v1, int v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(short v1, int v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(short v1, int v2) { return v1 >>> v2; }
	
	// long operators
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	public static @infix boolean $eq$eq(short v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPNE })
	public static @infix boolean $bang$eq(short v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLT })
	public static @infix boolean $lt(short v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLE })
	public static @infix boolean $lt$eq(short v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGT })
	public static @infix boolean $gt(short v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGE })
	public static @infix boolean $gt$eq(short v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LADD })
	public static @infix long $plus(short v1, long v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSUB })
	public static @infix long $minus(short v1, long v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LMUL })
	public static @infix long $times(short v1, long v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, L2D, DDIV })
	public static @infix double $div(short v1, long v2) { return (double) v1 / (double) v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LREM })
	public static @infix long $percent(short v1, long v2) { return v1 % v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LDIV })
	public static @infix long $bslash(short v1, long v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LAND })
	public static @infix long $amp(short v1, long v2) { return v1 & v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LOR })
	public static @infix long $bar(short v1, long v2) { return v1 | v2; }
	
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LXOR })
	public static @infix long $up(short v1, long v2) { return v1 ^ v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, ISHL })
	public static @infix int $lt$lt(short v1, long v2) { return v1 << v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, ISHR })
	public static @infix int $gt$gt(short v1, long v2) { return v1 >> v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2I, IUSHR })
	public static @infix int $gt$gt$gt(short v1, long v2) { return v1 >>> v2; }
	
	// float operators
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	public static @infix boolean $eq$eq(short v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPNE })
	public static @infix boolean $bang$eq(short v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLT })
	public static @infix boolean $lt(short v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLE })
	public static @infix boolean $lt$eq(short v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGT })
	public static @infix boolean $gt(short v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGE })
	public static @infix boolean $gt$eq(short v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FADD })
	public static @infix float $plus(short v1, float v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FSUB })
	public static @infix float $minus(short v1, float v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FMUL })
	public static @infix float $times(short v1, float v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FDIV })
	public static @infix float $div(short v1, float v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FREM })
	public static @infix float $percent(short v1, float v2) { return v1 % v2; }
	
	// double operators
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	public static @infix boolean $eq$eq(short v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPNE })
	public static @infix boolean $bang$eq(short v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLT })
	public static @infix boolean $lt(short v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLE })
	public static @infix boolean $lt$eq(short v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGT })
	public static @infix boolean $gt(short v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGE })
	public static @infix boolean $gt$eq(short v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DADD })
	public static @infix double $plus(short v1, double v2) { return v1 + v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DSUB })
	public static @infix double $minus(short v1, double v2) { return v1 - v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DMUL })
	public static @infix double $times(short v1, double v2) { return v1 * v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DDIV })
	public static @infix double $div(short v1, double v2) { return v1 / v2; }
	
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DREM })
	public static @infix double $percent(short v1, double v2) { return v1 % v2; }
	
	// generic operators
	
	@Override
	public @prefix Int $plus() { return Int.apply(this.value); };
	
	@Override
	public @prefix Int $minus() { return Int.apply(-this.value); }
	
	@Override
	public @prefix Int $tilde() { return Int.apply(~this.value); }
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.byteValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.byteValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.byteValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.byteValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.byteValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.byteValue(); }
	
	@Override
	public Int $plus(Number v) { return Int.apply(this.value + v.intValue()); }
	
	@Override
	public Int $minus(Number v) { return Int.apply(this.value - v.intValue()); }
	
	@Override
	public Int $times(Number v) { return Int.apply(this.value * v.intValue()); }
	
	@Override
	public Float $div(Number v) { return Float.apply(this.value / v.floatValue()); }
	
	@Override
	public Int $bslash(Integer v) { return Int.apply(this.value / v.intValue()); }
	
	@Override
	public Int $percent(Number v) { return Int.apply(this.value % v.intValue()); }
	
	@Override
	public Int $bar(Integer v) { return Int.apply(this.value | v.intValue()); }
	
	@Override
	public Int $amp(Integer v) { return Int.apply(this.value & v.intValue()); }
	
	@Override
	public Int $up(Integer v) { return Int.apply(this.value ^ v.intValue()); }
	
	@Override
	public Int $lt$lt(Integer v) { return Int.apply(this.value << v.intValue()); }
	
	@Override
	public Int $gt$gt(Integer v) { return Int.apply(this.value >> v.intValue()); }
	
	@Override
	public Int $gt$gt$gt(Integer v) { return Int.apply(this.value >>> v.intValue()); }
	
	@Override
	public int compareTo(Number o) { return java.lang.Long.compare(this.value, o.longValue()); }
	
	@Override
	public Short next() { return Short.apply((short) (this.value + 1)); }
	
	@Override
	public Short previous() { return Short.apply((short) (this.value - 1)); }
	
	// @formatter:on
	
	// Object methods
	
	public static @infix @inline String toString(short value)
	{
		return java.lang.Integer.toString(value);
	}
	
	public static @infix @inline String toBinaryString(short value)
	{
		return java.lang.Integer.toBinaryString(value);
	}
	
	public static @infix @inline String toHexString(short value)
	{
		return java.lang.Integer.toHexString(value);
	}
	
	public static @infix @inline String toOctalString(short value)
	{
		return java.lang.Integer.toOctalString(value);
	}
	
	public static @infix String toString(short value, int radix)
	{
		switch (radix)
		{
		case 2:
			return java.lang.Integer.toBinaryString(value);
		case 8:
			return java.lang.Integer.toOctalString(value);
		case 10:
			return java.lang.Integer.toString(value);
		case 16:
			return java.lang.Integer.toHexString(value);
		}
		return java.lang.Integer.toString(value, radix);
	}
	
	@Override
	public String toString()
	{
		return java.lang.Short.toString(this.value);
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
		return this.value == other.byteValue();
	}
}
