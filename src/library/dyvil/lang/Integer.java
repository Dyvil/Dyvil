package dyvil.lang;

public interface Integer extends Number
{
	// Unary operators
	
	public Integer $tilde();
	
	// byte operators
	
	public Integer $amp(byte v);
	
	public Integer $bar(byte v);
	
	public Integer $up(byte v);
	
	public Integer $less$less(byte v);
	
	public Integer $greater$greater(byte v);
	
	public Integer $greater$greater$greater(byte v);
	
	// short operators
	
	public Integer $amp(short v);
	
	public Integer $bar(short v);
	
	public Integer $up(short v);
	
	public Integer $less$less(short v);
	
	public Integer $greater$greater(short v);
	
	public Integer $greater$greater$greater(short v);
	
	// char operators
	
	public Integer $amp(char v);
	
	public Integer $bar(char v);
	
	public Integer $up(char v);
	
	public Integer $less$less(char v);
	
	public Integer $greater$greater(char v);
	
	public Integer $greater$greater$greater(char v);
	
	// int operators
	
	public Integer $amp(int v);
	
	public Integer $bar(int v);
	
	public Integer $up(int v);
	
	public Integer $less$less(int v);
	
	public Integer $greater$greater(int v);
	
	public Integer $greater$greater$greater(int v);
	
	// long operators
	
	public Integer $amp(long v);
	
	public Integer $bar(long v);
	
	public Integer $up(long v);
	
	public Integer $less$less(long v);
	
	public Integer $greater$greater(long v);
	
	public Integer $greater$greater$greater(long v);
	
	// Integer operators
	
	public Integer $amp(Integer v);
	
	public Integer $bar(Integer v);
	
	public Integer $up(Integer v);
	
	public Integer $less$less(Integer v);
	
	public Integer $greater$greater(Integer v);
	
	public Integer $greater$greater$greater(Integer v);
}
