package dyvil.lang;

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
	public byte byteValue()
	{
		return this.value;
	}
	
	@Override
	public short shortValue()
	{
		return this.value;
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
	public Byte $minus()
	{
		return Byte.create((byte) -this.value);
	}
	
	@Override
	public Byte $tilde()
	{
		return Byte.create((byte) ~this.value);
	}
	
	@Override
	public Int sqr()
	{
		return Int.create(this.value * this.value);
	}
	
	@Override
	public Byte rec()
	{
		return Byte.create((byte) (1 / this.value));
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
	public Byte $plus(byte v)
	{
		return Byte.create((byte) (this.value + v));
	}
	
	@Override
	public Byte $minus(byte v)
	{
		return Byte.create((byte) (this.value - v));
	}
	
	@Override
	public Byte $times(byte v)
	{
		return Byte.create((byte) (this.value * v));
	}
	
	@Override
	public Byte $div(byte v)
	{
		return Byte.create((byte) (this.value / v));
	}
	
	@Override
	public Byte $percent(byte v)
	{
		return Byte.create((byte) (this.value % v));
	}
	
	@Override
	public Byte $bar(byte v)
	{
		return Byte.create((byte) (this.value | v));
	}
	
	@Override
	public Byte $amp(byte v)
	{
		return Byte.create((byte) (this.value & v));
	}
	
	@Override
	public Byte $up(byte v)
	{
		return Byte.create((byte) (this.value ^ v));
	}
	
	@Override
	public Byte $less$less(byte v)
	{
		return Byte.create((byte) (this.value << v));
	}
	
	@Override
	public Byte $greater$greater(byte v)
	{
		return Byte.create((byte) (this.value >> v));
	}
	
	@Override
	public Byte $greater$greater$greater(byte v)
	{
		return Byte.create((byte) (this.value >>> v));
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
	public Short $plus(short v)
	{
		return Short.create((short) (this.value + v));
	}
	
	@Override
	public Short $minus(short v)
	{
		return Short.create((short) (this.value - v));
	}
	
	@Override
	public Short $times(short v)
	{
		return Short.create((short) (this.value * v));
	}
	
	@Override
	public Short $div(short v)
	{
		return Short.create((short) (this.value / v));
	}
	
	@Override
	public Short $percent(short v)
	{
		return Short.create((short) (this.value % v));
	}
	
	@Override
	public Short $bar(short v)
	{
		return Short.create((short) (this.value | v));
	}
	
	@Override
	public Short $amp(short v)
	{
		return Short.create((short) (this.value & v));
	}
	
	@Override
	public Short $up(short v)
	{
		return Short.create((short) (this.value ^ v));
	}
	
	@Override
	public Short $less$less(short v)
	{
		return Short.create((short) (this.value << v));
	}
	
	@Override
	public Short $greater$greater(short v)
	{
		return Short.create((short) (this.value >> v));
	}
	
	@Override
	public Short $greater$greater$greater(short v)
	{
		return Short.create((short) (this.value >>> v));
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
	public Int $less$less(long v)
	{
		return Int.create(this.value << v);
	}
	
	@Override
	public Int $greater$greater(long v)
	{
		return Int.create(this.value >> v);
	}
	
	@Override
	public Int $greater$greater$greater(long v)
	{
		return Int.create(this.value >>> v);
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
		return this.value == b.byteValue();
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
