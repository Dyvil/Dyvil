package dyvil.lang;

public class Short implements Integer
{
	protected short	value;
	
	protected Short(short value)
	{
		this.value = value;
	}
	
	public static Short create(short v)
	{
		if (v >= 0 && v < ConstPool.tableSize)
		{
			return ConstPool.SHORTS[v];
		}
		return new Short(v);
	}
	
	@Override
	public byte byteValue()
	{
		return (byte) this.value;
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
	public Short $minus()
	{
		return Short.create((short) -this.value);
	}
	
	@Override
	public Short $tilde()
	{
		return Short.create((short) ~this.value);
	}
	
	@Override
	public Int sqr()
	{
		return Int.create(this.value * this.value);
	}
	
	@Override
	public Short rec()
	{
		return Short.create((short) (1 / this.value));
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
	public Short $plus(byte v)
	{
		return Short.create((short) (this.value + v));
	}
	
	@Override
	public Short $minus(byte v)
	{
		return Short.create((short) (this.value - v));
	}
	
	@Override
	public Short $times(byte v)
	{
		return Short.create((short) (this.value * v));
	}
	
	@Override
	public Short $div(byte v)
	{
		return Short.create((short) (this.value / v));
	}
	
	@Override
	public Short $percent(byte v)
	{
		return Short.create((short) (this.value % v));
	}
	
	@Override
	public Short $bar(byte v)
	{
		return Short.create((short) (this.value | v));
	}
	
	@Override
	public Short $amp(byte v)
	{
		return Short.create((short) (this.value & v));
	}
	
	@Override
	public Short $up(byte v)
	{
		return Short.create((short) (this.value ^ v));
	}
	
	@Override
	public Short $less$less(byte v)
	{
		return Short.create((short) (this.value << v));
	}
	
	@Override
	public Short $greater$greater(byte v)
	{
		return Short.create((short) (this.value >> v));
	}
	
	@Override
	public Short $greater$greater$greater(byte v)
	{
		return Short.create((short) (this.value >>> v));
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
		return this.value == b.shortValue();
	}
	
	@Override
	public boolean $bang$eq(Number b)
	{
		return this.value != b.shortValue();
	}
	
	@Override
	public boolean $less(Number b)
	{
		return this.value < b.shortValue();
	}
	
	@Override
	public boolean $less$eq(Number b)
	{
		return this.value == b.shortValue();
	}
	
	@Override
	public boolean $greater(Number b)
	{
		return this.value > b.shortValue();
	}
	
	@Override
	public boolean $greater$eq(Number b)
	{
		return this.value >= b.shortValue();
	}
	
	@Override
	public Short $plus(Number v)
	{
		return Short.create((short) (this.value + v.shortValue()));
	}
	
	@Override
	public Short $minus(Number v)
	{
		return Short.create((short) (this.value - v.shortValue()));
	}
	
	@Override
	public Short $times(Number v)
	{
		return Short.create((short) (this.value * v.shortValue()));
	}
	
	@Override
	public Short $div(Number v)
	{
		return Short.create((short) (this.value / v.shortValue()));
	}
	
	@Override
	public Short $percent(Number v)
	{
		return Short.create((short) (this.value % v.shortValue()));
	}
	
	@Override
	public Short $bar(Integer v)
	{
		return Short.create((short) (this.value | v.shortValue()));
	}
	
	@Override
	public Short $amp(Integer v)
	{
		return Short.create((short) (this.value & v.shortValue()));
	}
	
	@Override
	public Short $up(Integer v)
	{
		return Short.create((short) (this.value ^ v.shortValue()));
	}
	
	@Override
	public Short $less$less(Integer v)
	{
		return Short.create((short) (this.value << v.shortValue()));
	}
	
	@Override
	public Short $greater$greater(Integer v)
	{
		return Short.create((short) (this.value >> v.shortValue()));
	}
	
	@Override
	public Short $greater$greater$greater(Integer v)
	{
		return Short.create((short) (this.value >>> v.shortValue()));
	}
	
	// Object methods
	
	@Override
	public java.lang.String toString()
	{
		return java.lang.Short.toString(this.value);
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
		return this.value == other.shortValue();
	}
}
