package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public abstract class Int implements Integer
{
	protected int	value;
	
	protected Int(int value)
	{
		this.value = value;
	}
	
	@Override
	public abstract Int $eq(byte v);
	
	@Override
	public abstract Int $eq(short v);
	
	@Override
	public abstract Int $eq(char v);
	
	@Override
	public abstract Int $eq(int v);
	
	@Override
	public abstract Long $eq(long v);
	
	@Override
	public abstract Float $eq(float v);
	
	@Override
	public abstract Double $eq(double v);
	
	@Override
	public Number $eq(Number v)
	{
		return v;
	}
	
	@Override
	public Integer $eq(Integer v)
	{
		return v;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2B)
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2S)
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = I2C)
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = {})
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
	@Bytecode(postfixOpcode = INEG)
	public Int $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
	@Bytecode(postfixOpcode = IBIN)
	public Int $tilde()
	{
		return this.$eq(~this.value);
	}
	
	@Override
	public Int $plus$plus()
	{
		return this.$eq(this.value + 1);
	}
	
	@Override
	public Int $minus$minus()
	{
		return this.$eq(this.value - 1);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP, IMUL })
	public Int sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = ICONST_1, postfixOpcode = IDIV)
	public Int rec()
	{
		return this.$eq(1 / this.value);
	}
	
	// byte operators
	
	@Override
	public boolean $eq$eq(byte b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(byte b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(byte b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(byte b)
	{
		return this.value <= b;
	}
	
	@Override
	public boolean $greater(byte b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(byte b)
	{
		return this.value >= b;
	}
	
	@Override
	public Int $plus(byte v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Int $minus(byte v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Int $times(byte v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Int $div(byte v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Int $percent(byte v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Int $bar(byte v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Int $amp(byte v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Int $up(byte v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Int $less$less(byte v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Int $greater$greater(byte v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(byte v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// short operators
	
	@Override
	public boolean $eq$eq(short b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(short b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(short b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(short b)
	{
		return this.value <= b;
	}
	
	@Override
	public boolean $greater(short b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(short b)
	{
		return this.value >= b;
	}
	
	@Override
	public Int $plus(short v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Int $minus(short v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Int $times(short v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Int $div(short v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Int $percent(short v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Int $bar(short v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Int $amp(short v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Int $up(short v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Int $less$less(short v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Int $greater$greater(short v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(short v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// char operators
	
	@Override
	public boolean $eq$eq(char b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(char b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(char b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(char b)
	{
		return this.value <= b;
	}
	
	@Override
	public boolean $greater(char b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(char b)
	{
		return this.value >= b;
	}
	
	@Override
	public Int $plus(char v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Int $minus(char v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Int $times(char v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Int $div(char v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Int $percent(char v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Int $bar(char v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Int $amp(char v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Int $up(char v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Int $less$less(char v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Int $greater$greater(char v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(char v)
	{
		return this.$eq(this.value >>> v);
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
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISUB)
	public Int $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IMUL)
	public Int $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IDIV)
	public Int $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IREM)
	public Int $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IOR)
	public Int $bar(int v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IAND)
	public Int $amp(int v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IXOR)
	public Int $up(int v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHL)
	public Int $less$less(int v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = ISHR)
	public Int $greater$greater(int v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = IUSHR)
	public Int $greater$greater$greater(int v)
	{
		return this.$eq(this.value >>> v);
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
	@Bytecode(infixOpcode = I2L, postfixOpcode = IF_LCMPNE)
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
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LSUB)
	public Long $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LMUL)
	public Long $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LDIV)
	public Long $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LREM)
	public Long $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LOR)
	public Long $bar(long v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LAND)
	public Long $amp(long v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LXOR)
	public Long $up(long v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LSHL)
	public Int $less$less(long v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LSHR)
	public Int $greater$greater(long v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2L, postfixOpcode = LUSHR)
	public Int $greater$greater$greater(long v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPNE)
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = IF_FCMPNE)
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
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2F, postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return this.$eq(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPNE)
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = IF_DCMPNE)
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
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = I2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return this.$eq(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.intValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.intValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.intValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value <= b.intValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.intValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.intValue();
	}
	
	@Override
	public Int $plus(Number v)
	{
		return this.$eq(this.value + v.intValue());
	}
	
	@Override
	public Int $minus(Number v)
	{
		return this.$eq(this.value - v.intValue());
	}
	
	@Override
	public Int $times(Number v)
	{
		return this.$eq(this.value * v.intValue());
	}
	
	@Override
	public Int $div(Number v)
	{
		return this.$eq(this.value / v.intValue());
	}
	
	@Override
	public Int $percent(Number v)
	{
		return this.$eq(this.value % v.intValue());
	}
	
	@Override
	public Int $bar(Integer v)
	{
		return this.$eq(this.value | v.intValue());
	}
	
	@Override
	public Int $amp(Integer v)
	{
		return this.$eq(this.value & v.intValue());
	}
	
	@Override
	public Int $up(Integer v)
	{
		return this.$eq(this.value ^ v.intValue());
	}
	
	@Override
	public Int $less$less(Integer v)
	{
		return this.$eq(this.value << v.intValue());
	}
	
	@Override
	public Int $greater$greater(Integer v)
	{
		return this.$eq(this.value >> v.intValue());
	}
	
	@Override
	public Int $greater$greater$greater(Integer v)
	{
		return this.$eq(this.value >>> v.intValue());
	}
	
	// string representations
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Integer.toString(this.value);
	}
}
