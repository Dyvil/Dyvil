package dyvil.collection;

import java.util.Objects;

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
