package dyvil.collection.mutable;

import dyvil.lang.literal.ArrayConvertible;

import java.util.Comparator;

@ArrayConvertible
public class TreeSet<E> extends MapBasedSet<E>
{
	private static final long serialVersionUID = 3616255313908232391L;

	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<>();
	}

	@SafeVarargs
	public static <E> TreeSet<E> apply(E... elements)
	{
		return new TreeSet<>(elements);
	}

	public static <E> TreeSet<E> from(E[] array)
	{
		return new TreeSet<>(array);
	}

	public static <E> TreeSet<E> from(Iterable<? extends E> iterable)
	{
		return new TreeSet<>(iterable);
	}

	public TreeSet()
	{
		super(new TreeMap<>());
	}

	public TreeSet(Comparator<? super E> comparator)
	{
		super(new TreeMap<>(comparator));
	}

	public TreeSet(E[] elements)
	{
		this();

		for (E element : elements)
		{
			this.map.put(element, true);
		}
	}

	public TreeSet(Iterable<? extends E> iterable)
	{
		this();

		for (E element : iterable)
		{
			this.map.put(element, true);
		}
	}
}
