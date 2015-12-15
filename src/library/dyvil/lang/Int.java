package dyvil.lang;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.IntConvertible;
import dyvil.reflect.Modifiers;

import java.io.Serializable;

import static dyvil.reflect.Opcodes.*;

@IntConvertible
public class Int implements Integer, Serializable
{
	private static final long serialVersionUID = -2779746531375152610L;
	
	public static final int  min  = java.lang.Integer.MIN_VALUE;
	public static final int  max  = java.lang.Integer.MAX_VALUE;
	public static final byte size = java.lang.Integer.SIZE;
	
	protected int value;
	
	private static final class ConstantPool
	{
		protected static final int TABLE_MIN  = -128;
		protected static final int TABLE_SIZE = 256;
		protected static final int TABLE_MAX  = TABLE_MIN + TABLE_SIZE;
		
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
	
	@DyvilModifiers(Modifiers.INFIX)
	public static int unapply(Int v)
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
	@DyvilModifiers(Modifiers.PREFIX) public static int $plus(int v1) { return v1; }
	
	@Intrinsic({ LOAD_0, INEG })
	@DyvilModifiers(Modifiers.PREFIX) public static int $minus(int v1) { return (byte) -v1; }

	@Intrinsic({ LOAD_0, ICONST_M1, IXOR })
	@DyvilModifiers(Modifiers.PREFIX) public static int $tilde(int v1) { return (byte) ~v1; }
	
	// byte operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, byte v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, byte v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, byte v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, byte v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, byte v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, byte v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	@DyvilModifiers(Modifiers.INFIX) public static int $plus(int v1, byte v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	@DyvilModifiers(Modifiers.INFIX) public static int $minus(int v1, byte v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	@DyvilModifiers(Modifiers.INFIX) public static int $times(int v1, byte v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(int v1, byte v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	@DyvilModifiers(Modifiers.INFIX) public static int $percent(int v1, byte v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	@DyvilModifiers(Modifiers.INFIX) public static int $bslash(int v1, byte v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	@DyvilModifiers(Modifiers.INFIX) public static int $amp(int v1, byte v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $bar(int v1, byte v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $up(int v1, byte v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	@DyvilModifiers(Modifiers.INFIX) public static int $lt$lt(int v1, byte v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt(int v1, byte v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt$gt(int v1, byte v2) { return v1 >>> v2; }
	
	// short operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, short v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, short v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, short v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, short v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, short v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, short v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	@DyvilModifiers(Modifiers.INFIX) public static int $plus(int v1, short v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	@DyvilModifiers(Modifiers.INFIX) public static int $minus(int v1, short v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	@DyvilModifiers(Modifiers.INFIX) public static int $times(int v1, short v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(int v1, short v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	@DyvilModifiers(Modifiers.INFIX) public static int $percent(int v1, short v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	@DyvilModifiers(Modifiers.INFIX) public static int $bslash(int v1, short v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	@DyvilModifiers(Modifiers.INFIX) public static int $amp(int v1, short v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $bar(int v1, short v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $up(int v1, short v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	@DyvilModifiers(Modifiers.INFIX) public static int $lt$lt(int v1, short v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt(int v1, short v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt$gt(int v1, short v2) { return v1 >>> v2; }
	
	// char operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, char v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, char v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, char v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, char v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, char v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, char v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	@DyvilModifiers(Modifiers.INFIX) public static int $plus(int v1, char v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	@DyvilModifiers(Modifiers.INFIX) public static int $minus(int v1, char v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	@DyvilModifiers(Modifiers.INFIX) public static int $times(int v1, char v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(int v1, char v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	@DyvilModifiers(Modifiers.INFIX) public static int $percent(int v1, char v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	@DyvilModifiers(Modifiers.INFIX) public static int $bslash(int v1, char v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	@DyvilModifiers(Modifiers.INFIX) public static int $amp(int v1, char v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $bar(int v1, char v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $up(int v1, char v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	@DyvilModifiers(Modifiers.INFIX) public static int $lt$lt(int v1, char v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt(int v1, char v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt$gt(int v1, char v2) { return v1 >>> v2; }
	
	// int operators

	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, int v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, int v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, int v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, int v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, int v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, int v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	@DyvilModifiers(Modifiers.INFIX) public static int $plus(int v1, int v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	@DyvilModifiers(Modifiers.INFIX) public static int $minus(int v1, int v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	@DyvilModifiers(Modifiers.INFIX) public static int $times(int v1, int v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(int v1, int v2) { return (float) v1 / (float) v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	@DyvilModifiers(Modifiers.INFIX) public static int $percent(int v1, int v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	@DyvilModifiers(Modifiers.INFIX) public static int $bslash(int v1, int v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	@DyvilModifiers(Modifiers.INFIX) public static int $amp(int v1, int v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $bar(int v1, int v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	@DyvilModifiers(Modifiers.INFIX) public static int $up(int v1, int v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	@DyvilModifiers(Modifiers.INFIX) public static int $lt$lt(int v1, int v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt(int v1, int v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt$gt(int v1, int v2) { return v1 >>> v2; }
	
	// long operators

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, long v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, long v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, long v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, long v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, long v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, long v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LADD })
	@DyvilModifiers(Modifiers.INFIX) public static long $plus(int v1, long v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSUB })
	@DyvilModifiers(Modifiers.INFIX) public static long $minus(int v1, long v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LMUL })
	@DyvilModifiers(Modifiers.INFIX) public static long $times(int v1, long v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, L2D, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(int v1, long v2) { return (double) v1 / (double) v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LREM })
	@DyvilModifiers(Modifiers.INFIX) public static long $percent(int v1, long v2) { return v1 % v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LDIV })
	@DyvilModifiers(Modifiers.INFIX) public static long $bslash(int v1, long v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LAND })
	@DyvilModifiers(Modifiers.INFIX) public static long $amp(int v1, long v2) { return v1 & v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $bar(int v1, long v2) { return v1 | v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LXOR })
	@DyvilModifiers(Modifiers.INFIX) public static long $up(int v1, long v2) { return v1 ^ v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSHL })
	@DyvilModifiers(Modifiers.INFIX) public static int $lt$lt(int v1, long v2) { return v1 << v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt(int v1, long v2) { return v1 >> v2; }

	@Intrinsic({ LOAD_0, I2L, LOAD_1, LUSHR })
	@DyvilModifiers(Modifiers.INFIX) public static int $gt$gt$gt(int v1, long v2) { return v1 >>> v2; }
	
	// float operators

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, float v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, float v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, float v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, float v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, float v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, float v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(int v1, float v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(int v1, float v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(int v1, float v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(int v1, float v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2F, LOAD_1, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(int v1, float v2) { return v1 % v2; }
	
	// double operators

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(int v1, double v2) { return v1 == v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(int v1, double v2) { return v1 != v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(int v1, double v2) { return v1 < v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(int v1, double v2) { return v1 <= v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(int v1, double v2) { return v1 > v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(int v1, double v2) { return v1 >= v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DADD })
	@DyvilModifiers(Modifiers.INFIX) public static double $plus(int v1, double v2) { return v1 + v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DSUB })
	@DyvilModifiers(Modifiers.INFIX) public static double $minus(int v1, double v2) { return v1 - v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DMUL })
	@DyvilModifiers(Modifiers.INFIX) public static double $times(int v1, double v2) { return v1 * v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(int v1, double v2) { return v1 / v2; }

	@Intrinsic({ LOAD_0, I2D, LOAD_1, DREM })
	@DyvilModifiers(Modifiers.INFIX) public static double $percent(int v1, double v2) { return v1 % v2; }
	
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
	@DyvilModifiers(Modifiers.PREFIX) public Int $plus() { return this; }
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Int $minus() { return Int.apply(-this.value); }
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Int $tilde() { return Int.apply(~this.value); }
	
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
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Integer", "toString",
			"(I)Ljava/lang/String;" })
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toString(int value)
	{
		return java.lang.Integer.toString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toBinaryString(int value)
	{
		return java.lang.Integer.toBinaryString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toHexString(int value)
	{
		return java.lang.Integer.toHexString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toOctalString(int value)
	{
		return java.lang.Integer.toOctalString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(int value, int radix)
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
	
	@Intrinsic( { LOAD_0 })
	@DyvilModifiers(Modifiers.INFIX)
	public static int $hash$hash(int v)
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
