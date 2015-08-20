package dyvil.lang;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.literal.LongConvertible;

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
	
	// Unary operators
	
	public Integer $tilde();
	
	// byte operators
	
	public Integer $bslash(byte v);
	
	public Integer $amp(byte v);
	
	public Integer $bar(byte v);
	
	public Integer $up(byte v);
	
	public Integer $lt$lt(byte v);
	
	public Integer $gt$gt(byte v);
	
	public Integer $gt$gt$gt(byte v);
	
	// short operators
	
	public Integer $bslash(short v);
	
	public Integer $amp(short v);
	
	public Integer $bar(short v);
	
	public Integer $up(short v);
	
	public Integer $lt$lt(short v);
	
	public Integer $gt$gt(short v);
	
	public Integer $gt$gt$gt(short v);
	
	// char operators
	
	public Integer $bslash(char v);
	
	public Integer $amp(char v);
	
	public Integer $bar(char v);
	
	public Integer $up(char v);
	
	public Integer $lt$lt(char v);
	
	public Integer $gt$gt(char v);
	
	public Integer $gt$gt$gt(char v);
	
	// int operators
	
	public Integer $bslash(int v);
	
	public Integer $amp(int v);
	
	public Integer $bar(int v);
	
	public Integer $up(int v);
	
	public Integer $lt$lt(int v);
	
	public Integer $gt$gt(int v);
	
	public Integer $gt$gt$gt(int v);
	
	// long operators
	
	public Integer $bslash(long v);
	
	public Integer $amp(long v);
	
	public Integer $bar(long v);
	
	public Integer $up(long v);
	
	public Integer $lt$lt(long v);
	
	public Integer $gt$gt(long v);
	
	public Integer $gt$gt$gt(long v);
	
	// Integer operators
	
	public Integer $bslash(Integer v);
	
	public Integer $amp(Integer v);
	
	public Integer $bar(Integer v);
	
	public Integer $up(Integer v);
	
	public Integer $lt$lt(Integer v);
	
	public Integer $gt$gt(Integer v);
	
	public Integer $gt$gt$gt(Integer v);
}
