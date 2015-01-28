package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public class Double implements Number
{
	protected double	value;
	
	protected Double(double value)
	{
		this.value = value;
	}
	
	public static Double create(double v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.DOUBLES[i];
		}
		return new Double(v);
	}
	
	@Override
	@Bytecode(postfixOpcode = D2B)
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = D2S)
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = D2C)
	public char charValue()
	{
		return (char) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = D2I)
	public int intValue()
	{
		return (int) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = D2L)
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = D2F)
	public float floatValue()
	{
		return (float) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcodes = {})
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Bytecode(postfixOpcode = DNEG)
	public Double $minus()
	{
		return Double.create(-this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP2, DMUL })
	public Double sqr()
	{
		return Double.create(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = DCONST_1, postfixOpcode = DDIV)
	public Double rec()
	{
		return Double.create(1 / this.value);
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
	public Double $plus(byte v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	public Double $minus(byte v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	public Double $times(byte v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	public Double $div(byte v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	public Double $percent(byte v)
	{
		return Double.create(this.value % v);
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
	public Double $plus(short v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	public Double $minus(short v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	public Double $times(short v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	public Double $div(short v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	public Double $percent(short v)
	{
		return Double.create(this.value % v);
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
	public Double $plus(char v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	public Double $minus(char v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	public Double $times(char v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	public Double $div(char v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	public Double $percent(char v)
	{
		return Double.create(this.value % v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPNE })
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPEQ })
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPGE })
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPGT })
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPLE })
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, IF_DCMPLT })
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, DADD })
	public Double $plus(int v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, DSUB })
	public Double $minus(int v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, DMUL })
	public Double $times(int v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, DDIV })
	public Double $div(int v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2D, DREM })
	public Double $percent(int v)
	{
		return Double.create(this.value % v);
	}
	
	// long operators
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPNE })
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPEQ })
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPGE })
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPGT })
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPLE })
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, IF_DCMPLT })
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, DADD })
	public Double $plus(long v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, DSUB })
	public Double $minus(long v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, DMUL })
	public Double $times(long v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, DDIV })
	public Double $div(long v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2D, DREM })
	public Double $percent(long v)
	{
		return Double.create(this.value % v);
	}
	
	// float operators
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPNE })
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPEQ })
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPGE })
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPGT })
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPLE })
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, IF_DCMPLT })
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, DADD })
	public Double $plus(float v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, DSUB })
	public Double $minus(float v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, DMUL })
	public Double $times(float v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, DDIV })
	public Double $div(float v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { F2D, DREM })
	public Double $percent(float v)
	{
		return Double.create(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPNE)
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPEQ)
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPGE)
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPGT)
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPLE)
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_DCMPLT)
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.doubleValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.doubleValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.doubleValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value <= b.doubleValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.doubleValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.doubleValue();
	}
	
	@Override
	public Double $plus(Number v)
	{
		return Double.create(this.value + v.doubleValue());
	}
	
	@Override
	public Double $minus(Number v)
	{
		return Double.create(this.value - v.doubleValue());
	}
	
	@Override
	public Double $times(Number v)
	{
		return Double.create(this.value * v.doubleValue());
	}
	
	@Override
	public Double $div(Number v)
	{
		return Double.create(this.value / v.doubleValue());
	}
	
	@Override
	public Double $percent(Number v)
	{
		return Double.create(this.value % v.doubleValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Double.toString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		long bits = java.lang.Double.doubleToLongBits(this.value);
		return (int) (bits ^ (bits >>> 32));
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
		return this.value == other.doubleValue();
	}
}
