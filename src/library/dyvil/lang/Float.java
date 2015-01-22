package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public abstract class Float implements Number
{
	protected float	value;
	
	protected Float(float value)
	{
		this.value = value;
	}
	
	@Override
	public abstract Float $eq(byte v);
	
	@Override
	public abstract Float $eq(short v);
	
	@Override
	public abstract Float $eq(char v);
	
	@Override
	public abstract Float $eq(int v);
	
	@Override
	public abstract Float $eq(long v);
	
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
	@Bytecode(postfixOpcode = F2B)
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = F2S)
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = F2C)
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = F2I)
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = F2L)
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = {})
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = F2D)
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Bytecode(postfixOpcode = FNEG)
	public Float $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
	public Float $plus$plus()
	{
		return this.$eq(this.value + 1);
	}
	
	@Override
	public Float $minus$minus()
	{
		return this.$eq(this.value - 1);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP, FMUL })
	public Float sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = FCONST_1, postfixOpcode = FDIV)
	public Float rec()
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
	public Float $plus(byte v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(byte v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(byte v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(byte v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(byte v)
	{
		return this.$eq(this.value % v);
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
	public Float $plus(short v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(short v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(short v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(short v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(short v)
	{
		return this.$eq(this.value % v);
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
	public Float $plus(char v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(char v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(char v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(char v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(char v)
	{
		return this.$eq(this.value % v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPL, IFNE })
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPL, IFEQ })
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPG, IFGE })
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPG, IFGT })
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPL, IFLE })
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FCMPL, IFLT })
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FADD })
	public Float $plus(int v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FSUB })
	public Float $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FMUL })
	public Float $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FDIV })
	public Float $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FREM })
	public Float $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	// long operators
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPL, IFNE })
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPL, IFEQ })
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPG, IFGE })
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPG, IFGT })
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPL, IFLE })
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FCMPL, IFLT })
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FADD })
	public Float $plus(long v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FSUB })
	public Float $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FMUL })
	public Float $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FDIV })
	public Float $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FREM })
	public Float $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	// float operators
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPL, IFNE })
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPL, IFEQ })
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPG, IFGE })
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPG, IFGT })
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPL, IFLE })
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { FCMPL, IFLT })
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = FADD)
	public Float $plus(float v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return this.$eq(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPL, IFNE })
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPL, IFEQ })
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPG, IFGE })
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPG, IFGT })
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPL, IFLE })
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcodes = { DCMPL, IFLT })
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return this.$eq(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.floatValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.floatValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.floatValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value <= b.floatValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.floatValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.floatValue();
	}
	
	@Override
	public Float $plus(Number v)
	{
		return this.$eq(this.value + v.floatValue());
	}
	
	@Override
	public Float $minus(Number v)
	{
		return this.$eq(this.value - v.floatValue());
	}
	
	@Override
	public Float $times(Number v)
	{
		return this.$eq(this.value * v.floatValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return this.$eq(this.value / v.floatValue());
	}
	
	@Override
	public Float $percent(Number v)
	{
		return this.$eq(this.value % v.floatValue());
	}
	
	// string representations
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Float.toString(this.value);
	}
}
