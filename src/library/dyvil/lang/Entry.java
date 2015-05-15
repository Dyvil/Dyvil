package dyvil.lang;

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
}
