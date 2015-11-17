package dyvil.collection.immutable;

import java.util.Comparator;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.ImmutableSet;
import dyvil.collection.Set;

@ArrayConvertible
public class TreeSet<E> extends MapBasedSet<E>
{
	private static final long serialVersionUID = -6636571715777235576L;
	
	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<E>();
	}
	
	public static <E> TreeSet<E> apply(E... elements)
	{
		return new TreeSet<E>(elements);
	}
	
	public static <E> Builder<E> builder()
	{
		return new Builder();
	}
	
	public static <E> Builder<E> builder(Comparator<? super E> comparator)
	{
		return new Builder();
	}
	
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private TreeMap.Builder<E, Object> mapBuilder;
		
		public Builder()
		{
			this.mapBuilder = new TreeMap.Builder<E, Object>();
		}
		
		public Builder(Comparator<? super E> comparator)
		{
			this.mapBuilder = new TreeMap.Builder<E, Object>(comparator);
		}
		
		@Override
		public void add(E element)
		{
			this.mapBuilder.put(element, Set.VALUE);
		}
		
		@Override
		public ImmutableSet<E> build()
		{
			return new TreeSet(this.mapBuilder.build());
		}
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
		super(buildMap(elements));
	}
	
	protected TreeSet(ImmutableMap<E, Object> map)
	{
		super(map);
	}
	
	private static <E> ImmutableMap<E, Object> buildMap(E... elements)
	{
		TreeMap.Builder<E, Object> builder = new TreeMap.Builder<E, Object>();
		
		for (E element : elements)
		{
			builder.put(element, Set.VALUE);
		}
		
		return builder.build();
	}
}
