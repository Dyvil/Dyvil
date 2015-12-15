package dyvil.lang;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.FloatConvertible;
import dyvil.reflect.Modifiers;
import sun.misc.FloatingDecimal;

import java.io.Serializable;

import static dyvil.reflect.Opcodes.*;

@FloatConvertible
public class Float implements Number, Serializable
{
	private static final long serialVersionUID = 2128649158072690759L;
	
	public static final float min               = java.lang.Float.MIN_VALUE;
	public static final float max               = java.lang.Float.MAX_VALUE;
	public static final float NaN               = java.lang.Float.NaN;
	public static final float infinity          = java.lang.Float.POSITIVE_INFINITY;
	public static final float negative_infinity = java.lang.Float.NEGATIVE_INFINITY;
	public static final byte  size              = java.lang.Float.SIZE;
	
	protected float value;
	
	public static Float apply(float v)
	{
		return new Float(v);
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	public static float unapply(Float v)
	{
		return v == null ? 0F : v.value;
	}
	
	protected Float(float value)
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
	public long longValue() { return (long) this.value; }
	
	@Override
	public float floatValue() { return this.value; }
	
	@Override
	public double doubleValue() { return this.value; }
	
	// Unary operators
	
	@Intrinsic({ LOAD_0 })
	@DyvilModifiers(Modifiers.PREFIX) public static float $plus(float v) { return v; }
	
	@Intrinsic({ LOAD_0, FNEG })
	@DyvilModifiers(Modifiers.PREFIX) public static float $minus(float v) { return -v; }
	
	// byte operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, byte v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, byte v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, byte v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, byte v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, byte v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, byte v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, byte v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, byte v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, byte v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, byte v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, byte v2) { return (v1 % v2); }
	
	// short operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, short v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, short v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, short v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, short v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, short v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, short v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, short v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, short v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, short v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, short v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, short v2) { return (v1 % v2); }
	
	// char operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, char v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, char v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, char v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, char v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, char v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, char v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, char v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, char v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, char v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, char v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, char v2) { return (v1 % v2); }
	
	// int operators
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, int v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, int v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, int v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, int v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, int v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, int v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, int v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, int v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, int v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, int v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, I2F, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, int v2) { return (v1 % v2); }
	
	// long operators
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, long v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, long v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, long v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, long v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, long v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, long v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, long v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, long v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, long v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, long v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, L2F, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, long v2) { return (v1 % v2); }
	
	// float operators
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, float v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, float v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, float v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, float v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, float v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, float v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, FADD })
	@DyvilModifiers(Modifiers.INFIX) public static float $plus(float v1, float v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FSUB })
	@DyvilModifiers(Modifiers.INFIX) public static float $minus(float v1, float v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FMUL })
	@DyvilModifiers(Modifiers.INFIX) public static float $times(float v1, float v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FDIV })
	@DyvilModifiers(Modifiers.INFIX) public static float $div(float v1, float v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, FREM })
	@DyvilModifiers(Modifiers.INFIX) public static float $percent(float v1, float v2) { return (v1 % v2); }
	
	// double operators
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(float v1, double v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(float v1, double v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt(float v1, double v2) { return v1 < v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPLE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq(float v1, double v2) { return v1 <= v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGT })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt(float v1, double v2) { return v1 > v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DCMPGE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $gt$eq(float v1, double v2) { return v1 >= v2; }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DADD })
	@DyvilModifiers(Modifiers.INFIX) public static double $plus(float v1, double v2) { return (v1 + v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DSUB })
	@DyvilModifiers(Modifiers.INFIX) public static double $minus(float v1, double v2) { return (v1 - v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DMUL })
	@DyvilModifiers(Modifiers.INFIX) public static double $times(float v1, double v2) { return (v1 * v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DDIV })
	@DyvilModifiers(Modifiers.INFIX) public static double $div(float v1, double v2) { return (v1 / v2); }
	
	@Intrinsic({ LOAD_0, F2D, LOAD_1, DREM })
	@DyvilModifiers(Modifiers.INFIX) public static double $percent(float v1, double v2) { return (v1 % v2); }
	
	// generic operators
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Float $plus() { return this; }
	
	@Override
	@DyvilModifiers(Modifiers.PREFIX) public Float $minus() { return Float.apply(-this.value); }
	
	@Override
	public boolean $eq$eq(Number v) { return this.value == v.floatValue(); }
	
	@Override
	public boolean $bang$eq(Number v) { return this.value != v.floatValue(); }
	
	@Override
	public boolean $lt(Number v) { return this.value < v.floatValue(); }
	
	@Override
	public boolean $lt$eq(Number v) { return this.value <= v.floatValue(); }
	
	@Override
	public boolean $gt(Number v) { return this.value > v.floatValue(); }
	
	@Override
	public boolean $gt$eq(Number v) { return this.value >= v.floatValue(); }
	
	@Override
	public Float $plus(Number v) { return Float.apply(this.value + v.floatValue()); }
	
	@Override
	public Float $minus(Number v) { return Float.apply(this.value - v.floatValue()); }
	
	@Override
	public Float $times(Number v) { return Float.apply(this.value * v.floatValue()); }
	
	@Override
	public Float $div(Number v) { return Float.apply(this.value / v.floatValue()); }
	
	@Override
	public Float $percent(Number v) { return Float.apply(this.value % v.floatValue()); }
	
	@Override
	public int compareTo(Number o) { return java.lang.Double.compare(this.value, o.doubleValue()); }
	
	@Override
	public Float next() { return Float.apply(this.value + 1F); }
	
	@Override
	public Float previous() { return Float.apply(this.value - 1F); }
	
	// @formatter:on
	
	// Object methods
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Float", "toString",
			"(F)Ljava/lang/String;" })
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toString(float value)
	{
		return FloatingDecimal.toJavaFormatString(value);
	}
	
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	public static String toHexString(float value)
	{
		return java.lang.Double.toHexString(value);
	}
	
	@Override
	public String toString()
	{
		return FloatingDecimal.toJavaFormatString(this.value);
	}
	
	@Intrinsic(value = { LOAD_0, INVOKESTATIC, 0, 1, 2 }, strings = { "java/lang/Float", "hashCode", "(F)I" })
	@DyvilModifiers(Modifiers.INFIX)
	public static int $hash$hash(float f)
	{
		return java.lang.Float.floatToIntBits(f);
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
