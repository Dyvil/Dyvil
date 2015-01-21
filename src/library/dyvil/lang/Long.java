package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public abstract class Long implements Integer
{
	protected long	value;
	
	protected Long(long value)
	{
		this.value = value;
	}
	
	@Override
	public abstract Long $eq(byte v);
	
	@Override
	public abstract Long $eq(short v);
	
	@Override
	public abstract Long $eq(char v);
	
	@Override
	public abstract Long $eq(int v);
	
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
	@Bytecode(postfixOpcodes = { L2I, I2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2I, I2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2I, I2C })
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = L2I)
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = {})
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = L2F)
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = L2D)
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Bytecode(postfixOpcode = LNEG)
	public Long $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCONST_M1, LXOR })
	public Long $tilde()
	{
		return this.$eq(~this.value);
	}
	
	@Override
	public Long $plus$plus()
	{
		return this.$eq(this.value + 1);
	}
	
	@Override
	public Long $minus$minus()
	{
		return this.$eq(this.value - 1);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP2, LMUL })
	public Long sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = LCONST_1, postfixOpcode = LDIV)
	public Long rec()
	{
		return this.$eq(1L / this.value);
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
	public Long $plus(byte v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Long $minus(byte v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Long $times(byte v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Long $div(byte v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Long $percent(byte v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Long $bar(byte v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Long $amp(byte v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Long $up(byte v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Long $less$less(byte v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Long $greater$greater(byte v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Long $greater$greater$greater(byte v)
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
	public Long $plus(short v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Long $minus(short v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Long $times(short v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Long $div(short v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Long $percent(short v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Long $bar(short v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Long $amp(short v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Long $up(short v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Long $less$less(short v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Long $greater$greater(short v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Long $greater$greater$greater(short v)
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
	public Long $plus(char v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Long $minus(char v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Long $times(char v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Long $div(char v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Long $percent(char v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Long $bar(char v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Long $amp(char v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Long $up(char v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Long $less$less(char v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Long $greater$greater(char v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Long $greater$greater$greater(char v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFNE })
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFEQ })
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFGT })
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFGE })
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFLE })
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LCMP, IFLT })
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LADD })
	public Long $plus(int v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSUB })
	public Long $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LMUL })
	public Long $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LDIV })
	public Long $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LREM })
	public Long $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LOR })
	public Long $bar(int v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LAND })
	public Long $amp(int v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LXOR })
	public Long $up(int v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHL })
	public Long $less$less(int v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHR })
	public Long $greater$greater(int v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LUSHR })
	public Long $greater$greater$greater(int v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFNE })
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFEQ })
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFGE })
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFGT })
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFLE })
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { LCMP, IFLT })
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = LADD)
	public Long $plus(long v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSUB)
	public Long $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LMUL)
	public Long $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LDIV)
	public Long $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LREM)
	public Long $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LOR)
	public Long $bar(long v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LAND)
	public Long $amp(long v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LXOR)
	public Long $up(long v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSHL)
	public Long $less$less(long v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSHR)
	public Long $greater$greater(long v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LUSHR)
	public Long $greater$greater$greater(long v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPL, IFNE })
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPL, IFEQ })
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPG, IFGE })
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPG, IFGT })
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPL, IFLE })
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcodes = { FCMPL, IFLT })
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FADD)
	public Float $plus(float v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return this.$eq(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPL, IFNE })
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPL, IFEQ })
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPG, IFGE })
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPG, IFGT })
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPL, IFLE })
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcodes = { DCMPL, IFLT })
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return this.$eq(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.longValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.longValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.longValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value <= b.longValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.longValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.longValue();
	}
	
	@Override
	public Long $plus(Number v)
	{
		return this.$eq(this.value + v.longValue());
	}
	
	@Override
	public Long $minus(Number v)
	{
		return this.$eq(this.value - v.longValue());
	}
	
	@Override
	public Long $times(Number v)
	{
		return this.$eq(this.value * v.longValue());
	}
	
	@Override
	public Long $div(Number v)
	{
		return this.$eq(this.value / v.longValue());
	}
	
	@Override
	public Long $percent(Number v)
	{
		return this.$eq(this.value % v.longValue());
	}
	
	@Override
	public Long $bar(Integer v)
	{
		return this.$eq(this.value | v.longValue());
	}
	
	@Override
	public Long $amp(Integer v)
	{
		return this.$eq(this.value & v.longValue());
	}
	
	@Override
	public Long $up(Integer v)
	{
		return this.$eq(this.value ^ v.longValue());
	}
	
	@Override
	public Long $less$less(Integer v)
	{
		return this.$eq(this.value << v.longValue());
	}
	
	@Override
	public Long $greater$greater(Integer v)
	{
		return this.$eq(this.value >> v.longValue());
	}
	
	@Override
	public Long $greater$greater$greater(Integer v)
	{
		return this.$eq(this.value >>> v.longValue());
	}
	
	// string representations
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Long.toString(this.value);
	}
}
