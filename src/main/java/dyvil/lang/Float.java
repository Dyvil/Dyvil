package dyvil.lang;

public abstract class Float implements Number
{
	protected float	value;
	
	protected Float(float value)
	{
		this.value = value;
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
		return (int) this.value;
	}
	
	@Override
	public long longValue()
	{
		return (long) this.value;
	}
	
	@Override
	public float floatValue()
	{
		return (float) this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return (double) this.value;
	}
	
	// Unary operators
	
	@Override
	public Number $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
	public Number $tilde()
	{
		// Unsupported
		return this;
	}
	
	@Override
	public Number $plus$plus()
	{
		return this.$eq(this.value + 1);
	}
	
	@Override
	public Number $minus$minus()
	{
		return this.$eq(this.value - 1);
	}
	
	@Override
	public Number sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	public Number sqrt()
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
	public Number $plus(byte v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(byte v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(byte v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(byte v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(byte v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Number $bar(byte v)
	{
		return this;
	}
	
	@Override
	public Number $amp(byte v)
	{
		return this;
	}
	
	@Override
	public Number $up(byte v)
	{
		return this;
	}
	
	@Override
	public Number $less$less(byte v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater(byte v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater$greater(byte v)
	{
		return this;
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
	public Number $plus(short v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(short v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(short v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(short v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(short v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Number $bar(short v)
	{
		return this;
	}
	
	@Override
	public Number $amp(short v)
	{
		return this;
	}
	
	@Override
	public Number $up(short v)
	{
		return this;
	}
	
	@Override
	public Number $less$less(short v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater(short v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater$greater(short v)
	{
		return this;
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
	public Number $plus(char v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(char v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(char v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(char v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(char v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Number $bar(char v)
	{
		return this;
	}
	
	@Override
	public Number $amp(char v)
	{
		return this;
	}
	
	@Override
	public Number $up(char v)
	{
		return this;
	}
	
	@Override
	public Number $less$less(char v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater(char v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater$greater(char v)
	{
		return this;
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
	public Number $plus(int v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Number $bar(int v)
	{
		return this;
	}
	
	@Override
	public Number $amp(int v)
	{
		return this;
	}
	
	@Override
	public Number $up(int v)
	{
		return this;
	}
	
	@Override
	public Number $less$less(int v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater(int v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater$greater(int v)
	{
		return this;
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
	public Number $plus(long v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Number $bar(long v)
	{
		return this;
	}
	
	@Override
	public Number $amp(long v)
	{
		return this;
	}
	
	@Override
	public Number $up(long v)
	{
		return this;
	}
	
	@Override
	public Number $less$less(long v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater(long v)
	{
		return this;
	}
	
	@Override
	public Number $greater$greater$greater(long v)
	{
		return this;
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
	public Number $plus(float v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(float v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(float v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(float v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(float v)
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
	public Number $plus(double v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Number $minus(double v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Number $times(double v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Number $div(double v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Number $percent(double v)
	{
		return this.$eq(this.value % v);
	}
}
