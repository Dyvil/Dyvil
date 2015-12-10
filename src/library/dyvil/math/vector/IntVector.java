package dyvil.math.vector;

import java.util.Arrays;

public class IntVector
{
	private static final int DEFAULT_CAPACITY = 10;
	private static final int MAX_ARRAY_SIZE   = Integer.MAX_VALUE - 8;
	
	private int[] elementData;
	private int   size;
	
	public IntVector()
	{
		this(DEFAULT_CAPACITY);
	}
	
	public IntVector(int initialCapacity)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		this.elementData = new int[initialCapacity];
	}
	
	public IntVector(int[] data)
	{
		this.size = data.length;
		this.elementData = data.clone();
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
	
	public void set(int index, int i)
	{
		this.elementData[index] = i;
	}
	
	public void add(int i)
	{
		this.size++;
		this.ensureCapacity(this.size);
		this.elementData[this.size] = i;
	}
	
	public void add(int index, int i)
	{
		this.ensureCapacity(this.size + 1); // Increments modCount!!
		System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
		this.elementData[index] = i;
		this.size++;
	}
	
	public boolean addAll(int... ints)
	{
		int len = ints.length;
		this.ensureCapacity(this.size + len);
		System.arraycopy(ints, 0, this.elementData, this.size, len);
		this.size += len;
		return len != 0;
	}
	
	public boolean addAll(int index, int... ints)
	{
		int len = ints.length;
		this.ensureCapacity(this.size + len);
		
		int numMoved = this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elementData, index, this.elementData, index + len, numMoved);
		}
		
		System.arraycopy(ints, 0, this.elementData, index, len);
		this.size += len;
		return len != 0;
	}
	
	public int get(int index)
	{
		return this.elementData[index];
	}
	
	public int remove(int i)
	{
		return this.removeAt(this.indexOf(i));
	}
	
	public int removeAt(int index)
	{
		int i = this.get(index);
		this.fastRemove(index);
		return i;
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
	
	public int indexOf(int i)
	{
		for (int j = 0; j < this.size; j++)
		{
			if (this.elementData[j] == i)
			{
				return j;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(int i)
	{
		for (int j = this.size - 1; j >= 0; j--)
		{
			if (this.elementData[j] == i)
			{
				return j;
			}
		}
		return -1;
	}
	
	public int[] toArray()
	{
		return this.toArray(new int[this.size]);
	}
	
	public int[] toArray(int[] array)
	{
		System.arraycopy(this.elementData, 0, array, 0, this.size);
		return array;
	}
}
