package dyvil.collection.immutable;

import dyvil.collection.*;
import dyvil.collection.iterator.SingletonIterator;
import dyvil.tuple.Tuple2;
import dyvil.annotation.Immutable;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Immutable
public class SingletonMap<K, V> implements ImmutableMap<K, V>, Entry<K, V>
{
	private static final long serialVersionUID = 2791619158507681686L;
	
	private transient K key;
	private transient V value;
	
	public static <K, V> SingletonMap<K, V> apply(K key, V value)
	{
		return new SingletonMap(key, value);
	}
	
	public static <K, V> SingletonMap<K, V> apply(Entry<K, V> entry)
	{
		return new SingletonMap(entry.getKey(), entry.getValue());
	}
	
	public SingletonMap(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new SingletonIterator<>(this);
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new SingletonIterator(this.key);
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new SingletonIterator(this.value);
	}
	
	@Override
	public K getKey()
	{
		return this.key;
	}
	
	@Override
	public V getValue()
	{
		return this.value;
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		action.accept(this);
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		action.accept(this.key, this.value);
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return Objects.equals(this.key, key);
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		return Objects.equals(this.key, key) && Objects.equals(this.value, value);
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		return Objects.equals(this.value, value);
	}
	
	@Override
	public V get(Object key)
	{
		return Objects.equals(key, this.key) ? this.value : null;
	}
	
	@Override
	public Option<V> getOption(Object key)
	{
		return Objects.equals(key, this.key) ? new Some(this.value) : None.instance;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return new ArrayMap(new Object[] { this.key, key }, new Object[] { this.value, value }, 2, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = 1;
		Tuple2<? extends K, ? extends V>[] tuples = new Tuple2[1 + map.size()];
		tuples[0] = new Tuple2(this.key, this.value);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			tuples[index++] = new Tuple2<K, V>(entry.getKey(), entry.getValue());
		}
		return new TupleMap(tuples, index);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		return Objects.equals(this.key, key) ? EmptyMap.instance : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return Objects.equals(this.key, key) && Objects.equals(this.value, value) ? EmptyMap.instance : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return Objects.equals(this.value, value) ? EmptyMap.instance : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		return map.contains(this.key, this.value) ? EmptyMap.instance : this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		return keys.contains(this.key) ? EmptyMap.instance : this;
	}
	
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		return new SingletonMap(mapper.apply(this.key, this.value), this.value);
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		return new SingletonMap(this.key, mapper.apply(this.key, this.value));
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		Entry<? extends NK, ? extends NV> entry = mapper.apply(this.key, this.value);
		return entry == null ? EmptyMap.instance : new SingletonMap(entry.getKey(), entry.getValue());
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		ArrayMap.Builder<NK, NV> builder = new ArrayMap.Builder<NK, NV>();
		for (Entry<? extends NK, ? extends NV> entry : mapper.apply(this.key, this.value))
		{
			builder.put(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return condition.test(this.key, this.value) ? this : EmptyMap.instance;
	}
	
	@Override
	public Entry<K, V>[] toArray()
	{
		return new Entry[] { this };
	}
	
	@Override
	public void toArray(int index, Entry<K, V>[] store)
	{
		store[index] = this;
	}
	
	@Override
	public Object[] toKeyArray()
	{
		return new Object[] { this.key };
	}
	
	@Override
	public void toKeyArray(int index, Object[] store)
	{
		store[index] = this.key;
	}
	
	@Override
	public Object[] toValueArray()
	{
		return new Object[] { this.value };
	}
	
	@Override
	public void toValueArray(int index, Object[] store)
	{
		store[index] = this.value;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		return new SingletonMap<V, K>(this.value, this.key);
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new SingletonMap(this.key, this.value);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return MutableMap.apply(this.key, this.value);
	}
	
	@Override
	public String toString()
	{
		return "[ " + this.key + " -> " + this.value + " ]";
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.singletonMap(this.key, this.value);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Map)
		{
			return Map.mapEquals(this, (Map) obj);
		}
		if (obj instanceof Entry)
		{
			return Entry.entryEquals(this, (Entry) obj);
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Entry.entryHashCode(this);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(this.key);
		out.writeObject(this.value);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.key = (K) in.readObject();
		this.value = (V) in.readObject();
	}
}
