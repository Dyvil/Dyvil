package dyvil.lang.primitive;

public class ConstPool
{
	protected static int		constantTableSize	= 128;
	
	private static ByteConst[] byteConstants = new ByteConst[constantTableSize];
	private static ShortConst[] shortConstants = new ShortConst[constantTableSize];
	private static CharConst[] charConstants = new CharConst[constantTableSize];
	private static IntConst[]	intConstants		= new IntConst[constantTableSize];
	private static LongConst[] longConstants = new LongConst[constantTableSize];
	private static FloatConst[] floatConstants = new FloatConst[constantTableSize];
	private static DoubleConst[] doubleConstants = new DoubleConst[constantTableSize];
	
	static
	{
		for (int i = 0; i < constantTableSize; i++)
		{
			byteConstants[i] = new ByteConst((byte) i);
			shortConstants[i] = new ShortConst((short) i);
			charConstants[i] = new CharConst((char) i);
			intConstants[i] = new IntConst(i);
			longConstants[i] = new LongConst(i);
			floatConstants[i] = new FloatConst(i);
			doubleConstants[i] = new DoubleConst(i);
		}
	}
	
	public static ByteConst getByte(byte value)
	{
		if (value >= 0 && value < constantTableSize)
			return byteConstants[value];
		return new ByteConst(value);
	}
	
	public static ShortConst getShort(short value)
	{
		if (value >= 0 && value < constantTableSize)
			return shortConstants[value];
		return new ShortConst(value);
	}
	
	public static CharConst getChar(char value)
	{
		if (value >= 0 && value < constantTableSize)
			return charConstants[value];
		return new CharConst(value);
	}
	
	public static IntConst getInt(int value)
	{
		if (value >= 0 && value < constantTableSize)
			return intConstants[value];
		return new IntConst(value);
	}
	
	public static LongConst getLong(long value)
	{
		if (value >= 0 && value < constantTableSize)
			return longConstants[(int) value];
		return new LongConst(value);
	}
	
	public static FloatConst getFloat(float value)
	{
		int i = (int) value;
		if (value >= 0 && value == i && value < constantTableSize)
			return floatConstants[i];
		return new FloatConst(value);
	}
	
	public static DoubleConst getDouble(double value)
	{
		int i = (int) value;
		if (value >= 0 && value == i && value < constantTableSize)
			return doubleConstants[i];
		return new DoubleConst(value);
	}
}
