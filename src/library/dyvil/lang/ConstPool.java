package dyvil.lang;

public class ConstPool
{
	protected static final int		tableSize	= 128;
	
	protected static final Byte[]	BYTES		= new Byte[tableSize];
	protected static final Short[]	SHORTS		= new Short[tableSize];
	protected static final Char[]	CHARS		= new Char[tableSize];
	protected static final Int[]	INTS		= new Int[tableSize];
	protected static final Long[]	LONGS		= new Long[tableSize];
	protected static final Float[]	FLOATS		= new Float[tableSize];
	protected static final Double[]	DOUBLES		= new Double[tableSize];
	
	static
	{
		for (int i = 0; i < tableSize; i++)
		{
			BYTES[i] = new Byte((byte) i);
			SHORTS[i] = new Short((short) i);
			CHARS[i] = new Char((char) i);
			INTS[i] = new Int(i);
			LONGS[i] = new Long(i);
			FLOATS[i] = new Float(i);
			DOUBLES[i] = new Double(i);
		}
	}
}
