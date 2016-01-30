package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.ImmutableMap;
import dyvil.collection.ImmutableSet;
import dyvil.lang.literal.ArrayConvertible;

import java.util.Comparator;

@ArrayConvertible
@Immutable
public class TreeSet<E> extends MapBasedSet<E>
{
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private TreeMap.Builder<E, Boolean> mapBuilder;

		public Builder()
		{
			this.mapBuilder = new TreeMap.Builder<>();
		}

		public Builder(Comparator<? super E> comparator)
		{
			this.mapBuilder = new TreeMap.Builder<>(comparator);
		}

		@Override
		public void add(E element)
		{
			this.mapBuilder.put(element, true);
		}

		@Override
		public ImmutableSet<E> build()
		{
			return new TreeSet<>(this.mapBuilder.build());
		}
	}

	private static final long serialVersionUID = -6636571715777235576L;

	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<>();
	}

	@SafeVarargs
	public static <E> TreeSet<E> apply(E... elements)
	{
		return new TreeSet<>(elements);
	}

	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	public static <E> Builder<E> builder(Comparator<? super E> comparator)
	{
		return new Builder<>(comparator);
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
		super(buildMap(elements));
	}
	
	protected TreeSet(ImmutableMap<E, Boolean> map)
	{
		super(map);
	}
	
	@SafeVarargs
	private static <E> ImmutableMap<E, Boolean> buildMap(E... elements)
	{
		TreeMap.Builder<E, Boolean> builder = new TreeMap.Builder<>();
		
		for (E element : elements)
		{
			builder.put(element, true);
		}
		
		return builder.build();
	}
}
