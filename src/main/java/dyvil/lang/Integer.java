package dyvil.lang;

public interface Integer extends Number
{
	public Integer $eq(Integer v);
	
	// byte operators
	
	public Number $amp(byte v);
	
	public Number $bar(byte v);
	
	public Number $up(byte v);
	
	public Number $less$less(byte v);
	
	public Number $greater$greater(byte v);
	
	public Number $greater$greater$greater(byte v);
	
	// short operators
	
	public Number $amp(short v);
	
	public Number $bar(short v);
	
	public Number $up(short v);
	
	public Number $less$less(short v);
	
	public Number $greater$greater(short v);
	
	public Number $greater$greater$greater(short v);
	
	// char operators
	
	public Number $amp(char v);
	
	public Number $bar(char v);
	
	public Number $up(char v);
	
	public Number $less$less(char v);
	
	public Number $greater$greater(char v);
	
	public Number $greater$greater$greater(char v);
	
	// int operators
	
	public Number $amp(int v);
	
	public Number $bar(int v);
	
	public Number $up(int v);
	
	public Number $less$less(int v);
	
	public Number $greater$greater(int v);
	
	public Number $greater$greater$greater(int v);
	
	// long operators
	
	public Number $amp(long v);
	
	public Number $bar(long v);
	
	public Number $up(long v);
	
	public Number $less$less(long v);
	
	public Number $greater$greater(long v);
	
	public Number $greater$greater$greater(long v);
	
	// generic operators
	
	public Number $amp(Integer v);
	
	public Number $bar(Integer v);
	
	public Number $up(Integer v);
	
	public Number $less$less(Integer v);
	
	public Number $greater$greater(Integer v);
	
	public Number $greater$greater$greater(Integer v);
}
