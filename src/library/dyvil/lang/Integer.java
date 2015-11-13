package dyvil.lang;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;

import dyvil.annotation._internal.prefix;

@IntConvertible
@LongConvertible
public interface Integer extends Number
{
	public static Int apply(int v)
	{
		return Int.apply(v);
	}
	
	public static Long apply(long v)
	{
		return Long.apply(v);
	}
	
	public @prefix Integer $tilde();
	
	public Integer $bslash(Integer v);
	
	public Integer $amp(Integer v);
	
	public Integer $bar(Integer v);
	
	public Integer $up(Integer v);
	
	public Integer $lt$lt(Integer v);
	
	public Integer $gt$gt(Integer v);
	
	public Integer $gt$gt$gt(Integer v);
}
