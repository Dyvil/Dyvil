package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.IntConvertible;

import dyvil.annotation.*;

import static dyvil.reflect.Opcodes.*;

@IntConvertible
public class Int implements Integer, Serializable
{
	private static final long serialVersionUID = -2779746531375152610L;
	
	public static final int		min		= java.lang.Integer.MIN_VALUE;
	public static final int		max		= java.lang.Integer.MAX_VALUE;
	public static final byte	size	= java.lang.Integer.SIZE;
	
	protected int value;
	
	private static final class ConstantPool
	{
		protected static final int	TABLE_MIN	= -128;
		protected static final int	TABLE_SIZE	= 256;
		protected static final int	TABLE_MAX	= TABLE_MIN + TABLE_SIZE;
		
		protected static final Int[] TABLE = new Int[TABLE_SIZE];
		
		static
		{
			for (int i = 0; i < TABLE_SIZE; i++)
			{
				TABLE[i] = new Int(i + TABLE_MIN);
			}
		}
	}
	
	public static Int apply(int v)
	{
		if (v >= ConstantPool.TABLE_MIN && v < ConstantPool.TABLE_MAX)
		{
			return ConstantPool.TABLE[v - ConstantPool.TABLE_MIN];
		}
		return new Int(v);
	}
	
	public static @infix int unapply(Int v)
	{
		return v == null ? 0 : v.value;
	}
	
	protected Int(int value)
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
	public int intValue() { return this.value; }
	
	@Override
	public long longValue() { return this.value; }
	
	@Override
	public float floatValue() { return this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	public static @prefix int $plus(int v1) { return v1; }
	
	@Intrinsic({ LOAD_0, INEG })
	public static @prefix int $minus(int v1) { return (byte) -v1; }

	@Intrinsic({ LOAD_0, ICONST_M1, IXOR })
	public static @prefix int $tilde(int v1) { return (byte) ~v1; }
	
	// byte operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(int v1, byte v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(int v1, byte v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(int v1, byte v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(int v1, byte v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(int v1, byte v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(int v1, byte v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(int v1, byte v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(int v1, byte v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(int v1, byte v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(int v1, byte v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(int v1, byte v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(int v1, byte v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(int v1, byte v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(int v1, byte v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(int v1, byte v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(int v1, byte v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(int v1, byte v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(int v1, byte v2) { return v1 >>> v2; }
	
	// short operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(int v1, short v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(int v1, short v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(int v1, short v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(int v1, short v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(int v1, short v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(int v1, short v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(int v1, short v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(int v1, short v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(int v1, short v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(int v1, short v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(int v1, short v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(int v1, short v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(int v1, short v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(int v1, short v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(int v1, short v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(int v1, short v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(int v1, short v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(int v1, short v2) { return v1 >>> v2; }
	
	// char operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(int v1, char v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(int v1, char v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(int v1, char v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(int v1, char v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(int v1, char v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(int v1, char v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(int v1, char v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(int v1, char v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(int v1, char v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(int v1, char v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(int v1, char v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(int v1, char v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(int v1, char v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(int v1, char v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(int v1, char v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(int v1, char v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(int v1, char v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(int v1, char v2) { return v1 >>> v2; }
	
	// int operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public static @infix boolean $eq$eq(int v1, int v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public static @infix boolean $bang$eq(int v1, int v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public static @infix boolean $lt(int v1, int v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public static @infix boolean $lt$eq(int v1, int v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public static @infix boolean $gt(int v1, int v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public static @infix boolean $gt$eq(int v1, int v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public static @infix int $plus(int v1, int v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public static @infix int $minus(int v1, int v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public static @infix int $times(int v1, int v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public static @infix float $div(int v1, int v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public static @infix int $percent(int v1, int v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public static @infix int $bslash(int v1, int v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public static @infix int $amp(int v1, int v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public static @infix int $bar(int v1, int v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public static @infix int $up(int v1, int v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public static @infix int $lt$lt(int v1, int v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public static @infix int $gt$gt(int v1, int v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public static @infix int $gt$gt$gt(int v1, int v2) { return v1 >>> v2; }
	
	// long operators

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	public static @infix boolean $eq$eq(int v1, long v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPNE })
	public static @infix boolean $bang$eq(int v1, long v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLT })
	public static @infix boolean $lt(int v1, long v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLE })
	public static @infix boolean $lt$eq(int v1, long v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	public static @infix boolean $gt(int v1, long v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGE })
	public static @infix boolean $gt$eq(int v1, long v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LADD })
	public static @infix long $plus(int v1, long v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSUB })
	public static @infix long $minus(int v1, long v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LMUL })
	public static @infix long $times(int v1, long v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, L2D, DDIV })
	public static @infix double $div(int v1, long v2) { return (double) v1 / (double) v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LREM })
	public static @infix long $percent(int v1, long v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LDIV })
	public static @infix long $bslash(int v1, long v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LAND })
	public static @infix long $amp(int v1, long v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LOR })
	public static @infix long $bar(int v1, long v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LXOR })
	public static @infix long $up(int v1, long v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSHL })
	public static @infix int $lt$lt(int v1, long v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSHR })
	public static @infix int $gt$gt(int v1, long v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LUSHR })
	public static @infix int $gt$gt$gt(int v1, long v2) { return v1 >>> v2; }
	
	// float operators

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	public static @infix boolean $eq$eq(int v1, float v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	public static @infix boolean $bang$eq(int v1, float v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLT })
	public static @infix boolean $lt(int v1, float v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLE })
	public static @infix boolean $lt$eq(int v1, float v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGT })
	public static @infix boolean $gt(int v1, float v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGE })
	public static @infix boolean $gt$eq(int v1, float v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FADD })
	public static @infix float $plus(int v1, float v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FSUB })
	public static @infix float $minus(int v1, float v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FMUL })
	public static @infix float $times(int v1, float v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FDIV })
	public static @infix float $div(int v1, float v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FREM })
	public static @infix float $percent(int v1, float v2) { return v1 % v2; }
	
	// double operators

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	public static @infix boolean $eq$eq(int v1, double v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	public static @infix boolean $bang$eq(int v1, double v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLT })
	public static @infix boolean $lt(int v1, double v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLE })
	public static @infix boolean $lt$eq(int v1, double v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGT })
	public static @infix boolean $gt(int v1, double v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGE })
	public static @infix boolean $gt$eq(int v1, double v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DADD })
	public static @infix double $plus(int v1, double v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DSUB })
	public static @infix double $minus(int v1, double v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DMUL })
	public static @infix double $times(int v1, double v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DDIV })
	public static @infix double $div(int v1, double v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DREM })
	public static @infix double $percent(int v1, double v2) { return v1 % v2; }
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.intValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.intValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.intValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.intValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.intValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.intValue(); }
	
	@Override
	public @prefix Int $plus() { return this; }
	
	@Override
	public @prefix Int $minus() { return Int.apply(-this.value); }
	
	@Override
	public @prefix Int $tilde() { return Int.apply(~this.value); }
	
	@Override
	public Int $plus(Number v) { return Int.apply(this.value + v.intValue()); }
	
	@Override
	public Int $minus(Number v) { return Int.apply(this.value - v.intValue()); }
	
	@Override
	public Int $times(Number v) { return Int.apply(this.value * v.intValue()); }
	
	@Override
	public Float $div(Number v) { return Float.apply(this.value / v.floatValue()); }
	
	@Override
	public Int $percent(Number v) { return Int.apply(this.value % v.intValue()); }
	
	@Override
	public Int $bslash(Integer v) { return Int.apply(this.value / v.intValue()); }
	
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
	public int compareTo(Number o) { return java.lang.Double.compare(this.value, o.doubleValue()); }
	
	@Override
	public Int next() { return Int.apply(this.value + 1); }
	
	@Override
	public Int previous() { return Int.apply(this.value - 1); }
	
	// @formatter:on
	
	// Object methods
	
	public static @infix @inline String toString(int value)
	{
		return java.lang.Integer.toString(value);
	}
	
	public static @infix @inline String toBinaryString(int value)
	{
		return java.lang.Integer.toBinaryString(value);
	}
	
	public static @infix @inline String toHexString(int value)
	{
		return java.lang.Integer.toHexString(value);
	}
	
	public static @infix @inline String toOctalString(int value)
	{
		return java.lang.Integer.toOctalString(value);
	}
	
	public static @infix String toString(int value, int radix)
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
	public java.lang.String toString()
	{
		return java.lang.Integer.toString(this.value);
	}
	
	// @formatter:on
	
	// Object methods
	
	@Intrinsic({ LOAD_0 })
	public static @postfix int $hash$hash(int v)
	{
		return v;
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
