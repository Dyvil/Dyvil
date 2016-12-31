package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractTreeMap;
import dyvil.lang.LiteralConvertible;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromColonOperator(methodName = "singleton")
@LiteralConvertible.FromArray
public class TreeMap<K, V> extends AbstractTreeMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = -7707452456610472904L;

	// Factory Methods

	@NonNull
	public static <K, V> TreeMap<K, V> singleton(K key, V value)
	{
		final TreeMap<K, V> result = new TreeMap<>();
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	public static <K, V> TreeMap<K, V> apply()
	{
		return new TreeMap<>();
	}

	@NonNull
	@SafeVarargs
	public static <K extends Comparable<K>, V> TreeMap<K, V> apply(@NonNull Entry<? extends K, ? extends V>... entries)
	{
		return new TreeMap<>(entries);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new TreeMap<>(array);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new TreeMap<>(iterable);
	}

	@NonNull
	public static <K extends Comparable<K>, V> TreeMap<K, V> from(@NonNull AbstractTreeMap<? extends K, ? extends V> treeMap)
	{
		return new TreeMap<>(treeMap);
	}

	// Constructors

	public TreeMap()
	{
	}

	public TreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public TreeMap(Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public TreeMap(Entry<? extends K, ? extends V> @NonNull [] entries, Comparator<? super K> comparator)
	{
		super(entries, comparator);
	}

	public TreeMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> map)
	{
		super(map);
	}

	public TreeMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> map, Comparator<? super K> comparator)
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

	@Override
	public void clear()
	{
		this.size = 0;
		this.root = null;
	}

	@Nullable
	@Override
	public V put(K key, V value)
	{
		return this.putInternal(key, value);
	}

	@Override
	public V putIfAbsent(K key, V value)
	{
		final TreeEntry<K, V> entry = this.getEntryInternal(key);
		if (entry != null)
		{
			return entry.value;
		}

		this.putInternal(key, value);
		return value;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		TreeEntry<K, V> p = this.getEntryInternal(key);
		if (p != null && Objects.equals(oldValue, p.value))
		{
			p.value = newValue;
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public V replace(K key, V value)
	{
		TreeEntry<K, V> p = this.getEntryInternal(key);
		if (p != null)
		{
			V oldValue = p.value;
			p.value = value;
			return oldValue;
		}
		return null;
	}

	@Nullable
	@Override
	public V removeKey(Object key)
	{
		TreeEntry<K, V> entry = this.getEntryInternal(key);
		if (entry == null)
		{
			return null;
		}

		V value = entry.value;
		this.deleteEntry(entry);
		return value;
	}

	@Override
	public boolean removeValue(Object value)
	{
		for (TreeEntry<K, V> e = this.getFirstEntry(); e != null; e = successor(e))
		{
			if (Objects.equals(value, e.value))
			{
				this.deleteEntry(e);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean remove(Object key, Object value)
	{
		TreeEntry<K, V> entry = this.getEntryInternal(key);
		if (entry == null || !Objects.equals(value, entry.value))
		{
			return false;
		}

		this.deleteEntry(entry);
		return true;
	}

	@Override
	public void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> function)
	{
		for (TreeEntry<K, V> e = this.getFirstEntry(); e != null; e = successor(e))
		{
			e.value = function.apply(e.key, e.value);
		}
	}

	@Override
	public void filter(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		TreeEntry<K, V> e = this.getFirstEntry();
		while (e != null)
		{
			TreeEntry<K, V> next = successor(e);
			if (!condition.test(e.key, e.value))
			{
				this.deleteEntry(e);
			}
			e = next;
		}
	}

	@NonNull
	@Override
	public MutableMap<K, V> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return this.immutableCopy();
	}
}
