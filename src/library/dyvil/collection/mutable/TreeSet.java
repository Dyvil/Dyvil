package dyvil.collection.mutable;

import java.util.Comparator;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.Set;

@ArrayConvertible
public class TreeSet<E> extends MapBasedSet<E>
{
	private static final long serialVersionUID = 3616255313908232391L;
	
	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<E>();
	}
	
	public static <E> TreeSet<E> apply(E... elements)
	{
		return new TreeSet<E>(elements);
	}
	
	public TreeSet()
	{
		super(new TreeMap<E, Object>());
	}
	
	public TreeSet(Comparator<? super E> comparator)
	{
		super(new TreeMap<E, Object>(comparator));
	}
	
	public TreeSet(E... elements)
	{
		this();
		
		for (E element : elements)
		{
			this.map.put(element, Set.VALUE);
		}
	}
}
