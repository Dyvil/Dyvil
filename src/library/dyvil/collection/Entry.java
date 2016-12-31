package dyvil.collection;

import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.tuple.Tuple;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * An <b>Entry</b> is a union of a Key and Value, as used in {@linkplain Map maps}.
 *
 * @param <K>
 * 	the type of the key
 * @param <V>
 * 	the type of the value
 */
public interface Entry<@Covariant K, @Covariant V> extends Serializable
{
	/**
	 * Returns the key stored by this entry
	 *
	 * @return the key
	 */
	@Nullable K getKey();

	/**
	 * Returns the value stored by this entry
	 *
	 * @return the value
	 */
	@Nullable V getValue();

	/**
	 * Converts this entry to a {@link Tuple.Of2 Tuple}.
	 *
	 * @return a tuple with this entry's key and value
	 */
	default Tuple.@Nullable Of2<K, V> toTuple()
	{
		return new Tuple.Of2<>(this.getKey(), this.getValue());
	}

	/**
	 * Returns a comparator that compares entries by key. The ordering is defined by the <i>natural order</i> of the
	 * key, i.e. the ordering given by {@link Comparable#compareTo(Object)}.
	 *
	 * @return a comparator that compares entries by key.
	 */
	static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey()
	{
		return Comparator.comparing(Entry::getKey);
	}

	/**
	 * Returns a comparator that compares entries by value. The ordering is defined by the <i>natural order</i> of the
	 * value, i.e. the ordering given by {@link Comparable#compareTo(Object)}.
	 *
	 * @return a comparator that compares entries by value.
	 */
	static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> comparingByValue()
	{
		return Comparator.comparing(Entry::getValue);
	}

	/**
	 * Returns a comparator that compares entries by key. The ordering is defined by the given {@code comparator}.
	 *
	 * @param cmp
	 * 	the comparator the defines the ordering of the keys
	 *
	 * @return a comparator that compares entries by key.
	 */
	static <K, V> Comparator<Entry<K, V>> comparingByKey(@NonNull Comparator<? super K> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
	}

	/**
	 * Returns a comparator that compares entries by value. The ordering is defined by the given {@code comparator}.
	 *
	 * @param cmp
	 * 	the comparator the defines the ordering of the values
	 *
	 * @return a comparator that compares entries by value.
	 */
	static <K, V> Comparator<Entry<K, V>> comparingByValue(@NonNull Comparator<? super V> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
	}

	@Nullable
	static String entryToString(@NonNull Entry<?, ?> entry)
	{
		return entry.getKey() + " -> " + entry.getValue();
	}

	static boolean entryEquals(@NonNull Entry<?, ?> entry, Object o)
	{
		return o instanceof Entry && entryEquals(entry, (Entry) o);
	}

	static boolean entryEquals(@NonNull Entry<?, ?> entry1, @NonNull Entry<?, ?> entry2)
	{
		return Objects.equals(entry1.getKey(), entry2.getKey()) && Objects.equals(entry1.getValue(), entry2.getValue());
	}

	static int entryHashCode(@NonNull Entry<?, ?> entry)
	{
		Object key = entry.getKey();
		Object value = entry.getValue();
		int hash = (key == null ? 0 : key.hashCode() * 31) + (value == null ? 0 : value.hashCode());
		// To achieve the same hash code as if this were a SingletonMap
		return hash * 31 + hash;
	}
}
