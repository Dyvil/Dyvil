package dyvil.math;

import java.util.Arrays;

public class LongVector
{
	private static final int	DEFAULT_CAPACITY	= 10;
	private static final int	MAX_ARRAY_SIZE		= Integer.MAX_VALUE - 8;
	
	private long[]	elementData;
	private int		size;
	
	public LongVector()
	{
		this(DEFAULT_CAPACITY);
	}
	
	public LongVector(int initialCapacity)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		this.elementData = new long[initialCapacity];
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
	
	public void set(int index, long l)
	{
		this.elementData[index] = l;
	}
	
	public void add(long l)
	{
		this.size++;
		this.ensureCapacity(this.size);
		this.elementData[this.size] = l;
	}
	
	public void add(int index, long l)
	{
		this.ensureCapacity(this.size + 1); // Increments modCount!!
		System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
		this.elementData[index] = l;
		this.size++;
	}
	
	public boolean addAll(long... longs)
	{
		int len = longs.length;
		this.ensureCapacity(this.size + len);
		System.arraycopy(longs, 0, this.elementData, this.size, len);
		this.size += len;
		return len != 0;
	}
	
	public boolean addAll(int index, long... longs)
	{
		int len = longs.length;
		this.ensureCapacity(this.size + len);
		
		int numMoved = this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elementData, index, this.elementData, index + len, numMoved);
		}
		
		System.arraycopy(longs, 0, this.elementData, index, len);
		this.size += len;
		return len != 0;
	}
	
	public long get(int index)
	{
		return this.elementData[index];
	}
	
	public long remove(long l)
	{
		return this.removeAt(this.indexOf(l));
	}
	
	public long removeAt(int index)
	{
		long l = this.get(index);
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
	
	public int indexOf(long l)
	{
		for (int j = 0; j < this.size; j++)
		{
			if (this.elementData[j] == l)
			{
				return j;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(long l)
	{
		for (int j = this.size - 1; j >= 0; j--)
		{
			if (this.elementData[j] == l)
			{
				return j;
			}
		}
		return -1;
	}
}
