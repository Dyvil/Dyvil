package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.ImmutableSet;
import dyvil.lang.LiteralConvertible;

import java.util.Comparator;

@LiteralConvertible.FromArray
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

	// Factory Methods

	@NonNull
	public static <E> TreeSet<E> apply()
	{
		return new TreeSet<>();
	}

	@NonNull
	@SafeVarargs
	public static <E> TreeSet<E> apply(@NonNull E... elements)
	{
		return new TreeSet<>(elements);
	}

	@NonNull
	public static <E> TreeSet<E> from(E @NonNull [] array)
	{
		return new TreeSet<>(array);
	}

	@NonNull
	public static <E> TreeSet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return new TreeSet<>(buildMap(iterable));
	}

	@NonNull
	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	@NonNull
	public static <E> Builder<E> builder(Comparator<? super E> comparator)
	{
		return new Builder<>(comparator);
	}

	// Constructors

	private TreeSet()
	{
		super(new TreeMap<>());
	}

	public TreeSet(Comparator<? super E> comparator)
	{
		super(new TreeMap<>(comparator));
	}

	public TreeSet(E @NonNull [] elements)
	{
		super(buildMap(elements));
	}

	public TreeSet(@NonNull Iterable<? extends E> iterable)
	{
		super(buildMap(iterable));
	}

	// Implementation Methods

	protected TreeSet(TreeMap<E, Boolean> map)
	{
		super(map);
	}

	@Nullable
	private static <E> TreeMap<E, Boolean> buildMap(E @NonNull [] array)
	{
		final TreeMap.Builder<E, Boolean> builder = new TreeMap.Builder<>();
		for (E element : array)
		{
			builder.put(element, true);
		}
		return builder.build();
	}

	@Nullable
	private static <E> TreeMap<E, Boolean> buildMap(@NonNull Iterable<? extends E> iterable)
	{
		final TreeMap.Builder<E, Boolean> builder = new TreeMap.Builder<>();
		for (E element : iterable)
		{
			builder.put(element, true);
		}
		return builder.build();
	}
}
