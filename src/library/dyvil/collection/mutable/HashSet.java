package dyvil.collection.mutable;

import java.util.Objects;
import java.util.function.Function;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractHashSet;
import dyvil.math.MathUtils;

import static dyvil.collection.impl.AbstractHashMap.*;

@NilConvertible
@ArrayConvertible
public class HashSet<E> extends AbstractHashSet<E>implements MutableSet<E>
{
	private static final long serialVersionUID = -993127062150101200L;
	
	private float			loadFactor;
	private transient int	threshold;
	
	public static <E> HashSet<E> apply()
	{
		return new HashSet();
	}
	
	public static <E> HashSet<E> apply(E... elements)
	{
		return new HashSet(elements);
	}
	
	public HashSet()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	public HashSet(int capacity)
	{
		this(capacity, DEFAULT_LOAD_FACTOR);
	}
	
	public HashSet(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}
	
	public HashSet(int capacity, float loadFactor)
	{
		super(capacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}
		
		this.loadFactor = loadFactor;
		this.threshold = (int) (capacity * this.loadFactor);
	}
	
	public HashSet(Collection<E> collection)
	{
		super(collection);
		this.defaultThreshold();
	}
	
	public HashSet(Set<E> set)
	{
		super(set);
		this.defaultThreshold();
	}
	
	public HashSet(AbstractHashSet<E> set)
	{
		super(set);
		this.defaultThreshold();
	}
	
	public HashSet(E... elements)
	{
		super(elements);
		this.defaultThreshold();
	}
	
	private void defaultThreshold()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int) (this.elements.length * DEFAULT_LOAD_FACTOR);
	}
	
	@Override
	protected void updateThreshold(int newCapacity)
	{
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		int length = this.elements.length;
		for (int i = 0; i < length; i++)
		{
			this.elements[i] = null;
		}
	}
	
	@Override
	protected void addElement(int hash, E element, int index)
	{
		HashElement[] tab = this.elements;
		if (this.size >= this.threshold)
		{
			// Rehash / flatten the table if the threshold is exceeded
			this.flatten();
			
			tab = this.elements;
			hash = hash(element);
			index = index(hash, tab.length);
		}
		
		tab[index] = new HashElement(element, hash, tab[index]);
		this.size++;
	}
	
	@Override
	public boolean add(E element)
	{
		return this.addInternal(element);
	}
	
	@Override
	public boolean remove(Object element)
	{
		int hash = hash(element);
		int i = index(hash, this.elements.length);
		HashElement<E> prev = this.elements[i];
		HashElement<E> e = prev;
		
		while (e != null)
		{
			HashElement<E> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.element) == element || Objects.equals(element, k)))
			{
				this.size--;
				if (prev == e)
				{
					this.elements[i] = next;
				}
				else
				{
					prev.next = next;
				}
				
				return true;
			}
			prev = e;
			e = next;
		}
		
		return false;
	}
	
	@Override
	public void map(Function<? super E, ? extends E> mapper)
	{
		// Other than flatMap, map allows us to inline the implementation,
		// because we can be sure that the size will not grow, and no re-hash /
		// table growing will be required.
		int len = MathUtils.powerOfTwo(this.size);
		HashElement[] newElements = new HashElement[len];
		int size = 0;
		
		for (HashElement<E> element : this.elements)
		{
			outer:
			for (; element != null; element = element.next)
			{
				E newElement = mapper.apply(element.element);
				int hash = hash(newElement);
				int index = index(hash, len);
				HashElement oldElement = newElements[index];
				
				for (; oldElement != null; oldElement = oldElement.next)
				{
					if (oldElement.hash == hash && Objects.equals(oldElement.element, newElement))
					{
						// Object already present, don't add it
						continue outer;
					}
				}
				
				size++;
				newElements[index] = new HashElement<E>(newElement, hash, newElements[index]);
			}
		}
		
		this.elements = newElements;
		this.size = size;
	}
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		// To simplify the implementation of this method, we create a temporary
		// copy that is used to collect all new elements produced by the mapper.
		HashSet<E> copy = new HashSet<E>(this.size << 2, this.loadFactor);
		for (E element : this)
		{
			for (E newElement : mapper.apply(element))
			{
				copy.add(newElement);
			}
		}
		
		// After supplying all elements to the wrapper, we simply use the
		// elements of the copy, which will be discarded anyway.
		this.size = copy.size;
		this.elements = copy.elements;
		this.threshold = copy.threshold;
	}
	
	@Override
	public MutableSet<E> copy()
	{
		return new HashSet<E>(this);
	}
	
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new HashSet<R>(this.size);
	}
	
	@Override
	public ImmutableSet<E> immutable()
	{
		return new dyvil.collection.immutable.HashSet<E>(this);
	}
}
