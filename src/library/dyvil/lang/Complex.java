package dyvil.lang;

public class Complex implements Number
{
	protected double	real;
	protected double	imag;
	
	protected Complex(double real, double imag)
	{
		this.real = real;
		this.imag = imag;
	}
	
	public static Complex create(double r, double i)
	{
		return new Complex(r, i);
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
	public Number $minus()
	{
		return create(-this.real, -this.imag);
	}
	
	public Number $tilde()
	{
		return create(this.real, -this.imag);
	}
	
	@Override
	public Number sqr()
	{
		double r = this.real;
		double i = this.imag;
		return create(r * r - i * i, 2D * r * i);
	}
	
	@Override
	public Number rec()
	{
		double r = this.real;
		double i = this.imag;
		double m = Math.sqrt(r * r + i * i);
		return create(Math.sqrt((r + m) / 2), Math.copySign((-r + m) / 2, i));
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
	public boolean $less(byte v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(byte v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(byte v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(byte v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(byte v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(byte v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(byte v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(byte v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(byte v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(short v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(short v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(short v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(short v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(short v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(short v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(short v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(short v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(short v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(char v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(char v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(char v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(char v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(char v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(char v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(char v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(char v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(char v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(int v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(int v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(int v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(int v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(int v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(int v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(int v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(int v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(int v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(long v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(long v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(long v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(long v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(long v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(long v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(long v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(long v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(long v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(float v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(float v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(float v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(float v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(float v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(float v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(float v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(float v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(float v)
	{
		return create(this.real % v, this.imag % v);
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
	public boolean $less(double v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	@Override
	public boolean $less$eq(double v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	@Override
	public boolean $greater(double v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	@Override
	public boolean $greater$eq(double v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	@Override
	public Complex $plus(double v)
	{
		return create(this.real + v, this.imag);
	}
	
	@Override
	public Complex $minus(double v)
	{
		return create(this.real - v, this.imag);
	}
	
	@Override
	public Complex $times(double v)
	{
		return create(this.real * v, this.imag * v);
	}
	
	@Override
	public Complex $div(double v)
	{
		return create(this.real / v, this.imag / v);
	}
	
	@Override
	public Complex $percent(double v)
	{
		return create(this.real % v, this.imag % v);
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
	
	public boolean $less(Complex v)
	{
		return false;
	}
	
	public boolean $less$eq(Complex v)
	{
		return false;
	}
	
	public boolean $greater(Complex v)
	{
		return false;
	}
	
	public boolean $greater$eq(Complex v)
	{
		return false;
	}
	
	public Complex $plus(Complex v)
	{
		return create(this.real + v.real, this.imag + v.imag);
	}
	
	public Complex $minus(Complex v)
	{
		return create(this.real - v.real, this.imag - v.imag);
	}
	
	public Complex $times(Complex v)
	{
		return create(this.real * v.real - this.imag * v.imag, this.imag * v.real + this.real * v.imag);
	}
	
	public Complex $div(Complex v)
	{
		double d = 1D / (v.real * v.real + v.imag * v.imag);
		return create((this.real * v.real + this.imag * v.imag) * d, (this.imag * v.real - this.real * v.imag) * d);
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
	public boolean $less(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $less$eq(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $greater(Number v)
	{
		return false;
	}
	
	@Override
	public boolean $greater$eq(Number v)
	{
		return false;
	}
	
	@Override
	public Complex $plus(Number v)
	{
		return create(this.real + v.doubleValue(), this.imag);
	}
	
	@Override
	public Complex $minus(Number v)
	{
		return create(this.real - v.doubleValue(), this.imag);
	}
	
	@Override
	public Complex $times(Number v)
	{
		return create(this.real * v.doubleValue(), this.imag * v.doubleValue());
	}
	
	@Override
	public Complex $div(Number v)
	{
		return create(this.real / v.doubleValue(), this.imag / v.doubleValue());
	}
	
	@Override
	public Complex $percent(Number v)
	{
		return create(this.real % v.doubleValue(), this.imag % v.doubleValue());
	}
	
	@Override
	public java.lang.String toString()
	{
		return new StringBuilder(20).append(this.real).append('+').append(this.imag).append('i').toString();
	}
}
