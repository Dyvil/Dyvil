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
	
	public @prefix Number $minus();
	
	public @prefix Number $tilde();
	
	public Number $plus$plus();
	
	public Number $minus$minus();
	
	public Number sqr();
	
	public Number sqrt();
	
	// byte operators
	
	public Number $eq(byte v);
	
	public boolean $eq$eq(byte v);
	
	public boolean $bang$eq(byte v);
	
	public boolean $less(byte v);
	
	public boolean $less$eq(byte v);
	
	public boolean $greater(byte v);
	
	public boolean $greater$eq(byte v);
	
	public Number $plus(byte v);
	
	public Number $minus(byte v);
	
	public Number $times(byte v);
	
	public Number $div(byte v);
	
	public Number $percent(byte v);
	
	public Number $amp(byte v);
	
	public Number $bar(byte v);
	
	public Number $up(byte v);
	
	public Number $less$less(byte v);
	
	public Number $greater$greater(byte v);
	
	public Number $greater$greater$greater(byte v);
	
	// short operators
	
	public Number $eq(short v);
	
	public boolean $eq$eq(short v);
	
	public boolean $bang$eq(short v);
	
	public boolean $less(short v);
	
	public boolean $less$eq(short v);
	
	public boolean $greater(short v);
	
	public boolean $greater$eq(short v);
	
	public Number $plus(short v);
	
	public Number $minus(short v);
	
	public Number $times(short v);
	
	public Number $div(short v);
	
	public Number $percent(short v);
	
	public Number $amp(short v);
	
	public Number $bar(short v);
	
	public Number $up(short v);
	
	public Number $less$less(short v);
	
	public Number $greater$greater(short v);
	
	public Number $greater$greater$greater(short v);
	
	// char operators
	
	public Number $eq(char v);
	
	public boolean $eq$eq(char v);
	
	public boolean $bang$eq(char v);
	
	public boolean $less(char v);
	
	public boolean $less$eq(char v);
	
	public boolean $greater(char v);
	
	public boolean $greater$eq(char v);
	
	public Number $plus(char v);
	
	public Number $minus(char v);
	
	public Number $times(char v);
	
	public Number $div(char v);
	
	public Number $percent(char v);
	
	public Number $amp(char v);
	
	public Number $bar(char v);
	
	public Number $up(char v);
	
	public Number $less$less(char v);
	
	public Number $greater$greater(char v);
	
	public Number $greater$greater$greater(char v);
	
	// int operators
	
	public Number $eq(int v);
	
	public boolean $eq$eq(int v);
	
	public boolean $bang$eq(int v);
	
	public boolean $less(int v);
	
	public boolean $less$eq(int v);
	
	public boolean $greater(int v);
	
	public boolean $greater$eq(int v);
	
	public Number $plus(int v);
	
	public Number $minus(int v);
	
	public Number $times(int v);
	
	public Number $div(int v);
	
	public Number $percent(int v);
	
	public Number $amp(int v);
	
	public Number $bar(int v);
	
	public Number $up(int v);
	
	public Number $less$less(int v);
	
	public Number $greater$greater(int v);
	
	public Number $greater$greater$greater(int v);
	
	// long operators
	
	public Number $eq(long v);
	
	public boolean $eq$eq(long v);
	
	public boolean $bang$eq(long v);
	
	public boolean $less(long v);
	
	public boolean $less$eq(long v);
	
	public boolean $greater(long v);
	
	public boolean $greater$eq(long v);
	
	public Number $plus(long v);
	
	public Number $minus(long v);
	
	public Number $times(long v);
	
	public Number $div(long v);
	
	public Number $percent(long v);
	
	public Number $amp(long v);
	
	public Number $bar(long v);
	
	public Number $up(long v);
	
	public Number $less$less(long v);
	
	public Number $greater$greater(long v);
	
	public Number $greater$greater$greater(long v);
	
	// float operators
	
	public Number $eq(float v);
	
	public boolean $eq$eq(float v);
	
	public boolean $bang$eq(float v);
	
	public boolean $less(float v);
	
	public boolean $less$eq(float v);
	
	public boolean $greater(float v);
	
	public boolean $greater$eq(float v);
	
	public Number $plus(float v);
	
	public Number $minus(float v);
	
	public Number $times(float v);
	
	public Number $div(float v);
	
	public Number $percent(float v);
	
	// double operators
	
	public Number $eq(double v);
	
	public boolean $eq$eq(double v);
	
	public boolean $bang$eq(double v);
	
	public boolean $less(double v);
	
	public boolean $less$eq(double v);
	
	public boolean $greater(double v);
	
	public boolean $greater$eq(double v);
	
	public Number $plus(double v);
	
	public Number $minus(double v);
	
	public Number $times(double v);
	
	public Number $div(double v);
	
	public Number $percent(double v);
}
