package dyvil.math;

import java.io.IOException;
import java.io.Serializable;

import dyvil.lang.Number;
import dyvil.lang.literal.TupleConvertible;

import dyvil.annotation._internal.prefix;

@TupleConvertible
public class Complex implements Number, Serializable
{
	private static final long serialVersionUID = 9178132461719363395L;
	
	protected transient double	real;
	protected transient double	imag;
	
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
	
	public boolean $eq$eq(double v)
	{
		return this.imag == 0D && this.real == v;
	}
	
	public boolean $bang$eq(double v)
	{
		return this.imag != 0D || this.real != v;
	}
	
	public boolean $lt(double v)
	{
		return this.imag == 0D && this.real < v;
	}
	
	public boolean $lt$eq(double v)
	{
		return this.imag == 0D && this.real <= v;
	}
	
	public boolean $gt(double v)
	{
		return this.imag == 0D && this.real > v;
	}
	
	public boolean $gt$eq(double v)
	{
		return this.imag == 0D && this.real >= v;
	}
	
	public Complex $plus(double v)
	{
		return apply(this.real + v, this.imag);
	}
	
	public Complex $minus(double v)
	{
		return apply(this.real - v, this.imag);
	}
	
	public Complex $times(double v)
	{
		return apply(this.real * v, this.imag * v);
	}
	
	public Complex $div(double v)
	{
		return apply(this.real / v, this.imag / v);
	}
	
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
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		Complex other = (Complex) obj;
		if (Double.doubleToLongBits(this.imag) != Double.doubleToLongBits(other.imag))
		{
			return false;
		}
		if (Double.doubleToLongBits(this.real) != Double.doubleToLongBits(other.real))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.imag);
		result = prime * result + (int) (temp ^ temp >>> 32);
		temp = Double.doubleToLongBits(this.real);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeDouble(this.real);
		out.writeDouble(this.imag);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.real = in.readDouble();
		this.imag = in.readDouble();
	}
}
