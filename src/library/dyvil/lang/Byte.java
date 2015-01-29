package dyvil.lang;

import dyvil.lang.annotation.Bytecode;
import static dyvil.reflect.Opcodes.*;

public class Byte implements Integer
{
	protected byte	value;
	
	protected Byte(byte value)
	{
		this.value = value;
	}
	
	public static Byte create(byte v)
	{
		if (v >= 0 && v < ConstPool.tableSize)
		{
			return ConstPool.BYTES[v];
		}
		return new Byte(v);
	}
	
	@Override
	@Bytecode
	public byte byteValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode
	public short shortValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2C)
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Bytecode
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2L)
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2F)
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2D)
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Bytecode(postfixOpcodes = { INEG, I2B })
	public Byte $minus()
	{
		return Byte.create((byte) -this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = ICONST_M1, postfixOpcodes = { IXOR, I2B })
	public Byte $tilde()
	{
		return Byte.create((byte) ~this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP, IMUL })
	public Int sqr()
	{
		return Int.create(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = ICONST_1, postfixOpcodes = { IDIV, I2B })
	public Byte rec()
	{
		return Byte.create((byte) (1 / this.value));
	}
	
	// byte operators
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPNE)
	public boolean $eq$eq(byte b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPEQ)
	public boolean $bang$eq(byte b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGE)
	public boolean $less(byte b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $less$eq(byte b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLE)
	public boolean $greater(byte b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $greater$eq(byte b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IADD, I2B })
	public Byte $plus(byte v)
	{
		return Byte.create((byte) (this.value + v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { ISUB, I2B })
	public Byte $minus(byte v)
	{
		return Byte.create((byte) (this.value - v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IMUL, I2B })
	public Byte $times(byte v)
	{
		return Byte.create((byte) (this.value * v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IDIV, I2B })
	public Byte $div(byte v)
	{
		return Byte.create((byte) (this.value / v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IREM, I2B })
	public Byte $percent(byte v)
	{
		return Byte.create((byte) (this.value % v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IAND)
	public Byte $amp(byte v)
	{
		return Byte.create((byte) (this.value & v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IOR)
	public Byte $bar(byte v)
	{
		return Byte.create((byte) (this.value | v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IXOR)
	public Byte $up(byte v)
	{
		return Byte.create((byte) (this.value ^ v));
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHL)
	public Int $less$less(byte v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHR)
	public Int $greater$greater(byte v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IUSHR)
	public Int $greater$greater$greater(byte v)
	{
		return Int.create(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPNE)
	public boolean $eq$eq(short b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPEQ)
	public boolean $bang$eq(short b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGE)
	public boolean $less(short b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $less$eq(short b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLE)
	public boolean $greater(short b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $greater$eq(short b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IADD, I2S })
	public Short $plus(short v)
	{
		return Short.create((short) (this.value + v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { ISUB, I2S })
	public Short $minus(short v)
	{
		return Short.create((short) (this.value - v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IMUL, I2S })
	public Short $times(short v)
	{
		return Short.create((short) (this.value * v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IDIV, I2S })
	public Short $div(short v)
	{
		return Short.create((short) (this.value / v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IREM, I2S })
	public Short $percent(short v)
	{
		return Short.create((short) (this.value % v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IAND)
	public Short $amp(short v)
	{
		return Short.create((short) (this.value & v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IOR)
	public Short $bar(short v)
	{
		return Short.create((short) (this.value | v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IXOR)
	public Short $up(short v)
	{
		return Short.create((short) (this.value ^ v));
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHL)
	public Int $less$less(short v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHR)
	public Int $greater$greater(short v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IUSHR)
	public Int $greater$greater$greater(short v)
	{
		return Int.create(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPNE)
	public boolean $eq$eq(char b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPEQ)
	public boolean $bang$eq(char b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGE)
	public boolean $less(char b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $less$eq(char b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLE)
	public boolean $greater(char b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLT)
	public boolean $greater$eq(char b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IADD, I2C })
	public Char $plus(char v)
	{
		return Char.create((char) (this.value + v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { ISUB, I2C })
	public Char $minus(char v)
	{
		return Char.create((char) (this.value - v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IMUL, I2C })
	public Char $times(char v)
	{
		return Char.create((char) (this.value * v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IDIV, I2C })
	public Char $div(char v)
	{
		return Char.create((char) (this.value / v));
	}
	
	@Override
	@Bytecode(postfixOpcodes = { IREM, I2C })
	public Char $percent(char v)
	{
		return Char.create((char) (this.value % v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IAND)
	public Char $amp(char v)
	{
		return Char.create((char) (this.value & v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IOR)
	public Char $bar(char v)
	{
		return Char.create((char) (this.value | v));
	}
	
	@Override
	@Bytecode(postfixOpcode = IXOR)
	public Char $up(char v)
	{
		return Char.create((char) (this.value ^ v));
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHL)
	public Int $less$less(char v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHR)
	public Int $greater$greater(char v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IUSHR)
	public Int $greater$greater$greater(char v)
	{
		return Int.create(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPNE)
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPEQ)
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGE)
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPGT)
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLE)
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_ICMPLT)
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IADD)
	public Int $plus(int v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISUB)
	public Int $minus(int v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IMUL)
	public Int $times(int v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IDIV)
	public Int $div(int v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IREM)
	public Int $percent(int v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IAND)
	public Int $amp(int v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IOR)
	public Int $bar(int v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IXOR)
	public Int $up(int v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHL)
	public Int $less$less(int v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHR)
	public Int $greater$greater(int v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IUSHR)
	public Int $greater$greater$greater(int v)
	{
		return Int.create(this.value >>> v);
	}
	
	// long operators
	
	@Override
	 @Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPNE)
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPEQ)
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPGE)
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPGT)
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPLE)
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPLT)
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LADD)
	public Long $plus(long v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LSUB)
	public Long $minus(long v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LMUL)
	public Long $times(long v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LDIV)
	public Long $div(long v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LREM)
	public Long $percent(long v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LAND)
	public Long $amp(long v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LOR)
	public Long $bar(long v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LXOR)
	public Long $up(long v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2I, ISHL })
	public Int $less$less(long v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2I, ISHR })
	public Int $greater$greater(long v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2I, IUSHR })
	public Int $greater$greater$greater(long v)
	{
		return Int.create(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPNE)
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPEQ)
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPGE)
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPGT)
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPLE)
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPLT)
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FADD)
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPNE)
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPEQ)
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPGE)
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPGT)
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPLE)
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPLT)
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.byteValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.byteValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.byteValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value <= b.byteValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.byteValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.byteValue();
	}
	
	@Override
	public Byte $plus(Number v)
	{
		return Byte.create((byte) (this.value + v.byteValue()));
	}
	
	@Override
	public Byte $minus(Number v)
	{
		return Byte.create((byte) (this.value - v.byteValue()));
	}
	
	@Override
	public Byte $times(Number v)
	{
		return Byte.create((byte) (this.value * v.byteValue()));
	}
	
	@Override
	public Byte $div(Number v)
	{
		return Byte.create((byte) (this.value / v.byteValue()));
	}
	
	@Override
	public Byte $percent(Number v)
	{
		return Byte.create((byte) (this.value % v.byteValue()));
	}
	
	@Override
	public Byte $bar(Integer v)
	{
		return Byte.create((byte) (this.value | v.byteValue()));
	}
	
	@Override
	public Byte $amp(Integer v)
	{
		return Byte.create((byte) (this.value & v.byteValue()));
	}
	
	@Override
	public Byte $up(Integer v)
	{
		return Byte.create((byte) (this.value ^ v.byteValue()));
	}
	
	@Override
	public Byte $less$less(Integer v)
	{
		return Byte.create((byte) (this.value << v.byteValue()));
	}
	
	@Override
	public Byte $greater$greater(Integer v)
	{
		return Byte.create((byte) (this.value >> v.byteValue()));
	}
	
	@Override
	public Byte $greater$greater$greater(Integer v)
	{
		return Byte.create((byte) (this.value >>> v.byteValue()));
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Byte.toString(this.value);
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
