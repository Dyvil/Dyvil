package dyvil.lang;

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
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	public Float $minus()
	{
		return this.$eq(-this.value);
	}
	
	@Override
	public Float $tilde()
	{
		// Unsupported
		return this;
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
	public Float sqr()
	{
		return this.$eq(this.value * this.value);
	}
	
	@Override
	public Float sqrt()
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
	
	@Override
	public Float $bar(byte v)
	{
		return this;
	}
	
	@Override
	public Float $amp(byte v)
	{
		return this;
	}
	
	@Override
	public Float $up(byte v)
	{
		return this;
	}
	
	@Override
	public Float $less$less(byte v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater(byte v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater$greater(byte v)
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
	
	@Override
	public Float $bar(short v)
	{
		return this;
	}
	
	@Override
	public Float $amp(short v)
	{
		return this;
	}
	
	@Override
	public Float $up(short v)
	{
		return this;
	}
	
	@Override
	public Float $less$less(short v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater(short v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater$greater(short v)
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
	
	@Override
	public Float $bar(char v)
	{
		return this;
	}
	
	@Override
	public Float $amp(char v)
	{
		return this;
	}
	
	@Override
	public Float $up(char v)
	{
		return this;
	}
	
	@Override
	public Float $less$less(char v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater(char v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater$greater(char v)
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
	public Float $plus(int v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(int v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(int v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(int v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(int v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Float $bar(int v)
	{
		return this;
	}
	
	@Override
	public Float $amp(int v)
	{
		return this;
	}
	
	@Override
	public Float $up(int v)
	{
		return this;
	}
	
	@Override
	public Float $less$less(int v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater(int v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater$greater(int v)
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
	public Float $plus(long v)
	{
		return this.$eq(this.value + v);
	}
	
	@Override
	public Float $minus(long v)
	{
		return this.$eq(this.value - v);
	}
	
	@Override
	public Float $times(long v)
	{
		return this.$eq(this.value * v);
	}
	
	@Override
	public Float $div(long v)
	{
		return this.$eq(this.value / v);
	}
	
	@Override
	public Float $percent(long v)
	{
		return this.$eq(this.value % v);
	}
	
	@Override
	public Float $bar(long v)
	{
		return this;
	}
	
	@Override
	public Float $amp(long v)
	{
		return this;
	}
	
	@Override
	public Float $up(long v)
	{
		return this;
	}
	
	@Override
	public Float $less$less(long v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater(long v)
	{
		return this;
	}
	
	@Override
	public Float $greater$greater$greater(long v)
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
}
