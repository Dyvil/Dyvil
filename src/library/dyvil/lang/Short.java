package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;

public class Short implements Integer
{
	protected short	value;
	
	protected Short(short value)
	{
		this.value = value;
	}
	
	public static Short create(short v)
	{
		if (v >= 0 && v < ConstPool.tableSize)
		{
			return ConstPool.SHORTS[v];
		}
		return new Short(v);
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public short shortValue()
	{
		return this.value;
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
	@Intrinsic({ INSTANCE })
	public Int $plus()
	{
		return Int.create(this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, INEG })
	public Int $minus()
	{
		return Int.create((byte) -this.value);
	}
	
	@Override
	@Intrinsic({ ICONST_M1, INSTANCE, IXOR })
	public Int $tilde()
	{
		return Int.create((byte) ~this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, DUP, IMUL })
	public Int sqr()
	{
		return Int.create(this.value * this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ICONST_1, IDIV })
	public Int rec()
	{
		return Int.create((byte) (1 / this.value));
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $less(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $less$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $greater(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $greater$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(byte v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(byte v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(byte v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $div(byte v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(byte v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(byte v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(byte v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(byte v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $less$less(byte v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $greater$greater(byte v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $greater$greater$greater(byte v)
	{
		return Int.create(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $less(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $less$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $greater(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $greater$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(short v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(short v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(short v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $div(short v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(short v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(short v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(short v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(short v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $less$less(short v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $greater$greater(short v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $greater$greater$greater(short v)
	{
		return Int.create(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $less(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $less$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $greater(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $greater$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(char v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(char v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(char v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $div(char v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(char v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(char v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(char v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(char v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $less$less(char v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $greater$greater(char v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $greater$greater$greater(char v)
	{
		return Int.create(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGE })
	public boolean $less(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPGT })
	public boolean $less$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLE })
	public boolean $greater(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPLT })
	public boolean $greater$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IADD })
	public Int $plus(int v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISUB })
	public Int $minus(int v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IMUL })
	public Int $times(int v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IDIV })
	public Int $div(int v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IREM })
	public Int $percent(int v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND })
	public Int $amp(int v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR })
	public Int $bar(int v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR })
	public Int $up(int v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHL })
	public Int $less$less(int v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, ISHR })
	public Int $greater$greater(int v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IUSHR })
	public Int $greater$greater$greater(int v)
	{
		return Int.create(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPNE })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPEQ })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPGE })
	public boolean $less(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPGT })
	public boolean $less$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPLE })
	public boolean $greater(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, IF_LCMPLT })
	public boolean $greater$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LADD })
	public Long $plus(long v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LSUB })
	public Long $minus(long v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LMUL })
	public Long $times(long v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LDIV })
	public Long $div(long v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LREM })
	public Long $percent(long v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LAND })
	public Long $amp(long v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LOR })
	public Long $bar(long v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2L, ARGUMENTS, LXOR })
	public Long $up(long v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2I, ISHL })
	public Int $less$less(long v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2I, ISHR })
	public Int $greater$greater(long v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, L2I, IUSHR })
	public Int $greater$greater$greater(long v)
	{
		return Int.create(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPNE })
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
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPGE })
	public boolean $less(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPGT })
	public boolean $less$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPLE })
	public boolean $greater(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, IF_FCMPLT })
	public boolean $greater$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FADD })
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FSUB })
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FMUL })
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FDIV })
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2F, ARGUMENTS, FREM })
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPNE })
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
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPGE })
	public boolean $less(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPGT })
	public boolean $less$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPLE })
	public boolean $greater(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, IF_DCMPLT })
	public boolean $greater$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DADD })
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DSUB })
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DMUL })
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DDIV })
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Intrinsic({ INSTANCE, I2D, ARGUMENTS, DREM })
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.byteValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.byteValue();
	}
	
	@Override
	public boolean $less(Number v)
	{
		return this.value < v.byteValue();
	}
	
	@Override
	public boolean $less$eq(Number v)
	{
		return this.value <= v.byteValue();
	}
	
	@Override
	public boolean $greater(Number v)
	{
		return this.value > v.byteValue();
	}
	
	@Override
	public boolean $greater$eq(Number v)
	{
		return this.value >= v.byteValue();
	}
	
	@Override
	public Int $plus(Number v)
	{
		return Int.create(this.value + v.intValue());
	}
	
	@Override
	public Int $minus(Number v)
	{
		return Int.create(this.value - v.intValue());
	}
	
	@Override
	public Int $times(Number v)
	{
		return Int.create(this.value * v.intValue());
	}
	
	@Override
	public Int $div(Number v)
	{
		return Int.create(this.value / v.intValue());
	}
	
	@Override
	public Int $percent(Number v)
	{
		return Int.create(this.value % v.intValue());
	}
	
	@Override
	public Int $bar(Integer v)
	{
		return Int.create(this.value | v.intValue());
	}
	
	@Override
	public Int $amp(Integer v)
	{
		return Int.create(this.value & v.intValue());
	}
	
	@Override
	public Int $up(Integer v)
	{
		return Int.create(this.value ^ v.intValue());
	}
	
	@Override
	public Int $less$less(Integer v)
	{
		return Int.create(this.value << v.intValue());
	}
	
	@Override
	public Int $greater$greater(Integer v)
	{
		return Int.create(this.value >> v.intValue());
	}
	
	@Override
	public Int $greater$greater$greater(Integer v)
	{
		return Int.create(this.value >>> v.intValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
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
		return this.value == other.shortValue();
	}
}
