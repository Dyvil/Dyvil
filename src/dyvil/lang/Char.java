package dyvil.lang;

public abstract class Char implements Number
{
	protected char	value;
	
	protected Char(char value)
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
	public Number neg$()
	{
		return this.set$(-this.value);
	}
	
	@Override
	public Number inv$()
	{
		return this.set$(~this.value);
	}
	
	@Override
	public Number inc$()
	{
		return this.set$(this.value + 1);
	}
	
	@Override
	public Number dec$()
	{
		return this.set$(this.value - 1);
	}
	
	@Override
	public Number sqr$()
	{
		return this.set$(this.value * this.value);
	}
	
	@Override
	public Number rec$()
	{
		return this.set$(1 / this.value);
	}
	
	// byte operators
	
	@Override
	public boolean eq$(byte b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(byte b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(byte b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(byte b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(byte b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(byte b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(byte v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(byte v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(byte v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(byte v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(byte v)
	{
		return this.set$(this.value % v);
	}
	
	@Override
	public Number or$(byte v)
	{
		return this.set$(this.value | v);
	}
	
	@Override
	public Number and$(byte v)
	{
		return this.set$(this.value & v);
	}
	
	@Override
	public Number xor$(byte v)
	{
		return this.set$(this.value ^ v);
	}
	
	@Override
	public Number bsl$(byte v)
	{
		return this.set$(this.value << v);
	}
	
	@Override
	public Number bsr$(byte v)
	{
		return this.set$(this.value >> v);
	}
	
	@Override
	public Number usr$(byte v)
	{
		return this.set$(this.value >>> v);
	}
	
	// short operators
	
	@Override
	public boolean eq$(short b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(short b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(short b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(short b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(short b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(short b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(short v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(short v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(short v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(short v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(short v)
	{
		return this.set$(this.value % v);
	}
	
	@Override
	public Number or$(short v)
	{
		return this.set$(this.value | v);
	}
	
	@Override
	public Number and$(short v)
	{
		return this.set$(this.value & v);
	}
	
	@Override
	public Number xor$(short v)
	{
		return this.set$(this.value ^ v);
	}
	
	@Override
	public Number bsl$(short v)
	{
		return this.set$(this.value << v);
	}
	
	@Override
	public Number bsr$(short v)
	{
		return this.set$(this.value >> v);
	}
	
	@Override
	public Number usr$(short v)
	{
		return this.set$(this.value >>> v);
	}
	
	// char operators
	
	@Override
	public boolean eq$(char b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(char b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(char b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(char b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(char b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(char b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(char v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(char v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(char v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(char v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(char v)
	{
		return this.set$(this.value % v);
	}
	
	@Override
	public Number or$(char v)
	{
		return this.set$(this.value | v);
	}
	
	@Override
	public Number and$(char v)
	{
		return this.set$(this.value & v);
	}
	
	@Override
	public Number xor$(char v)
	{
		return this.set$(this.value ^ v);
	}
	
	@Override
	public Number bsl$(char v)
	{
		return this.set$(this.value << v);
	}
	
	@Override
	public Number bsr$(char v)
	{
		return this.set$(this.value >> v);
	}
	
	@Override
	public Number usr$(char v)
	{
		return this.set$(this.value >>> v);
	}
	
	// int operators
	
	@Override
	public boolean eq$(int b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(int b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(int b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(int b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(int b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(int b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(int v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(int v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(int v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(int v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(int v)
	{
		return this.set$(this.value % v);
	}
	
	@Override
	public Number or$(int v)
	{
		return this.set$(this.value | v);
	}
	
	@Override
	public Number and$(int v)
	{
		return this.set$(this.value & v);
	}
	
	@Override
	public Number xor$(int v)
	{
		return this.set$(this.value ^ v);
	}
	
	@Override
	public Number bsl$(int v)
	{
		return this.set$(this.value << v);
	}
	
	@Override
	public Number bsr$(int v)
	{
		return this.set$(this.value >> v);
	}
	
	@Override
	public Number usr$(int v)
	{
		return this.set$(this.value >>> v);
	}
	
	// long operators
	
	@Override
	public boolean eq$(long b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(long b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(long b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(long b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(long b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(long b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(long v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(long v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(long v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(long v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(long v)
	{
		return this.set$(this.value % v);
	}
	
	@Override
	public Number or$(long v)
	{
		return this.set$(this.value | v);
	}
	
	@Override
	public Number and$(long v)
	{
		return this.set$(this.value & v);
	}
	
	@Override
	public Number xor$(long v)
	{
		return this.set$(this.value ^ v);
	}
	
	@Override
	public Number bsl$(long v)
	{
		return this.set$(this.value << v);
	}
	
	@Override
	public Number bsr$(long v)
	{
		return this.set$(this.value >> v);
	}
	
	@Override
	public Number usr$(long v)
	{
		return this.set$(this.value >>> v);
	}
	
	// float operators
	
	@Override
	public boolean eq$(float b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(float b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(float b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(float b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(float b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(float b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(float v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(float v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(float v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(float v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(float v)
	{
		return this.set$(this.value % v);
	}
	
	// double operators
	
	@Override
	public boolean eq$(double b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean ue$(double b)
	{
		return this.value != b;
	}
	
	@Override
	public boolean st$(double b)
	{
		return this.value < b;
	}
	
	@Override
	public boolean se$(double b)
	{
		return this.value == b;
	}
	
	@Override
	public boolean gt$(double b)
	{
		return this.value > b;
	}
	
	@Override
	public boolean ge$(double b)
	{
		return this.value >= b;
	}
	
	@Override
	public Number add$(double v)
	{
		return this.set$(this.value + v);
	}
	
	@Override
	public Number sub$(double v)
	{
		return this.set$(this.value - v);
	}
	
	@Override
	public Number mul$(double v)
	{
		return this.set$(this.value * v);
	}
	
	@Override
	public Number div$(double v)
	{
		return this.set$(this.value / v);
	}
	
	@Override
	public Number mod$(double v)
	{
		return this.set$(this.value % v);
	}
}
