package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public class Long implements Integer
{
	protected long	value;
	
	protected Long(long value)
	{
		this.value = value;
	}
	
	public static Long create(long value)
	{
		if (value >= 0 && value < ConstPool.tableSize)
		{
			return ConstPool.LONGS[(int) value];
		}
		return new Long(value);
	}
	
	@Override
	@Bytecode(postfixOpcode = L2B)
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = L2S)
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Bytecode(postfixOpcode = L2C)
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
	@Bytecode
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
		return Long.create(-this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = LBIN)
	public Long $tilde()
	{
		return Long.create(~this.value);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { DUP2, LMUL })
	public Long sqr()
	{
		return Long.create(this.value * this.value);
	}
	
	@Override
	@Bytecode(prefixOpcode = LCONST_1, postfixOpcode = LDIV)
	public Long rec()
	{
		return Long.create(1L / this.value);
	}
	
	// byte operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPNE })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPEQ })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGE })
	public boolean $less(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGT })
	public boolean $less$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLE })
	public boolean $greater(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLT })
	public boolean $greater$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LADD })
	public Long $plus(byte v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSUB })
	public Long $minus(byte v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LMUL })
	public Long $times(byte v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LDIV })
	public Long $div(byte v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LREM })
	public Long $percent(byte v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LAND })
	public Long $amp(byte v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LOR })
	public Long $bar(byte v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LXOR })
	public Long $up(byte v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHL })
	public Long $less$less(byte v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHR })
	public Long $greater$greater(byte v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LUSHR })
	public Long $greater$greater$greater(byte v)
	{
		return Long.create(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPNE })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPEQ })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGE })
	public boolean $less(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGT })
	public boolean $less$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLE })
	public boolean $greater(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLT })
	public boolean $greater$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LADD })
	public Long $plus(short v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSUB })
	public Long $minus(short v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LMUL })
	public Long $times(short v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LDIV })
	public Long $div(short v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LREM })
	public Long $percent(short v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LAND })
	public Long $amp(short v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LOR })
	public Long $bar(short v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LXOR })
	public Long $up(short v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHL })
	public Long $less$less(short v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHR })
	public Long $greater$greater(short v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LUSHR })
	public Long $greater$greater$greater(short v)
	{
		return Long.create(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPNE })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPEQ })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGE })
	public boolean $less(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGT })
	public boolean $less$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLE })
	public boolean $greater(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLT })
	public boolean $greater$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LADD })
	public Long $plus(char v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSUB })
	public Long $minus(char v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LMUL })
	public Long $times(char v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LDIV })
	public Long $div(char v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LREM })
	public Long $percent(char v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LAND })
	public Long $amp(char v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LOR })
	public Long $bar(char v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LXOR })
	public Long $up(char v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHL })
	public Long $less$less(char v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHR })
	public Long $greater$greater(char v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LUSHR })
	public Long $greater$greater$greater(char v)
	{
		return Long.create(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPNE })
	public boolean $eq$eq(int b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPEQ })
	public boolean $bang$eq(int b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGT })
	public boolean $less(int b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPGE })
	public boolean $less$eq(int b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLE })
	public boolean $greater(int b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, IF_LCMPLT })
	public boolean $greater$eq(int b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LADD })
	public Long $plus(int v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSUB })
	public Long $minus(int v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LMUL })
	public Long $times(int v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LDIV })
	public Long $div(int v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LREM })
	public Long $percent(int v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LAND })
	public Long $amp(int v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LOR })
	public Long $bar(int v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LXOR })
	public Long $up(int v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHL })
	public Long $less$less(int v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LSHR })
	public Long $greater$greater(int v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcodes = { I2L, LUSHR })
	public Long $greater$greater$greater(int v)
	{
		return Long.create(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPNE)
	public boolean $eq$eq(long b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPEQ)
	public boolean $bang$eq(long b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPGE)
	public boolean $less(long b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPGT)
	public boolean $less$eq(long b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPLE)
	public boolean $greater(long b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(postfixOpcode = IF_LCMPLT)
	public boolean $greater$eq(long b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(postfixOpcode = LADD)
	public Long $plus(long v)
	{
		return Long.create(this.value + v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSUB)
	public Long $minus(long v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LMUL)
	public Long $times(long v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LDIV)
	public Long $div(long v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LREM)
	public Long $percent(long v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LAND)
	public Long $amp(long v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LOR)
	public Long $bar(long v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LXOR)
	public Long $up(long v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSHL)
	public Long $less$less(long v)
	{
		return Long.create(this.value << v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LSHR)
	public Long $greater$greater(long v)
	{
		return Long.create(this.value >> v);
	}
	
	@Override
	@Bytecode(postfixOpcode = LUSHR)
	public Long $greater$greater$greater(long v)
	{
		return Long.create(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPNE)
	public boolean $eq$eq(float b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPEQ)
	public boolean $bang$eq(float b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPGE)
	public boolean $less(float b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPGT)
	public boolean $less$eq(float b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPLE)
	public boolean $greater(float b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = IF_FCMPLT)
	public boolean $greater$eq(float b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FADD)
	public Float $plus(float v)
	{
		return Float.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FSUB)
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FMUL)
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FDIV)
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2F, postfixOpcode = FREM)
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
	}
	
	// double operators
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPNE)
	public boolean $eq$eq(double b)
	{
		return this.value == b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPEQ)
	public boolean $bang$eq(double b)
	{
		return this.value != b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPGE)
	public boolean $less(double b)
	{
		return this.value < b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPGT)
	public boolean $less$eq(double b)
	{
		return this.value <= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPLE)
	public boolean $greater(double b)
	{
		return this.value > b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = IF_DCMPLT)
	public boolean $greater$eq(double b)
	{
		return this.value >= b;
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DADD)
	public Double $plus(double v)
	{
		return Double.create(this.value + v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DSUB)
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DMUL)
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DDIV)
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	@Bytecode(infixOpcode = L2D, postfixOpcode = DREM)
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
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
		return Long.create(this.value + v.longValue());
	}
	
	@Override
	public Long $minus(Number v)
	{
		return Long.create(this.value - v.longValue());
	}
	
	@Override
	public Long $times(Number v)
	{
		return Long.create(this.value * v.longValue());
	}
	
	@Override
	public Long $div(Number v)
	{
		return Long.create(this.value / v.longValue());
	}
	
	@Override
	public Long $percent(Number v)
	{
		return Long.create(this.value % v.longValue());
	}
	
	@Override
	public Long $bar(Integer v)
	{
		return Long.create(this.value | v.longValue());
	}
	
	@Override
	public Long $amp(Integer v)
	{
		return Long.create(this.value & v.longValue());
	}
	
	@Override
	public Long $up(Integer v)
	{
		return Long.create(this.value ^ v.longValue());
	}
	
	@Override
	public Long $less$less(Integer v)
	{
		return Long.create(this.value << v.longValue());
	}
	
	@Override
	public Long $greater$greater(Integer v)
	{
		return Long.create(this.value >> v.longValue());
	}
	
	@Override
	public Long $greater$greater$greater(Integer v)
	{
		return Long.create(this.value >>> v.longValue());
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Long.toString(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return (int) ((this.value >>> 32) ^ this.value);
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
		return this.value == other.longValue();
	}
}
