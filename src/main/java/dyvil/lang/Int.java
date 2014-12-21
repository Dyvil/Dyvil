package dyvil.lang;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.lang.annotation.bytecode;

public abstract class Int extends Value implements Integer
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
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@bytecode(Opcodes.INEG)
	public Int $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
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
	public Int sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	public Int sqrt()
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
		return this.value == b;
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
		return this.value == b;
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
		return this.value == b;
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
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@bytecode(Opcodes.IADD)
	public Int $plus(int v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	@bytecode(Opcodes.ISUB)
	public Int $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	@bytecode(Opcodes.IMUL)
	public Int $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	@bytecode(Opcodes.IDIV)
	public Int $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	@bytecode(Opcodes.IREM)
	public Int $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	@bytecode(Opcodes.IOR)
	public Int $bar(int v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	@bytecode(Opcodes.IAND)
	public Int $amp(int v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	@bytecode(Opcodes.IXOR)
	public Int $up(int v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	@bytecode(Opcodes.ISHL)
	public Int $less$less(int v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	@bytecode(Opcodes.ISHR)
	public Int $greater$greater(int v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	@bytecode(Opcodes.IUSHR)
	public Int $greater$greater$greater(int v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// long operators
	
	@Override
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	public Long $plus(long v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Long $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Long $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Long $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Long $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Long $bar(long v)
	{
		return this.$eq(this.value | v);
	}
	
	@Override
	public Long $amp(long v)
	{
		return this.$eq(this.value & v);
	}
	
	@Override
	public Long $up(long v)
	{
		return this.$eq(this.value ^ v);
	}
	
	@Override
	public Int $less$less(long v)
	{
		return this.$eq(this.value << v);
	}
	
	@Override
	public Int $greater$greater(long v)
	{
		return this.$eq(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(long v)
	{
		return this.$eq(this.value >>> v);
	}
	
	// float operators
	
	@Override
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	public Float $plus(float v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(float v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(float v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(float v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(float v)
	{
		return this.$eq(this.value % v);
	}
	
	// double operators
	
	@Override
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean $less$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	public Double $plus(double v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Double $minus(double v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Double $times(double v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Double $div(double v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
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
		return this.value == b.intValue();
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
		return this.$eq((int) (this.value + v.intValue()));
	}
	
	@Override
	public Int $minus(Number v)
	{
		return this.$eq((int) (this.value - v.intValue()));
	}
	
	@Override
	public Int $times(Number v)
	{
		return this.$eq((int) (this.value * v.intValue()));
	}
	
	@Override
	public Int $div(Number v)
	{
		return this.$eq((int) (this.value / v.intValue()));
	}
	
	@Override
	public Int $percent(Number v)
	{
		return this.$eq((int) (this.value % v.intValue()));
	}
	
	@Override
	public Int $bar(Integer v)
	{
		return this.$eq((int) (this.value | v.intValue()));
	}
	
	@Override
	public Int $amp(Integer v)
	{
		return this.$eq((int) (this.value & v.intValue()));
	}
	
	@Override
	public Int $up(Integer v)
	{
		return this.$eq((int) (this.value ^ v.intValue()));
	}
	
	@Override
	public Int $less$less(Integer v)
	{
		return this.$eq((int) (this.value << v.intValue()));
	}
	
	@Override
	public Int $greater$greater(Integer v)
	{
		return this.$eq((int) (this.value >> v.intValue()));
	}
	
	@Override
	public Int $greater$greater$greater(Integer v)
	{
		return this.$eq((int) (this.value >>> v.intValue()));
	}
	
	// string representations
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Integer.toString(this.value);
	}
}
