package dyvil.collection.mutable;

import dyvil.lang.literal.ArrayConvertible;

import java.util.Comparator;

@ArrayConvertible
public class TreeSet<E> extends MapBasedSet<E>
{
	private static final long serialVersionUID = 3616255313908232391L;
	
	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<E>();
	}
	
	@SafeVarargs
	public static <E> TreeSet<E> apply(E... elements)
	{
		return new TreeSet<E>(elements);
	}
	
	public TreeSet()
	{
		super(new TreeMap<>());
	}
	
	public TreeSet(Comparator<? super E> comparator)
	{
		super(new TreeMap<>(comparator));
	}
	
	@SafeVarargs
	public TreeSet(E... elements)
	{
		this();
		
		for (E element : elements)
		{
			this.map.put(element, true);
		}
	}
}
