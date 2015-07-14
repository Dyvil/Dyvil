package dyvil.collection;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import dyvil.tuple.Tuple2;

/**
 * An <b>Entry</b> is a union of a Key and Value, as used in {@linkplain Map
 * maps}.
 * 
 * @author Clashsoft
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of the value
 */
public interface Entry<K, V>
{
	/**
	 * Returns the key stored by this entry
	 * 
	 * @return the key
	 */
	public K getKey();
	
	/**
	 * Returns the value stored by this entry
	 * 
	 * @return the value
	 */
	public V getValue();
	
	public default Tuple2<K, V> toTuple()
	{
		return new Tuple2<K, V>(this.getKey(), this.getValue());
	}
	
	public static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey()
	{
		return (c1, c2) -> c1.getKey().compareTo(c2.getKey());
	}
	
	public static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> comparingByValue()
	{
		return (c1, c2) -> c1.getValue().compareTo(c2.getValue());
	}
	
	public static <K, V> Comparator<Entry<K, V>> comparingByKey(Comparator<? super K> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
	}
	
	public static <K, V> Comparator<Entry<K, V>> comparingByValue(Comparator<? super V> cmp)
	{
		return (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
	}
	
	public static <K, V> String entryToString(Entry<K, V> entry)
	{
		return entry.getKey() + " -> " + entry.getValue();
	}
	
	public static <K, V> boolean entryEquals(Entry<K, V> entry, Object o)
	{
		if (!(o instanceof Entry))
		{
			return false;
		}
		
		return entryEquals(entry, (Entry) o);
	}
	
	public static <K, V> boolean entryEquals(Entry<K, V> entry1, Entry<K, V> entry2)
	{
		return Objects.equals(entry1.getKey(), entry2.getKey()) && Objects.equals(entry1.getValue(), entry2.getValue());
	}
	
	public static <K, V> int entryHashCode(Entry<K, V> entry)
	{
		K key = entry.getKey();
		V value = entry.getValue();
		int hash = (key == null ? 0 : key.hashCode() * 31) + (value == null ? 0 : value.hashCode());
		// To achieve the same hash code as if this were a SingletonMap
		return hash * 31 + hash;
	}
}
