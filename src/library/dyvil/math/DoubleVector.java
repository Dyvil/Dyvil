package dyvil.math;

import java.util.Arrays;

public class DoubleVector
{
	private static final int	DEFAULT_CAPACITY	= 10;
	private static final int	MAX_ARRAY_SIZE		= Integer.MAX_VALUE - 8;
	
	private double[]			elementData;
	private int					size;
	
	public DoubleVector()
	{
		this(DEFAULT_CAPACITY);
	}
	
	public DoubleVector(int initialCapacity)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		this.elementData = new double[initialCapacity];
	}
	
	private void ensureCapacity(int minCapacity)
	{
		if (minCapacity - this.elementData.length > 0)
		{
			this.grow(minCapacity);
		}
	}
	
	private void grow(int minCapacity)
	{
		// overflow-conscious code
		int oldCapacity = this.elementData.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1);
		if (newCapacity - minCapacity < 0)
		{
			newCapacity = minCapacity;
		}
		if (newCapacity - MAX_ARRAY_SIZE > 0)
		{
			newCapacity = hugeCapacity(minCapacity);
		}
		// minCapacity is usually close to size, so this is a win:
		this.elementData = Arrays.copyOf(this.elementData, newCapacity);
	}
	
	private static int hugeCapacity(int minCapacity)
	{
		if (minCapacity < 0)
		{
			throw new OutOfMemoryError();
		}
		return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}
	
	public void set(int index, double d)
	{
		this.elementData[index] = d;
	}
	
	public void add(double d)
	{
		this.size++;
		this.ensureCapacity(this.size);
		this.elementData[this.size] = d;
	}
	
	public void add(int index, double d)
	{
		this.ensureCapacity(this.size + 1); // Increments modCount!!
		System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
		this.elementData[index] = d;
		this.size++;
	}
	
	public boolean addAll(double... doubles)
	{
		int len = doubles.length;
		this.ensureCapacity(this.size + len);
		System.arraycopy(doubles, 0, this.elementData, this.size, len);
		this.size += len;
		return len != 0;
	}
	
	public boolean addAll(int index, double... doubles)
	{
		int len = doubles.length;
		this.ensureCapacity(this.size + len);
		
		int numMoved = this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elementData, index, this.elementData, index + len, numMoved);
		}
		
		System.arraycopy(doubles, 0, this.elementData, index, len);
		this.size += len;
		return len != 0;
	}
	
	public double get(int index)
	{
		return this.elementData[index];
	}
	
	public double remove(double d)
	{
		return this.removeAt(this.indexOf(d));
	}
	
	public double removeAt(int index)
	{
		double l = this.get(index);
		this.fastRemove(index);
		return l;
	}
	
	public void fastRemove(int index)
	{
		int numMoved = this.size - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
		}
	}
	
	public void removeRange(int start, int length)
	{
		int numMoved = this.size - start - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.elementData, start + length, this.elementData, start, numMoved);
		}
	}
	
	public int indexOf(double d)
	{
		for (int j = 0; j < this.size; j++)
		{
			if (this.elementData[j] == d)
			{
				return j;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(double d)
	{
		for (int j = this.size - 1; j >= 0; j--)
		{
			if (this.elementData[j] == d)
			{
				return j;
			}
		}
		return -1;
	}
}
