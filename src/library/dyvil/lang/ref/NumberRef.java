package dyvil.lang.ref;

import dyvil.lang.Number;

public interface NumberRef extends Number
{
	// Setters
	
	public Number $eq(byte v);
	
	public Number $eq(short v);
	
	public Number $eq(char v);
	
	public Number $eq(int v);
	
	public Number $eq(long v);
	
	public Number $eq(float v);
	
	public Number $eq(double v);
	
	// unary operations
	
	public NumberRef $plus$plus();
	
	public NumberRef $minus$minus();
	
	// byte operations
	
	public NumberRef $plus$eq(byte v);
	
	public NumberRef $minus$eq(byte v);
	
	public NumberRef $times$eq(byte v);
	
	public NumberRef $div$eq(byte v);
	
	public NumberRef $percent$eq(byte v);
	
	// short operations
	
	public NumberRef $plus$eq(short v);
	
	public NumberRef $minus$eq(short v);
	
	public NumberRef $times$eq(short v);
	
	public NumberRef $div$eq(short v);
	
	public NumberRef $percent$eq(short v);
	
	// char operations
	
	public NumberRef $plus$eq(char v);
	
	public NumberRef $minus$eq(char v);
	
	public NumberRef $times$eq(char v);
	
	public NumberRef $div$eq(char v);
	
	public NumberRef $percent$eq(char v);
	
	// int operations
	
	public NumberRef $plus$eq(int v);
	
	public NumberRef $minus$eq(int v);
	
	public NumberRef $times$eq(int v);
	
	public NumberRef $div$eq(int v);
	
	public NumberRef $percent$eq(int v);
	
	// long operations
	
	public NumberRef $plus$eq(long v);
	
	public NumberRef $minus$eq(long v);
	
	public NumberRef $times$eq(long v);
	
	public NumberRef $div$eq(long v);
	
	public NumberRef $percent$eq(long v);
	
	// float operations
	
	public NumberRef $plus$eq(float v);
	
	public NumberRef $minus$eq(float v);
	
	public NumberRef $times$eq(float v);
	
	public NumberRef $div$eq(float v);
	
	public NumberRef $percent$eq(float v);
	
	// double operations
	
	public NumberRef $plus$eq(double v);
	
	public NumberRef $minus$eq(double v);
	
	public NumberRef $times$eq(double v);
	
	public NumberRef $div$eq(double v);
	
	public NumberRef $percent$eq(double v);
}
