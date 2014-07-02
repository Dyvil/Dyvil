package dyvil.lang;

public abstract class Number
{
	// Primitive value getters
	
	public abstract byte byteValue();
	
	public abstract short shortValue();
	
	public abstract char charValue();
	
	public abstract int intValue();
	
	public abstract long longValue();
	
	public abstract float floatValue();
	
	public abstract double doubleValue();
	
	// Unary operators
	
	public abstract Number neg$();
	
	public abstract Number inv$();
	
	public abstract Number inc$();
	
	public abstract Number dec$();
	
	public abstract Number sqr$();
	
	public abstract Number rec$();
	
	// byte operators
	
	public abstract Number set$(byte v);
	
	public abstract boolean eq$(byte v);
	
	public abstract boolean ue$(byte v);
	
	public abstract boolean st$(byte v);
	
	public abstract boolean se$(byte v);
	
	public abstract boolean gt$(byte v);
	
	public abstract boolean ge$(byte v);
	
	public abstract Number add$(byte v);
	
	public abstract Number sub$(byte v);
	
	public abstract Number mul$(byte v);
	
	public abstract Number div$(byte v);
	
	public abstract Number mod$(byte v);
	
	public abstract Number bsl$(byte v);
	
	public abstract Number bsr$(byte v);
	
	public abstract Number usr$(byte v);
	
	// short operators
	
	public abstract Number set$(short v);
	
	public abstract boolean eq$(short v);
	
	public abstract boolean ue$(short v);
	
	public abstract boolean st$(short v);
	
	public abstract boolean se$(short v);
	
	public abstract boolean gt$(short v);
	
	public abstract boolean ge$(short v);
	
	public abstract Number add$(short v);
	
	public abstract Number sub$(short v);
	
	public abstract Number mul$(short v);
	
	public abstract Number div$(short v);
	
	public abstract Number mod$(short v);
	
	public abstract Number bsl$(short v);
	
	public abstract Number bsr$(short v);
	
	public abstract Number usr$(short v);
	
	// char operators
	
	public abstract Number set$(char v);
	
	public abstract boolean eq$(char v);
	
	public abstract boolean ue$(char v);
	
	public abstract boolean st$(char v);
	
	public abstract boolean se$(char v);
	
	public abstract boolean gt$(char v);
	
	public abstract boolean ge$(char v);
	
	public abstract Number add$(char v);
	
	public abstract Number sub$(char v);
	
	public abstract Number mul$(char v);
	
	public abstract Number div$(char v);
	
	public abstract Number mod$(char v);
	
	public abstract Number bsl$(char v);
	
	public abstract Number bsr$(char v);
	
	public abstract Number usr$(char v);
	
	// int operators
	
	public abstract Number set$(int v);
	
	public abstract boolean eq$(int v);
	
	public abstract boolean ue$(int v);
	
	public abstract boolean st$(int v);
	
	public abstract boolean se$(int v);
	
	public abstract boolean gt$(int v);
	
	public abstract boolean ge$(int v);
	
	public abstract Number add$(int v);
	
	public abstract Number sub$(int v);
	
	public abstract Number mul$(int v);
	
	public abstract Number div$(int v);
	
	public abstract Number mod$(int v);
	
	public abstract Number bsl$(int v);
	
	public abstract Number bsr$(int v);
	
	public abstract Number usr$(int v);
	
	// long operators
	
	public abstract Number set$(long v);
	
	public abstract boolean eq$(long v);
	
	public abstract boolean ue$(long v);
	
	public abstract boolean st$(long v);
	
	public abstract boolean se$(long v);
	
	public abstract boolean gt$(long v);
	
	public abstract boolean ge$(long v);
	
	public abstract Number add$(long v);
	
	public abstract Number sub$(long v);
	
	public abstract Number mul$(long v);
	
	public abstract Number div$(long v);
	
	public abstract Number mod$(long v);
	
	public abstract Number bsl$(long v);
	
	public abstract Number bsr$(long v);
	
	public abstract Number usr$(long v);
	
	// float operators
	
	public abstract Number set$(float v);
	
	public abstract boolean eq$(float v);
	
	public abstract boolean ue$(float v);
	
	public abstract boolean st$(float v);
	
	public abstract boolean se$(float v);
	
	public abstract boolean gt$(float v);
	
	public abstract boolean ge$(float v);
	
	public abstract Number add$(float v);
	
	public abstract Number sub$(float v);
	
	public abstract Number mul$(float v);
	
	public abstract Number div$(float v);
	
	public abstract Number mod$(float v);
	
	// double operators
	
	public abstract Number set$(double v);
	
	public abstract boolean eq$(double v);
	
	public abstract boolean ue$(double v);
	
	public abstract boolean st$(double v);
	
	public abstract boolean se$(double v);
	
	public abstract boolean gt$(double v);
	
	public abstract boolean ge$(double v);
	
	public abstract Number add$(double v);
	
	public abstract Number sub$(double v);
	
	public abstract Number mul$(double v);
	
	public abstract Number div$(double v);
	
	public abstract Number mod$(double v);
}
