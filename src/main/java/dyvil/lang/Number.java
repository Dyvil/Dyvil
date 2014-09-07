package dyvil.lang;

import dyvil.lang.annotation.prefix;

public interface Number
{
	// Primitive value getters
	
	public byte byteValue();
	
	public short shortValue();
	
	public char charValue();
	
	public int intValue();
	
	public long longValue();
	
	public float floatValue();
	
	public double doubleValue();
	
	// Unary operators
	
	public @prefix Number neg$();
	
	public @prefix Number inv$();
	
	public Number inc$();
	
	public Number dec$();
	
	public Number sqr$();
	
	public Number rec$();
	
	// byte operators
	
	public Number set$(byte v);
	
	public boolean eq$(byte v);
	
	public boolean ue$(byte v);
	
	public boolean st$(byte v);
	
	public boolean se$(byte v);
	
	public boolean gt$(byte v);
	
	public boolean ge$(byte v);
	
	public Number add$(byte v);
	
	public Number sub$(byte v);
	
	public Number mul$(byte v);
	
	public Number div$(byte v);
	
	public Number mod$(byte v);
	
	public Number and$(byte v);
	
	public Number or$(byte v);
	
	public Number xor$(byte v);
	
	public Number bsl$(byte v);
	
	public Number bsr$(byte v);
	
	public Number usr$(byte v);
	
	// short operators
	
	public Number set$(short v);
	
	public boolean eq$(short v);
	
	public boolean ue$(short v);
	
	public boolean st$(short v);
	
	public boolean se$(short v);
	
	public boolean gt$(short v);
	
	public boolean ge$(short v);
	
	public Number add$(short v);
	
	public Number sub$(short v);
	
	public Number mul$(short v);
	
	public Number div$(short v);
	
	public Number mod$(short v);
	
	public Number and$(short v);
	
	public Number or$(short v);
	
	public Number xor$(short v);
	
	public Number bsl$(short v);
	
	public Number bsr$(short v);
	
	public Number usr$(short v);
	
	// char operators
	
	public Number set$(char v);
	
	public boolean eq$(char v);
	
	public boolean ue$(char v);
	
	public boolean st$(char v);
	
	public boolean se$(char v);
	
	public boolean gt$(char v);
	
	public boolean ge$(char v);
	
	public Number add$(char v);
	
	public Number sub$(char v);
	
	public Number mul$(char v);
	
	public Number div$(char v);
	
	public Number mod$(char v);
	
	public Number and$(char v);
	
	public Number or$(char v);
	
	public Number xor$(char v);
	
	public Number bsl$(char v);
	
	public Number bsr$(char v);
	
	public Number usr$(char v);
	
	// int operators
	
	public Number set$(int v);
	
	public boolean eq$(int v);
	
	public boolean ue$(int v);
	
	public boolean st$(int v);
	
	public boolean se$(int v);
	
	public boolean gt$(int v);
	
	public boolean ge$(int v);
	
	public Number add$(int v);
	
	public Number sub$(int v);
	
	public Number mul$(int v);
	
	public Number div$(int v);
	
	public Number mod$(int v);
	
	public Number and$(int v);
	
	public Number or$(int v);
	
	public Number xor$(int v);
	
	public Number bsl$(int v);
	
	public Number bsr$(int v);
	
	public Number usr$(int v);
	
	// long operators
	
	public Number set$(long v);
	
	public boolean eq$(long v);
	
	public boolean ue$(long v);
	
	public boolean st$(long v);
	
	public boolean se$(long v);
	
	public boolean gt$(long v);
	
	public boolean ge$(long v);
	
	public Number add$(long v);
	
	public Number sub$(long v);
	
	public Number mul$(long v);
	
	public Number div$(long v);
	
	public Number mod$(long v);
	
	public Number and$(long v);
	
	public Number or$(long v);
	
	public Number xor$(long v);
	
	public Number bsl$(long v);
	
	public Number bsr$(long v);
	
	public Number usr$(long v);
	
	// float operators
	
	public Number set$(float v);
	
	public boolean eq$(float v);
	
	public boolean ue$(float v);
	
	public boolean st$(float v);
	
	public boolean se$(float v);
	
	public boolean gt$(float v);
	
	public boolean ge$(float v);
	
	public Number add$(float v);
	
	public Number sub$(float v);
	
	public Number mul$(float v);
	
	public Number div$(float v);
	
	public Number mod$(float v);
	
	// double operators
	
	public Number set$(double v);
	
	public boolean eq$(double v);
	
	public boolean ue$(double v);
	
	public boolean st$(double v);
	
	public boolean se$(double v);
	
	public boolean gt$(double v);
	
	public boolean ge$(double v);
	
	public Number add$(double v);
	
	public Number sub$(double v);
	
	public Number mul$(double v);
	
	public Number div$(double v);
	
	public Number mod$(double v);
}
