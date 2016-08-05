package dyvil.math;

import dyvil.lang.LiteralConvertible;

import java.io.IOException;
import java.io.Serializable;

@LiteralConvertible.FromTuple
public class Complex implements Serializable
{
	private static final long serialVersionUID = 9178132461719363395L;
	
	protected transient double real;
	protected transient double imag;
	
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
	
	public static Complex $tilde(Complex x)
	{
		return apply(-x.real, x.imag);
	}
	
	public Complex sqr()
	{
		double r = this.real;
		double i = this.imag;
		return apply(r * r - i * i, 2 * r * i);
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
	
	@Override
	public java.lang.String toString()
	{
		return String.valueOf(this.real) + '+' + this.imag + 'i';
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
		return Double.doubleToLongBits(this.imag) == Double.doubleToLongBits(other.imag)
				&& Double.doubleToLongBits(this.real) == Double.doubleToLongBits(other.real);
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
