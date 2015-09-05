package dyvil.math;

import dyvil.lang.Number;
import dyvil.lang.literal.TupleConvertible;

import dyvil.annotation.prefix;

@TupleConvertible
public class Complex implements Number
{
	protected double	real;
	protected double	imag;
	
	public static Complex apply(double r, double i)
	{
		return new Complex(r, i);
	}
	
	protected Complex(double real, double imag)
	{
		this.real = real;
		this.imag = imag;
	}
	
	public double real()
	{
		return this.real;
	}
	
	public double imag()
	{
		return this.imag;
	}
	
	@Override
	public byte byteValue()
	{
		return (byte) this.real;
	}
	
	@Override
	public short shortValue()
	{
		return (short) this.real;
	}
	
	@Override
	public char charValue()
	{
		return (char) this.real;
	}
	
	@Override
	public int intValue()
	{
		return (int) this.real;
	}
	
	@Override
	public long longValue()
	{
		return (long) this.real;
	}
	
	@Override
	public float floatValue()
	{
		return (float) this.real;
	}
	
	@Override
	public double doubleValue()
	{
		return this.real;
	}
	
	@Override
	public @prefix Complex $plus()
	{
		return this;
	}
	
	@Override
	public @prefix Complex $minus()
	{
		return apply(-this.real, -this.imag);
	}
	
	public @prefix Complex $tilde()
	{
		return apply(-this.real, this.imag);
	}
	
	public Complex sqr()
	{
		double r = this.real;
		double i = this.imag;
		return apply(r * r - i * i, 2D * r * i);
	}
	
	public Complex rec()
	{
		double r = this.real;
		double i = this.imag;
		double m = Math.sqrt(r * r + i * i);
		return apply(Math.sqrt((r + m) / 2), Math.copySign((-r + m) / 2, i));
	}
	
	// byte operators
	
	@Override
	public boolean $eq$eq(byte v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(byte v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(byte v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(byte v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(byte v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(byte v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(byte v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(byte v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(byte v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(byte v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(byte v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// short operators
	
	@Override
	public boolean $eq$eq(short v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(short v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(short v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(short v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(short v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(short v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(short v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(short v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(short v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(short v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(short v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// char operators
	
	@Override
	public boolean $eq$eq(char v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(char v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(char v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(char v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(char v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(char v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(char v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(char v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(char v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(char v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(char v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// int operators
	
	@Override
	public boolean $eq$eq(int v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(int v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(int v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(int v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(int v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(int v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(int v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(int v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(int v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(int v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(int v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// long operators
	
	@Override
	public boolean $eq$eq(long v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(long v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(long v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(long v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(long v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(long v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(long v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(long v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(long v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(long v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(long v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// float operators
	
	@Override
	public boolean $eq$eq(float v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(float v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(float v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(float v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(float v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(float v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(float v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(float v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(float v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(float v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(float v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// double operators
	
	@Override
	public boolean $eq$eq(double v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	@Override
	public boolean $bang$eq(double v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	@Override
	public boolean $lt(double v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $lt$eq(double v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $gt(double v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $gt$eq(double v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(double v)
	{
		return apply(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(double v)
	{
		return apply(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(double v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(double v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(double v)
	{
		return apply(this.real % v, this.imag % v);
	}
	
	// Complex operators
	
	public boolean $eq$eq(Complex v)
	{
		return this.real == v.real && this.imag == v.imag;
	}
	
	public boolean $bang$eq(Complex v)
	{
		return this.real != v.real || this.imag != v.imag;
	}
	
	public boolean $lt(Complex v)
	{
		return false;
	}
	
	public boolean $lt$eq(Complex v)
	{
		return false;
	}
	
	public boolean $gt(Complex v)
	{
		return false;
	}
	
	public boolean $gt$eq(Complex v)
	{
		return false;
	}
	
	public Complex $plus(Complex v)
	{
		return apply(this.real + v.real, this.imag + v.imag);
	}
	
	public Complex $minus(Complex v)
	{
		return apply(this.real - v.real, this.imag - v.imag);
	}
	
	public Complex $times(Complex v)
	{
		return apply(this.real * v.real - this.imag * v.imag, this.imag * v.real + this.real * v.imag);
	}
	
	public Complex $div(Complex v)
	{
		double d = 1D / (v.real * v.real + v.imag * v.imag);
		return apply((this.real * v.real + this.imag * v.imag) * d, (this.imag * v.real - this.real * v.imag) * d);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.imag == 0D && this.real == v.doubleValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.imag != 0D || this.real != v.doubleValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return false;
	}
	
	@Override
	public Complex $plus(Number v)
	{
		return apply(this.real + v.doubleValue(), this.imag);
	}
	
	@Override
	public Complex $minus(Number v)
	{
		return apply(this.real - v.doubleValue(), this.imag);
	}
	
	@Override
	public Complex $times(Number v)
	{
		return apply(this.real * v.doubleValue(), this.imag * v.doubleValue());
	}
	
	@Override
	public Complex $div(Number v)
	{
		return apply(this.real / v.doubleValue(), this.imag / v.doubleValue());
	}
	
	@Override
	public Complex $percent(Number v)
	{
		return apply(this.real % v.doubleValue(), this.imag % v.doubleValue());
	}
	
	@Override
	public int compareTo(Number o)
	{
		return java.lang.Double.compare(this.real, o.doubleValue());
	}
	
	@Override
	public Complex next()
	{
		return Complex.apply(this.real + 1D, this.imag);
	}
	
	@Override
	public Number previous()
	{
		return Complex.apply(this.real - 1D, this.imag);
	}
	
	@Override
	public java.lang.String toString()
	{
		return new StringBuilder(20).append(this.real).append('+').append(this.imag).append('i').toString();
	}
}
