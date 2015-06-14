package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.ArrayConvertible;

@ArrayConvertible
public class ArraySet<E> implements ImmutableSet<E>
{
	protected E[]	array;
	protected int	size;
	
	public static <E> ArraySet<E> apply(E... elements)
	{
		return new ArraySet(elements, true);
	}
	
	public ArraySet(E... elements)
	{
		this.array = elements.clone();
		this.size = Set.distinct(this.array, elements.length);
	}
	
	public ArraySet(E[] elements, int size)
	{
		this.array = (E[]) new Object[size];
		System.arraycopy(elements, 0, this.array, 0, size);
		this.size = size;
	}
	
	public ArraySet(E[] elements, boolean trusted)
	{
		this.array = elements;
		this.size = Set.distinct(this.array, this.array.length);
	}
	
	public ArraySet(E[] elements, int size, boolean trusted)
	{
		this.array = elements;
		this.size = size;
	}
	
	public ArraySet(Collection<E> elements)
	{
		Object[] array = new Object[elements.size()];
		int index = 0;
		outer:
		for (E element : elements)
		{
			// Check if the element is already present in the array
			for (int i = 0; i < index; i++)
			{
				if (Objects.equals(array[i], element))
				{
					continue outer;
				}
			}
			
			array[index++] = element;
		}
		
		this.array = (E[]) array;
		this.size = index;
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new ArrayIterator<E>(this.array, this.size);
	}
	
	@Override
	public boolean contains(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.array[i], element))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		if (this.contains(element))
		{
			return this;
		}
		
		Object[] newArray = new Object[this.size + 1];
		System.arraycopy(this.array, 0, newArray, 0, this.size);
		newArray[this.size] = element;
		return new ArraySet(newArray, this.size + 1, true);
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.array[i], element))
			{
				Object[] newArray = new Object[this.size - 1];
				System.arraycopy(this.array, 0, newArray, 0, i);
				System.arraycopy(this.array, i + 1, newArray, i, this.size - i - 1);
				return new ArraySet(newArray, this.size - 1, true);
			}
		}
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = this.array[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		Object[] newArray = new Object[Math.min(this.size, collection.size())];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = this.array[i];
			if (collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		int size = this.size;
		Object[] newArray = new Object[size + collection.size()];
		System.arraycopy(this.array, 0, newArray, 0, this.size);
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[size++] = element;
			}
		}
		return new ArraySet(newArray, size, true);
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		Object[] newArray = new Object[this.size + collection.size()];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = this.array[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			R newElement = mapper.apply(this.array[i]);
			
			// Search if the mapped element is already present in the array
			for (int j = 0; j < index; j++)
			{
				if (Objects.equals(newArray[i], newElement))
				{
					continue outer;
				}
			}
			
			newArray[index++] = newElement;
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		Object[] newArray = new Object[this.size << 2];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			results:
			for (R result : mapper.apply(this.array[i]))
			{
				// Search if the mapped element is already present in the array
				for (int j = 0; j < index; j++)
				{
					if (Objects.equals(newArray[j], result))
					{
						continue results;
					}
				}
				
				// Add the element to the array
				int index1 = index++;
				if (index1 >= newArray.length)
				{
					Object[] temp = new Object[index << 1];
					System.arraycopy(newArray, 0, temp, 0, newArray.length);
					newArray = temp;
				}
				newArray[index1] = result;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = this.array[i];
			if (condition.test(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet(newArray, index, true);
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		System.arraycopy(this.array, 0, store, index, this.size);
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new ArraySet(this.array, this.size, true);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return null; // TODO mutable.ArraySet ?
	}
	
	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.array[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ").append(this.array[i]);
		}
		return builder.append("]").toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}
}
