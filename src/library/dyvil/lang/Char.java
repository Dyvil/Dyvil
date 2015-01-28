package dyvil.lang;

public class Char implements Integer
{
	protected char	value;
	
	protected Char(char value)
	{
		this.value = value;
	}
	
	public static Char create(char v)
	{
		if (v >= 0 && v < ConstPool.tableSize)
		{
			return ConstPool.CHARS[v];
		}
		return new Char(v);
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
		return this.value;
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
	public Char $minus()
	{
		return Char.create((char) -this.value);
	}
	
	@Override
	public Char $tilde()
	{
		return Char.create((char) ~this.value);
	}
	
	@Override
	public Int sqr()
	{
		return Int.create(this.value * this.value);
	}
	
	@Override
	public Char rec()
	{
		return Char.create((char) (1 / this.value));
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
	public Char $plus(byte v)
	{
		return Char.create((char) (this.value + v));
	}
	
	@Override
	public Char $minus(byte v)
	{
		return Char.create((char) (this.value - v));
	}
	
	@Override
	public Char $times(byte v)
	{
		return Char.create((char) (this.value * v));
	}
	
	@Override
	public Char $div(byte v)
	{
		return Char.create((char) (this.value / v));
	}
	
	@Override
	public Char $percent(byte v)
	{
		return Char.create((char) (this.value % v));
	}
	
	@Override
	public Char $bar(byte v)
	{
		return Char.create((char) (this.value | v));
	}
	
	@Override
	public Char $amp(byte v)
	{
		return Char.create((char) (this.value & v));
	}
	
	@Override
	public Char $up(byte v)
	{
		return Char.create((char) (this.value ^ v));
	}
	
	@Override
	public Char $less$less(byte v)
	{
		return Char.create((char) (this.value << v));
	}
	
	@Override
	public Char $greater$greater(byte v)
	{
		return Char.create((char) (this.value >> v));
	}
	
	@Override
	public Char $greater$greater$greater(byte v)
	{
		return Char.create((char) (this.value >>> v));
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
	public Char $plus(short v)
	{
		return Char.create((char) (this.value + v));
	}
	
	@Override
	public Char $minus(short v)
	{
		return Char.create((char) (this.value - v));
	}
	
	@Override
	public Char $times(short v)
	{
		return Char.create((char) (this.value * v));
	}
	
	@Override
	public Char $div(short v)
	{
		return Char.create((char) (this.value / v));
	}
	
	@Override
	public Char $percent(short v)
	{
		return Char.create((char) (this.value % v));
	}
	
	@Override
	public Char $bar(short v)
	{
		return Char.create((char) (this.value | v));
	}
	
	@Override
	public Char $amp(short v)
	{
		return Char.create((char) (this.value & v));
	}
	
	@Override
	public Char $up(short v)
	{
		return Char.create((char) (this.value ^ v));
	}
	
	@Override
	public Char $less$less(short v)
	{
		return Char.create((char) (this.value << v));
	}
	
	@Override
	public Char $greater$greater(short v)
	{
		return Char.create((char) (this.value >> v));
	}
	
	@Override
	public Char $greater$greater$greater(short v)
	{
		return Char.create((char) (this.value >>> v));
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
	public Char $plus(char v)
	{
		return Char.create((char) (this.value + v));
	}
	
	@Override
	public Char $minus(char v)
	{
		return Char.create((char) (this.value - v));
	}
	
	@Override
	public Char $times(char v)
	{
		return Char.create((char) (this.value * v));
	}
	
	@Override
	public Char $div(char v)
	{
		return Char.create((char) (this.value / v));
	}
	
	@Override
	public Char $percent(char v)
	{
		return Char.create((char) (this.value % v));
	}
	
	@Override
	public Char $bar(char v)
	{
		return Char.create((char) (this.value | v));
	}
	
	@Override
	public Char $amp(char v)
	{
		return Char.create((char) (this.value & v));
	}
	
	@Override
	public Char $up(char v)
	{
		return Char.create((char) (this.value ^ v));
	}
	
	@Override
	public Char $less$less(char v)
	{
		return Char.create((char) (this.value << v));
	}
	
	@Override
	public Char $greater$greater(char v)
	{
		return Char.create((char) (this.value >> v));
	}
	
	@Override
	public Char $greater$greater$greater(char v)
	{
		return Char.create((char) (this.value >>> v));
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
	public Int $plus(int v)
	{
		return Int.create(this.value + v);
	}
	
	@Override
	public Int $minus(int v)
	{
		return Int.create(this.value - v);
	}
	
	@Override
	public Int $times(int v)
	{
		return Int.create(this.value * v);
	}
	
	@Override
	public Int $div(int v)
	{
		return Int.create(this.value / v);
	}
	
	@Override
	public Int $percent(int v)
	{
		return Int.create(this.value % v);
	}
	
	@Override
	public Int $bar(int v)
	{
		return Int.create(this.value | v);
	}
	
	@Override
	public Int $amp(int v)
	{
		return Int.create(this.value & v);
	}
	
	@Override
	public Int $up(int v)
	{
		return Int.create(this.value ^ v);
	}
	
	@Override
	public Int $less$less(int v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	public Int $greater$greater(int v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(int v)
	{
		return Int.create(this.value >>> v);
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
		return Long.create(this.value + v);
	}
	
	@Override
	public Long $minus(long v)
	{
		return Long.create(this.value - v);
	}
	
	@Override
	public Long $times(long v)
	{
		return Long.create(this.value * v);
	}
	
	@Override
	public Long $div(long v)
	{
		return Long.create(this.value / v);
	}
	
	@Override
	public Long $percent(long v)
	{
		return Long.create(this.value % v);
	}
	
	@Override
	public Long $bar(long v)
	{
		return Long.create(this.value | v);
	}
	
	@Override
	public Long $amp(long v)
	{
		return Long.create(this.value & v);
	}
	
	@Override
	public Long $up(long v)
	{
		return Long.create(this.value ^ v);
	}
	
	@Override
	public Char $less$less(long v)
	{
		return Char.create((char) (this.value << v));
	}
	
	@Override
	public Char $greater$greater(long v)
	{
		return Char.create((char) (this.value >> v));
	}
	
	@Override
	public Char $greater$greater$greater(long v)
	{
		return Char.create((char) (this.value >>> v));
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
		return Float.create(this.value + v);
	}
	
	@Override
	public Float $minus(float v)
	{
		return Float.create(this.value - v);
	}
	
	@Override
	public Float $times(float v)
	{
		return Float.create(this.value * v);
	}
	
	@Override
	public Float $div(float v)
	{
		return Float.create(this.value / v);
	}
	
	@Override
	public Float $percent(float v)
	{
		return Float.create(this.value % v);
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
		return Double.create(this.value + v);
	}
	
	@Override
	public Double $minus(double v)
	{
		return Double.create(this.value - v);
	}
	
	@Override
	public Double $times(double v)
	{
		return Double.create(this.value * v);
	}
	
	@Override
	public Double $div(double v)
	{
		return Double.create(this.value / v);
	}
	
	@Override
	public Double $percent(double v)
	{
		return Double.create(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number b)
	{
		return this.value == b.charValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.charValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.charValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value == b.charValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.charValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.charValue();
	}
	
	@Override
	public Char $plus(Number v)
	{
		return Char.create((char) (this.value + v.charValue()));
	}
	
	@Override
	public Char $minus(Number v)
	{
		return Char.create((char) (this.value - v.charValue()));
	}
	
	@Override
	public Char $times(Number v)
	{
		return Char.create((char) (this.value * v.charValue()));
	}
	
	@Override
	public Char $div(Number v)
	{
		return Char.create((char) (this.value / v.charValue()));
	}
	
	@Override
	public Char $percent(Number v)
	{
		return Char.create((char) (this.value % v.charValue()));
	}
	
	@Override
	public Char $bar(Integer v)
	{
		return Char.create((char) (this.value | v.charValue()));
	}
	
	@Override
	public Char $amp(Integer v)
	{
		return Char.create((char) (this.value & v.charValue()));
	}
	
	@Override
	public Char $up(Integer v)
	{
		return Char.create((char) (this.value ^ v.charValue()));
	}
	
	@Override
	public Char $less$less(Integer v)
	{
		return Char.create((char) (this.value << v.charValue()));
	}
	
	@Override
	public Char $greater$greater(Integer v)
	{
		return Char.create((char) (this.value >> v.charValue()));
	}
	
	@Override
	public Char $greater$greater$greater(Integer v)
	{
		return Char.create((char) (this.value >>> v.charValue()));
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Character.toString(this.value);
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
		return this.value == other.charValue();
	}
}
