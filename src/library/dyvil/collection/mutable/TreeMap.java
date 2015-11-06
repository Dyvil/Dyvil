package dyvil.collection.mutable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractTreeMap;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public class TreeMap<K, V> extends AbstractTreeMap<K, V>implements MutableMap<K, V>
{
	private static final long serialVersionUID = -7707452456610472904L;
	
	public static <K, V> TreeMap<K, V> apply()
	{
		return new TreeMap();
	}
	
	public static <K extends Comparable<K>, V> TreeMap<K, V> apply(Tuple2<K, V>... entries)
	{
		TreeMap<K, V> map = new TreeMap();
		for (Tuple2<K, V> entry : entries)
		{
			map.put(entry._1, entry._2);
		}
		return map;
	}
	
	public TreeMap()
	{
		super();
	}
	
	public TreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}
	
	public TreeMap(Map<? extends K, ? extends V> m)
	{
		super(m, null);
	}
	
	public TreeMap(Map<? extends K, ? extends V> m, Comparator<? super K> comparator)
	{
		super(m, comparator);
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		this.root = null;
	}
	
	@Override
	public V put(K key, V value)
	{
		return this.putInternal(key, value);
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		if (this.contains(key, value))
		{
			return false;
		}
		
		this.putInternal(key, value);
		return true;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		TreeEntry<K, V> p = this.getEntry(key);
		if (p != null && Objects.equals(oldValue, p.value))
		{
			p.value = newValue;
			return true;
		}
		return false;
	}
	
	@Override
	public V replace(K key, V value)
	{
		TreeEntry<K, V> p = this.getEntry(key);
		if (p != null)
		{
			V oldValue = p.value;
			p.value = value;
			return oldValue;
		}
		return null;
	}
	
	@Override
	public V removeKey(Object key)
	{
		TreeEntry<K, V> entry = this.getEntry(key);
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
		TreeEntry<K, V> entry = this.getEntry(key);
		if (entry == null || !Objects.equals(value, entry.value))
		{
			return false;
		}
		
		this.deleteEntry(entry);
		return true;
	}
	
	@Override
	public void mapValues(BiFunction<? super K, ? super V, ? extends V> function)
	{
		for (TreeEntry<K, V> e = this.getFirstEntry(); e != null; e = successor(e))
		{
			e.value = function.apply(e.key, e.value);
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
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
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new TreeMap(this, this.comparator);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new TreeMap(this.comparator);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.TreeMap(this, this.comparator);
	}
}
