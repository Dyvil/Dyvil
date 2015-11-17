package dyvil.collection;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import dyvil.tuple.Tuple2;

/**
 * An <b>Entry</b> is a union of a Key and Value, as used in {@linkplain Map
 * maps}.
 * 
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of the value
 */
public interface Entry<K, V> extends Serializable
{
	/**
	 * Returns the key stored by this entry
	 * 
	 * @return the key
	 */
	K getKey();
	
	/**
	 * Returns the value stored by this entry
	 * 
	 * @return the value
	 */
	V getValue();
	
	/**
	 * Converts this entry to a {@link Tuple2 Tuple}.
	 * 
	 * @return a tuple with this entry's key and value
	 */
	default Tuple2<K, V> toTuple()
	{
		return new Tuple2<K, V>(this.getKey(), this.getValue());
	}
	
	/**
	 * Returns a comparator that compares entries by key. The ordering is
	 * defined by the <i>natural order</i> of the key, i.e. the ordering given
	 * by {@link Comparable#compareTo(Object)}.
	 * 
	 * @return a comparator that compares entries by key.
	 */
	static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey()
	{
		return (c1, c2) -> c1.getKey().compareTo(c2.getKey());
	}
	
	/**
	 * Returns a comparator that compares entries by value. The ordering is
	 * defined by the <i>natural order</i> of the value, i.e. the ordering given
	 * by {@link Comparable#compareTo(Object)}.
	 * 
	 * @return a comparator that compares entries by value.
	 */
	static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> comparingByValue()
	{
		return (c1, c2) -> c1.getValue().compareTo(c2.getValue());
	}
	
	/**
	 * Returns a comparator that compares entries by key. The ordering is
	 * defined by the given {@code comparator}.
	 * 
	 * @param cmp
	 *            the comparator the defines the ordering of the keys
	 * @return a comparator that compares entries by key.
	 */
	static <K, V> Comparator<Entry<K, V>> comparingByKey(Comparator<? super K> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
	}
	
	/**
	 * Returns a comparator that compares entries by value. The ordering is
	 * defined by the given {@code comparator}.
	 * 
	 * @param cmp
	 *            the comparator the defines the ordering of the values
	 * @return a comparator that compares entries by value.
	 */
	static <K, V> Comparator<Entry<K, V>> comparingByValue(Comparator<? super V> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
	}
	
	static String entryToString(Entry<?, ?> entry)
	{
		return entry.getKey() + " -> " + entry.getValue();
	}
	
	static boolean entryEquals(Entry<?, ?> entry, Object o)
	{
		if (!(o instanceof Entry))
		{
			return false;
		}
		
		return entryEquals(entry, (Entry) o);
	}
	
	static boolean entryEquals(Entry<?, ?> entry1, Entry<?, ?> entry2)
	{
		return Objects.equals(entry1.getKey(), entry2.getKey()) && Objects.equals(entry1.getValue(), entry2.getValue());
	}
	
	static int entryHashCode(Entry<?, ?> entry)
	{
		Object key = entry.getKey();
		Object value = entry.getValue();
		int hash = (key == null ? 0 : key.hashCode() * 31) + (value == null ? 0 : value.hashCode());
		// To achieve the same hash code as if this were a SingletonMap
		return hash * 31 + hash;
	}
}
