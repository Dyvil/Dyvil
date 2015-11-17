package dyvil.lang;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;

import dyvil.annotation._internal.prefix;

@IntConvertible
@LongConvertible
public interface Integer extends Number
{
	static Int apply(int v)
	{
		return Int.apply(v);
	}
	
	static Long apply(long v)
	{
		return Long.apply(v);
	}
	
	@prefix Integer $tilde();
	
	Integer $bslash(Integer v);
	
	Integer $amp(Integer v);
	
	Integer $bar(Integer v);
	
	Integer $up(Integer v);
	
	Integer $lt$lt(Integer v);
	
	Integer $gt$gt(Integer v);
	
	Integer $gt$gt$gt(Integer v);
}
