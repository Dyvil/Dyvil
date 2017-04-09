package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractTreeMap;
import dyvil.lang.LiteralConvertible;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromArray
@Immutable
public class TreeMap<K, V> extends AbstractTreeMap<K, V> implements ImmutableMap<K, V>
{
	protected static final class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private TreeMap<K, V> map;

		public Builder()
		{
			this.map = new TreeMap<>();
		}

		public Builder(Comparator<? super K> comparator)
		{
			this.map = new TreeMap<>(comparator);
		}

		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built!");
			}
			this.map.putInternal(key, value);
		}

		@Override
		public TreeMap<K, V> build()
		{
			TreeMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}

	private static final long serialVersionUID = 2012245218476747334L;

	// Factory Methods

	@NonNull
	public static <K, V> TreeMap<K, V> singleton(K key, V value)
	{
		final TreeMap<K, V> result = new TreeMap<>();
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	@SafeVarargs
	public static <K extends Comparable<K>, V> TreeMap<K, V> apply(@NonNull Entry<? extends K, ? extends V>... entries)
	{
		return new TreeMap<>(entries);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(@NonNull Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new TreeMap<>(array);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		return new TreeMap<>(iterable);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(@NonNull AbstractTreeMap<? extends K, ? extends V> treeMap)
	{
		return new TreeMap<>(treeMap);
	}

	@NonNull
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<>();
	}

	@NonNull
	public static <K, V> Builder<K, V> builder(Comparator<? super K> comparator)
	{
		return new Builder<>(comparator);
	}

	// Constructors

	protected TreeMap()
	{
		super();
	}

	public TreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public TreeMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public TreeMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries, Comparator<? super K> comparator)
	{
		super(entries, comparator);
	}

	public TreeMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> map)
	{
		super(map);
	}

	public TreeMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> map, Comparator<? super K> comparator)
	{
		super(map, comparator);
	}

	public TreeMap(@NonNull AbstractTreeMap<? extends K, ? extends V> treeMap)
	{
		super(treeMap);
	}

	public TreeMap(@NonNull AbstractTreeMap<? extends K, ? extends V> treeMap, Comparator<? super K> comparator)
	{
		super(treeMap, comparator);
	}

	// Implementation Methods

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		copy.putInternal(key, value);
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putInternal(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		boolean found = false;
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			if (!found && Objects.equals(key, entryKey))
			{
				found = true;
				continue;
			}

			copy.putInternal(entryKey, entry.getValue());
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		boolean found = false;
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!found && Objects.equals(key, entryKey))
			{
				found = true;
				if (Objects.equals(value, entryValue))
				{
					continue;
				}
			}

			copy.putInternal(entryKey, entryValue);
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			V entryValue = entry.getValue();
			if (!Objects.equals(value, entryValue))
			{
				copy.putInternal(entry.getKey(), entryValue);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				copy.putInternal(entryKey, entry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		TreeMap<NK, V> copy = new TreeMap<>();
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			V value = entry.value;
			copy.putInternal(mapper.apply(entry.key, value), value);
		}
		return copy;
	}

	@Nullable
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		TreeMap<K, NV> copy = new TreeMap<>();
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.key;
			copy.putInternal(key, mapper.apply(key, entry.getValue()));
		}
		return null;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		TreeMap<NK, NV> copy = new TreeMap<>();
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			Entry<? extends NK, ? extends NV> newEntry = mapper.apply(entry.key, entry.value);
			if (newEntry != null)
			{
				copy.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		TreeMap<NK, NV> copy = new TreeMap<>();
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			for (Entry<? extends NK, ? extends NV> newEntry : mapper.apply(entry.key, entry.value))
			{
				copy.putInternal(newEntry.getKey(), newEntry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		TreeMap<K, V> copy = new TreeMap<>(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.key;
			V value = entry.value;
			if (condition.test(key, value))
			{
				copy.putInternal(key, value);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		TreeMap<V, K> copy = new TreeMap<>();
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			copy.putInternal(entry.value, entry.key);
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this.immutableCopy();
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public <RK, RV> ImmutableMap.@NonNull Builder<RK, RV> immutableBuilder()
	{
		return builder();
	}

	@Override
	public java.util.@NonNull Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}
