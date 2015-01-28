package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public class Float implements Number
{
	protected float	value;
	
	protected Float(float value)
	{
		this.value = value;
	}
	
	public static Float create(float v)
	{
		int i = (int) v;
		if (i >= 0 && v == i && i < ConstPool.tableSize)
		{
			return ConstPool.FLOATS[i];
		}
		return new Float(v);
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
		return Float.create(-this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP, FMUL })
	public Float sqr()
	{
		return Float.create(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = FCONST_1, postfixOpcode = FDIV)
	public Float rec()
	{
		return Float.create(1 / this.value);
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
		return Float.create(this.value + v);
	}
	
	@Override
	public Float $minus(byte v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	public Float $times(byte v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	public Float $div(byte v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	public Float $percent(byte v)
	{
		return Float.create(this.value % v);
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
		return Float.create(this.value + v);
	}
	
	@Override
	public Float $minus(short v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	public Float $times(short v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	public Float $div(short v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	public Float $percent(short v)
	{
		return Float.create(this.value % v);
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
		return Float.create(this.value + v);
	}
	
	@Override
	public Float $minus(char v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	public Float $times(char v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	public Float $div(char v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	public Float $percent(char v)
	{
		return Float.create(this.value % v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPNE })
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPEQ })
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPGE })
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPGT })
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPLE })
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, IF_FCMPLT })
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FADD })
	public Float $plus(int v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FSUB })
	public Float $minus(int v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FMUL })
	public Float $times(int v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FDIV })
	public Float $div(int v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2F, FREM })
	public Float $percent(int v)
	{
		return Float.create(this.value % v);
	}
	
	// long operators
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPNE })
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPEQ })
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPGE })
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPGT })
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPLE })
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, IF_FCMPLT })
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FADD })
	public Float $plus(long v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FSUB })
	public Float $minus(long v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FMUL })
	public Float $times(long v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FDIV })
	public Float $div(long v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { L2F, FREM })
	public Float $percent(long v)
	{
		return Float.create(this.value % v);
	}
	
	// float operators
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPNE)
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPEQ)
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPGE)
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPGT)
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPLE)
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_FCMPLT)
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = FADD)
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPNE)
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPEQ)
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPGE)
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPGT)
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPLE)
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = IF_DCMPLT)
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = F2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
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
		return Float.create(this.value + v.floatValue());
	}
	
	@Override
	public Float $minus(Number v)
	{
		return Float.create(this.value - v.floatValue());
	}
	
	@Override
	public Float $times(Number v)
	{
		return Float.create(this.value * v.floatValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return Float.create(this.value / v.floatValue());
	}
	
	@Override
	public Float $percent(Number v)
	{
		return Float.create(this.value % v.floatValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Float.toString(this.value);
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
