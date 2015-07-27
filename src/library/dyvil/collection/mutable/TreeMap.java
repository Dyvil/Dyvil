package dyvil.collection.mutable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.immutable.ArrayMap;

public class TreeMap<K, V> implements MutableMap<K, V>
{
	static final class TreeEntry<K, V> implements dyvil.collection.Entry<K, V>
	{
		K				key;
		V				value;
		TreeEntry<K, V>	left	= null;
		TreeEntry<K, V>	right	= null;
		TreeEntry<K, V>	parent;
		boolean			color	= BLACK;
		
		TreeEntry(K key, V value, TreeEntry<K, V> parent)
		{
			this.key = key;
			this.value = value;
			this.parent = parent;
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
		
		public V setValue(V value)
		{
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof TreeEntry))
			{
				return false;
			}
			TreeEntry<?, ?> e = (TreeEntry<?, ?>) o;
			
			return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
		}
		
		@Override
		public int hashCode()
		{
			int keyHash = this.key == null ? 0 : this.key.hashCode();
			int valueHash = this.value == null ? 0 : this.value.hashCode();
			return keyHash ^ valueHash;
		}
		
		@Override
		public String toString()
		{
			return this.key + "=" + this.value;
		}
	}
	
	private static final boolean RED = false;
	
	private static final boolean BLACK = true;
	
	private final Comparator<? super K> comparator;
	
	private TreeEntry<K, V>	root	= null;
	private int				size	= 0;
	
	public TreeMap()
	{
		this.comparator = null;
	}
	
	public TreeMap(Comparator<? super K> comparator)
	{
		this.comparator = comparator;
	}
	
	public TreeMap(Map<? extends K, ? extends V> m)
	{
		this.comparator = null;
		this.putAll(m);
	}
	
	public Comparator<? super K> comparator()
	{
		return this.comparator;
	}
	
	@SuppressWarnings("unchecked")
	final int compare(Object k1, Object k2)
	{
		return this.comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : this.comparator.compare((K) k1, (K) k2);
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	abstract class PrivateEntryIterator<T> implements Iterator<T>
	{
		TreeEntry<K, V>	next;
		TreeEntry<K, V>	lastReturned;
		int				expectedModCount;
		
		PrivateEntryIterator(TreeEntry<K, V> first)
		{
			this.lastReturned = null;
			this.next = first;
		}
		
		@Override
		public final boolean hasNext()
		{
			return this.next != null;
		}
		
		final TreeEntry<K, V> nextEntry()
		{
			TreeEntry<K, V> e = this.next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}
			this.next = successor(e);
			this.lastReturned = e;
			return e;
		}
		
		final TreeEntry<K, V> prevEntry()
		{
			TreeEntry<K, V> e = this.next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}
			this.next = predecessor(e);
			this.lastReturned = e;
			return e;
		}
		
		@Override
		public void remove()
		{
			if (this.lastReturned == null)
			{
				throw new IllegalStateException();
			}
			
			if (this.lastReturned.left != null && this.lastReturned.right != null)
			{
				this.next = this.lastReturned;
			}
			TreeMap.this.deleteEntry(this.lastReturned);
			this.lastReturned = null;
		}
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new PrivateEntryIterator(this.getFirstEntry())
		{
			@Override
			public Entry<K, V> next()
			{
				return this.nextEntry();
			}
		};
	}
	
	@Override
	public Spliterator<Entry<K, V>> spliterator()
	{
		return new EntrySpliterator<K, V>(this, null, null, 0, -1);
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new PrivateEntryIterator<K>(this.getFirstEntry())
		{
			@Override
			public K next()
			{
				return this.nextEntry().key;
			}
		};
	}
	
	@Override
	public final Spliterator<K> keySpliterator()
	{
		return new KeySpliterator<K, V>(this, null, null, 0, -1);
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new PrivateEntryIterator<V>(this.getFirstEntry())
		{
			@Override
			public V next()
			{
				return this.nextEntry().value;
			}
		};
	}
	
	@Override
	public Spliterator<V> valueSpliterator()
	{
		return new ValueSpliterator<K, V>(this, null, null, 0, -1);
	}
	
	static class TreeMapSpliterator<K, V>
	{
		final TreeMap<K, V>		tree;
		TreeMap.TreeEntry<K, V>	current;
		TreeMap.TreeEntry<K, V>	fence;
		int						side;
		int						est;
		
		TreeMapSpliterator(TreeMap<K, V> tree, TreeMap.TreeEntry<K, V> origin, TreeMap.TreeEntry<K, V> fence, int side, int est)
		{
			this.tree = tree;
			this.current = origin;
			this.fence = fence;
			this.side = side;
			this.est = est;
		}
		
		final int getEstimate()
		{
			int s;
			TreeMap<K, V> t;
			if ((s = this.est) < 0)
			{
				if ((t = this.tree) != null)
				{
					this.current = s == -1 ? t.getFirstEntry() : t.getLastEntry();
					s = this.est = t.size;
				}
				else
				{
					s = this.est = 0;
				}
			}
			return s;
		}
		
		public final long estimateSize()
		{
			return this.getEstimate();
		}
	}
	
	static final class KeySpliterator<K, V> extends TreeMapSpliterator<K, V>implements Spliterator<K>
	{
		KeySpliterator(TreeMap<K, V> tree, TreeMap.TreeEntry<K, V> origin, TreeMap.TreeEntry<K, V> fence, int side, int est)
		{
			super(tree, origin, fence, side, est);
		}
		
		@Override
		public KeySpliterator<K, V> trySplit()
		{
			if (this.est < 0)
			{
				this.getEstimate();
			}
			int d = this.side;
			TreeMap.TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && this.tree.compare(e.key, s.key) < 0)
			{
				this.side = 1;
				return new KeySpliterator<>(this.tree, e, this.current = s, -1, this.est >>>= 1);
			}
			return null;
		}
		
		@Override
		public void forEachRemaining(Consumer<? super K> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			TreeMap.TreeEntry<K, V> f = this.fence, e, p, pl;
			if ((e = this.current) != null && e != f)
			{
				this.current = f;
				do
				{
					action.accept(e.key);
					if ((p = e.right) != null)
					{
						while ((pl = p.left) != null)
						{
							p = pl;
						}
					}
					else
					{
						while ((p = e.parent) != null && e == p.right)
						{
							e = p;
						}
					}
				}
				while ((e = p) != null && e != f);
			}
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super K> action)
		{
			TreeMap.TreeEntry<K, V> e;
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			if ((e = this.current) == null || e == this.fence)
			{
				return false;
			}
			this.current = successor(e);
			action.accept(e.key);
			return true;
		}
		
		@Override
		public int characteristics()
		{
			return (this.side == 0 ? Spliterator.SIZED : 0) | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
		}
		
		@Override
		public final Comparator<? super K> getComparator()
		{
			return this.tree.comparator;
		}
		
	}
	
	static final class ValueSpliterator<K, V> extends TreeMapSpliterator<K, V>implements Spliterator<V>
	{
		ValueSpliterator(TreeMap<K, V> tree, TreeMap.TreeEntry<K, V> origin, TreeMap.TreeEntry<K, V> fence, int side, int est)
		{
			super(tree, origin, fence, side, est);
		}
		
		@Override
		public ValueSpliterator<K, V> trySplit()
		{
			if (this.est < 0)
			{
				this.getEstimate();
			}
			int d = this.side;
			TreeMap.TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && this.tree.compare(e.key, s.key) < 0)
			{
				this.side = 1;
				return new ValueSpliterator<>(this.tree, e, this.current = s, -1, this.est >>>= 1);
			}
			return null;
		}
		
		@Override
		public void forEachRemaining(Consumer<? super V> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			TreeMap.TreeEntry<K, V> f = this.fence, e, p, pl;
			if ((e = this.current) != null && e != f)
			{
				this.current = f;
				do
				{
					action.accept(e.value);
					if ((p = e.right) != null)
					{
						while ((pl = p.left) != null)
						{
							p = pl;
						}
					}
					else
					{
						while ((p = e.parent) != null && e == p.right)
						{
							e = p;
						}
					}
				}
				while ((e = p) != null && e != f);
			}
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super V> action)
		{
			TreeMap.TreeEntry<K, V> e;
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			if ((e = this.current) == null || e == this.fence)
			{
				return false;
			}
			this.current = successor(e);
			action.accept(e.value);
			return true;
		}
		
		@Override
		public int characteristics()
		{
			return (this.side == 0 ? Spliterator.SIZED : 0) | Spliterator.ORDERED;
		}
	}
	
	static final class EntrySpliterator<K, V> extends TreeMapSpliterator<K, V>implements Spliterator<Entry<K, V>>
	{
		EntrySpliterator(TreeMap<K, V> tree, TreeMap.TreeEntry<K, V> origin, TreeMap.TreeEntry<K, V> fence, int side, int est)
		{
			super(tree, origin, fence, side, est);
		}
		
		@Override
		public EntrySpliterator<K, V> trySplit()
		{
			if (this.est < 0)
			{
				this.getEstimate();
			}
			int d = this.side;
			TreeMap.TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && this.tree.compare(e.key, s.key) < 0)
			{
				this.side = 1;
				return new EntrySpliterator<>(this.tree, e, this.current = s, -1, this.est >>>= 1);
			}
			return null;
		}
		
		@Override
		public void forEachRemaining(Consumer<? super Entry<K, V>> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			TreeMap.TreeEntry<K, V> f = this.fence, e, p, pl;
			if ((e = this.current) != null && e != f)
			{
				this.current = f;
				do
				{
					action.accept(e);
					if ((p = e.right) != null)
					{
						while ((pl = p.left) != null)
						{
							p = pl;
						}
					}
					else
					{
						while ((p = e.parent) != null && e == p.right)
						{
							e = p;
						}
					}
				}
				while ((e = p) != null && e != f);
			}
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Entry<K, V>> action)
		{
			TreeMap.TreeEntry<K, V> e;
			if (action == null)
			{
				throw new NullPointerException();
			}
			if (this.est < 0)
			{
				this.getEstimate();
			}
			if ((e = this.current) == null || e == this.fence)
			{
				return false;
			}
			this.current = successor(e);
			action.accept(e);
			return true;
		}
		
		@Override
		public int characteristics()
		{
			return (this.side == 0 ? Spliterator.SIZED : 0) | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
		}
		
		@Override
		public Comparator<Entry<K, V>> getComparator()
		{
			if (this.tree.comparator != null)
			{
				return Entry.comparingByKey(this.tree.comparator);
			}
			return (Comparator) Entry.<Comparable, V> comparingByKey();
		}
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return this.getEntry(key) != null;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		for (TreeEntry<K, V> e = this.getFirstEntry(); e != null; e = successor(e))
		{
			if (Objects.equals(value, e.value))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		TreeEntry<K, V> entry = this.getEntry(key);
		if (entry == null)
		{
			return false;
		}
		
		return Objects.equals(value, entry.value);
	}
	
	@Override
	public V get(Object key)
	{
		TreeEntry<K, V> p = this.getEntry(key);
		return p == null ? null : p.value;
	}
	
	final TreeEntry<K, V> getEntry(Object key)
	{
		if (this.comparator != null)
		{
			K k = (K) key;
			Comparator<? super K> cpr = this.comparator;
			if (cpr != null)
			{
				TreeEntry<K, V> p = this.root;
				while (p != null)
				{
					int cmp = cpr.compare(k, p.key);
					if (cmp < 0)
					{
						p = p.left;
					}
					else if (cmp > 0)
					{
						p = p.right;
					}
					else
					{
						return p;
					}
				}
			}
			return null;
		}
		if (key == null)
		{
			throw new NullPointerException();
		}
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key;
		TreeEntry<K, V> p = this.root;
		while (p != null)
		{
			int cmp = k.compareTo(p.key);
			if (cmp < 0)
			{
				p = p.left;
			}
			else if (cmp > 0)
			{
				p = p.right;
			}
			else
			{
				return p;
			}
		}
		return null;
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
		TreeEntry<K, V> t = this.root;
		if (t == null)
		{
			this.compare(key, key);
			
			this.root = new TreeEntry<>(key, value, null);
			this.size = 1;
			return null;
		}
		int cmp;
		TreeEntry<K, V> parent;
		
		Comparator<? super K> cpr = this.comparator;
		if (cpr != null)
		{
			do
			{
				parent = t;
				cmp = cpr.compare(key, t.key);
				if (cmp < 0)
				{
					t = t.left;
				}
				else if (cmp > 0)
				{
					t = t.right;
				}
				else
				{
					return t.setValue(value);
				}
			}
			while (t != null);
		}
		else
		{
			if (key == null)
			{
				throw new NullPointerException();
			}
			@SuppressWarnings("unchecked")
			Comparable<? super K> k = (Comparable<? super K>) key;
			do
			{
				parent = t;
				cmp = k.compareTo(t.key);
				if (cmp < 0)
				{
					t = t.left;
				}
				else if (cmp > 0)
				{
					t = t.right;
				}
				else
				{
					return t.setValue(value);
				}
			}
			while (t != null);
		}
		TreeEntry<K, V> e = new TreeEntry<>(key, value, parent);
		if (cmp < 0)
		{
			parent.left = e;
		}
		else
		{
			parent.right = e;
		}
		this.fixAfterInsertion(e);
		this.size++;
		return null;
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		if (this.contains(key, value))
		{
			return false;
		}
		
		this.put(key, value);
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
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (TreeEntry<K, V> e = this.getFirstEntry(); e != null; e = successor(e))
		{
			action.accept(e.key, e.value);
		}
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> function)
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
	
	final TreeEntry<K, V> getFirstEntry()
	{
		TreeEntry<K, V> p = this.root;
		if (p != null)
		{
			while (p.left != null)
			{
				p = p.left;
			}
		}
		return p;
	}
	
	final TreeEntry<K, V> getLastEntry()
	{
		TreeEntry<K, V> p = this.root;
		if (p != null)
		{
			while (p.right != null)
			{
				p = p.right;
			}
		}
		return p;
	}
	
	static <K, V> TreeMap.TreeEntry<K, V> successor(TreeEntry<K, V> t)
	{
		if (t == null)
		{
			return null;
		}
		else if (t.right != null)
		{
			TreeEntry<K, V> p = t.right;
			while (p.left != null)
			{
				p = p.left;
			}
			return p;
		}
		else
		{
			TreeEntry<K, V> p = t.parent;
			TreeEntry<K, V> ch = t;
			while (p != null && ch == p.right)
			{
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}
	
	static <K, V> TreeEntry<K, V> predecessor(TreeEntry<K, V> t)
	{
		if (t == null)
		{
			return null;
		}
		else if (t.left != null)
		{
			TreeEntry<K, V> p = t.left;
			while (p.right != null)
			{
				p = p.right;
			}
			return p;
		}
		else
		{
			TreeEntry<K, V> p = t.parent;
			TreeEntry<K, V> ch = t;
			while (p != null && ch == p.left)
			{
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}
	
	private static <K, V> boolean colorOf(TreeEntry<K, V> p)
	{
		return p == null ? BLACK : p.color;
	}
	
	private static <K, V> TreeEntry<K, V> parentOf(TreeEntry<K, V> p)
	{
		return p == null ? null : p.parent;
	}
	
	private static <K, V> void setColor(TreeEntry<K, V> p, boolean c)
	{
		if (p != null)
		{
			p.color = c;
		}
	}
	
	private static <K, V> TreeEntry<K, V> leftOf(TreeEntry<K, V> p)
	{
		return p == null ? null : p.left;
	}
	
	private static <K, V> TreeEntry<K, V> rightOf(TreeEntry<K, V> p)
	{
		return p == null ? null : p.right;
	}
	
	private void rotateLeft(TreeEntry<K, V> p)
	{
		if (p != null)
		{
			TreeEntry<K, V> r = p.right;
			p.right = r.left;
			if (r.left != null)
			{
				r.left.parent = p;
			}
			r.parent = p.parent;
			if (p.parent == null)
			{
				this.root = r;
			}
			else if (p.parent.left == p)
			{
				p.parent.left = r;
			}
			else
			{
				p.parent.right = r;
			}
			r.left = p;
			p.parent = r;
		}
	}
	
	private void rotateRight(TreeEntry<K, V> p)
	{
		if (p != null)
		{
			TreeEntry<K, V> l = p.left;
			p.left = l.right;
			if (l.right != null)
			{
				l.right.parent = p;
			}
			l.parent = p.parent;
			if (p.parent == null)
			{
				this.root = l;
			}
			else if (p.parent.right == p)
			{
				p.parent.right = l;
			}
			else
			{
				p.parent.left = l;
			}
			l.right = p;
			p.parent = l;
		}
	}
	
	private void fixAfterInsertion(TreeEntry<K, V> x)
	{
		x.color = RED;
		
		while (x != null && x != this.root && x.parent.color == RED)
		{
			if (parentOf(x) == leftOf(parentOf(parentOf(x))))
			{
				TreeEntry<K, V> y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED)
				{
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == rightOf(parentOf(x)))
					{
						x = parentOf(x);
						this.rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					this.rotateRight(parentOf(parentOf(x)));
				}
			}
			else
			{
				TreeEntry<K, V> y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED)
				{
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == leftOf(parentOf(x)))
					{
						x = parentOf(x);
						this.rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					this.rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
		this.root.color = BLACK;
	}
	
	private void deleteEntry(TreeEntry<K, V> p)
	{
		this.size--;
		
		if (p.left != null && p.right != null)
		{
			TreeEntry<K, V> s = successor(p);
			p.key = s.key;
			p.value = s.value;
			p = s;
		}
		
		TreeEntry<K, V> replacement = p.left != null ? p.left : p.right;
		
		if (replacement != null)
		{
			
			replacement.parent = p.parent;
			if (p.parent == null)
			{
				this.root = replacement;
			}
			else if (p == p.parent.left)
			{
				p.parent.left = replacement;
			}
			else
			{
				p.parent.right = replacement;
			}
			
			p.left = p.right = p.parent = null;
			
			if (p.color == BLACK)
			{
				this.fixAfterDeletion(replacement);
			}
		}
		else if (p.parent == null)
		{
			this.root = null;
		}
		else
		{
			if (p.color == BLACK)
			{
				this.fixAfterDeletion(p);
			}
			
			if (p.parent != null)
			{
				if (p == p.parent.left)
				{
					p.parent.left = null;
				}
				else if (p == p.parent.right)
				{
					p.parent.right = null;
				}
				p.parent = null;
			}
		}
	}
	
	private void fixAfterDeletion(TreeEntry<K, V> x)
	{
		while (x != this.root && colorOf(x) == BLACK)
		{
			if (x == leftOf(parentOf(x)))
			{
				TreeEntry<K, V> sib = rightOf(parentOf(x));
				
				if (colorOf(sib) == RED)
				{
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					this.rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}
				
				if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK)
				{
					setColor(sib, RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(rightOf(sib)) == BLACK)
					{
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						this.rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					this.rotateLeft(parentOf(x));
					x = this.root;
				}
			}
			else
			{
				TreeEntry<K, V> sib = leftOf(parentOf(x));
				
				if (colorOf(sib) == RED)
				{
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					this.rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}
				
				if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK)
				{
					setColor(sib, RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(leftOf(sib)) == BLACK)
					{
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						this.rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					this.rotateRight(parentOf(x));
					x = this.root;
				}
			}
		}
		
		setColor(x, BLACK);
	}
	
	private void buildFromSorted(int size, Iterator<?> it, java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException
	{
		this.size = size;
		this.root = this.buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
	}
	
	private final TreeEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator<?> it, java.io.ObjectInputStream str, V defaultVal)
			throws java.io.IOException, ClassNotFoundException
	{
		
		if (hi < lo)
		{
			return null;
		}
		
		int mid = lo + hi >>> 1;
		
		TreeEntry<K, V> left = null;
		if (lo < mid)
		{
			left = this.buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
		}
		
		// extract key and/or value from iterator or stream
		K key;
		V value;
		if (it != null)
		{
			if (defaultVal == null)
			{
				TreeEntry<?, ?> entry = (TreeEntry<?, ?>) it.next();
				key = (K) entry.getKey();
				value = (V) entry.getValue();
			}
			else
			{
				key = (K) it.next();
				value = defaultVal;
			}
		}
		else
		{ // use stream
			key = (K) str.readObject();
			value = defaultVal != null ? defaultVal : (V) str.readObject();
		}
		
		TreeEntry<K, V> middle = new TreeEntry<>(key, value, null);
		
		// color nodes in non-full bottommost level red
		if (level == redLevel)
		{
			middle.color = RED;
		}
		
		if (left != null)
		{
			middle.left = left;
			left.parent = middle;
		}
		
		if (mid < hi)
		{
			TreeEntry<K, V> right = this.buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
			middle.right = right;
			right.parent = middle;
		}
		
		return middle;
	}
	
	private static int computeRedLevel(int sz)
	{
		int level = 0;
		for (int m = sz - 1; m >= 0; m = m / 2 - 1)
		{
			level++;
		}
		return level;
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		TreeMap<K, V> copy = new TreeMap();
		
		try
		{
			copy.buildFromSorted(this.size, this.iterator(), null, null);
		}
		catch (java.io.IOException cannotHappen)
		{
		}
		catch (ClassNotFoundException cannotHappen)
		{
		}
		
		return copy;
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new TreeMap();
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new ArrayMap<K, V>(this); // TODO immutable.TreeMap
	}
	
	@Override
	public String toString()
	{
		return Map.mapToString(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Map.mapEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Map.mapHashCode(this);
	}
}
