package dyvil.collection.impl;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

public abstract class AbstractTreeMap<K, V> implements Map<K, V>
{
	protected static final class TreeEntry<K, V> implements dyvil.collection.Entry<K, V>
	{
		private static final long serialVersionUID = -8592912850607607269L;
		
		public transient K					key;
		public transient V					value;
		protected transient TreeEntry<K, V>	left	= null;
		protected transient TreeEntry<K, V>	right	= null;
		protected transient TreeEntry<K, V>	parent;
		protected transient boolean			color	= BLACK;
		
		protected TreeEntry(K key, V value, TreeEntry<K, V> parent)
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
		
		@Override
		public boolean equals(Object o)
		{
			return Entry.entryEquals(this, o);
		}
		
		@Override
		public int hashCode()
		{
			return Entry.entryHashCode(this);
		}
		
		@Override
		public String toString()
		{
			return this.key + " -> " + this.value;
		}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException
		{
			out.defaultWriteObject();
			
			out.writeObject(this.key);
			out.writeObject(this.value);
			out.writeObject(this.left);
			out.writeObject(this.right);
			out.writeObject(this.parent);
			out.writeBoolean(this.color);
		}
		
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			in.defaultReadObject();
			
			this.key = (K) in.readObject();
			this.value = (V) in.readObject();
			this.left = (TreeEntry) in.readObject();
			this.right = (TreeEntry) in.readObject();
			this.parent = (TreeEntry) in.readObject();
			this.color = in.readBoolean();
		}
	}
	
	protected abstract class TreeEntryIterator<T> implements Iterator<T>
	{
		TreeEntry<K, V>	next;
		TreeEntry<K, V>	lastReturned;
		
		protected TreeEntryIterator(TreeEntry<K, V> first)
		{
			this.lastReturned = null;
			this.next = first;
		}
		
		@Override
		public final boolean hasNext()
		{
			return this.next != null;
		}
		
		protected final TreeEntry<K, V> nextEntry()
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
		
		protected final TreeEntry<K, V> prevEntry()
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
			AbstractTreeMap.this.deleteEntry(this.lastReturned);
			this.lastReturned = null;
		}
	}
	
	private static final long serialVersionUID = 4299609156116845922L;
	
	private static final boolean	RED		= false;
	private static final boolean	BLACK	= true;
	
	protected transient final Comparator<? super K>	comparator;
	protected transient TreeEntry<K, V>				root;
	protected transient int							size;
	
	public AbstractTreeMap()
	{
		this.comparator = null;
	}
	
	public AbstractTreeMap(Comparator<? super K> comparator)
	{
		this.comparator = comparator;
	}
	
	public AbstractTreeMap(Map<? extends K, ? extends V> map, Comparator<? super K> comparator)
	{
		this.comparator = comparator;
		
		if (map instanceof AbstractTreeMap)
		{
			this.buildFromSorted(map.size(), map.iterator());
			return;
		}
		
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.putUnsafe(entry.getKey(), entry.getValue());
		}
	}
	
	public Comparator<? super K> comparator()
	{
		return this.comparator;
	}
	
	protected static final int compare(Comparator comparator, Object k1, Object k2)
	{
		return comparator == null ? ((Comparable) k1).compareTo(k2) : comparator.compare(k1, k2);
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isSorted()
	{
		if (this.comparator == null)
		{
			return true;
		}
		return Map.super.isSorted();
	}
	
	@Override
	public boolean isSorted(Comparator<? super K> comparator)
	{
		if (comparator == this.comparator)
		{
			return true;
		}
		return Map.super.isSorted(comparator);
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new TreeEntryIterator(this.getFirstEntry())
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
		return new TreeEntryIterator<K>(this.getFirstEntry())
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
		return new TreeEntryIterator<V>(this.getFirstEntry())
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
		final AbstractTreeMap<K, V>	tree;
		TreeEntry<K, V>				current;
		TreeEntry<K, V>				fence;
		int							side;
		int							est;
		
		TreeMapSpliterator(AbstractTreeMap<K, V> tree, TreeEntry<K, V> origin, TreeEntry<K, V> fence, int side, int est)
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
			AbstractTreeMap<K, V> t;
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
		KeySpliterator(AbstractTreeMap<K, V> tree, TreeEntry<K, V> origin, TreeEntry<K, V> fence, int side, int est)
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
			TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && compare(this.tree.comparator, e.key, s.key) < 0)
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
			TreeEntry<K, V> f = this.fence, e, p, pl;
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
			TreeEntry<K, V> e;
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
		ValueSpliterator(AbstractTreeMap<K, V> tree, TreeEntry<K, V> origin, TreeEntry<K, V> fence, int side, int est)
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
			TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && compare(this.tree.comparator, e.key, s.key) < 0)
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
			TreeEntry<K, V> f = this.fence, e, p, pl;
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
			TreeEntry<K, V> e;
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
		EntrySpliterator(AbstractTreeMap<K, V> tree, TreeEntry<K, V> origin, TreeEntry<K, V> fence, int side, int est)
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
			TreeEntry<K, V> e = this.current, f = this.fence,
					s = e == null || e == f ? null : d == 0 ? this.tree.root : d > 0 ? e.right : d < 0 && f != null ? f.left : null;
			if (s != null && s != e && s != f && compare(this.tree.comparator, e.key, s.key) < 0)
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
			TreeEntry<K, V> f = this.fence, e, p, pl;
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
			TreeEntry<K, V> e;
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
	
	@Override
	public Option<V> getOption(Object key)
	{
		TreeEntry<K, V> p = this.getEntry(key);
		return p == null ? None.instance : new Some(p.value);
	}
	
	protected final V putUnsafe(K key, V value)
	{
		TreeEntry<K, V> t = this.root;
		if (t == null)
		{
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
					V oldValue = t.value;
					t.value = value;
					return oldValue;
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
					V oldValue = t.value;
					t.value = value;
					return oldValue;
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
	
	protected final TreeEntry<K, V> getEntry(Object key)
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
	
	protected final TreeEntry<K, V> getFirstEntry()
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
	
	protected static <K, V> TreeEntry<K, V> successor(TreeEntry<K, V> t)
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
	
	protected static <K, V> TreeEntry<K, V> predecessor(TreeEntry<K, V> t)
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
	
	protected void fixAfterInsertion(TreeEntry<K, V> x)
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
	
	protected void deleteEntry(TreeEntry<K, V> p)
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
	
	protected void buildFromSorted(int size, Iterator<? extends Entry<? extends K, ? extends V>> iterator)
	{
		this.size = size;
		this.root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), iterator);
	}
	
	private static final <K, V> TreeEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel,
			Iterator<? extends Entry<? extends K, ? extends V>> iterator)
	{
		if (hi < lo)
		{
			return null;
		}
		
		int mid = lo + hi >>> 1;
		
		TreeEntry<K, V> left = null;
		if (lo < mid)
		{
			left = buildFromSorted(level + 1, lo, mid - 1, redLevel, iterator);
		}
		
		// extract key and/or value from iterator
		Entry<? extends K, ? extends V> entry = iterator.next();
		TreeEntry<K, V> middle = new TreeEntry<K, V>(entry.getKey(), entry.getValue(), null);
		
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
			TreeEntry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, iterator);
			middle.right = right;
			right.parent = middle;
		}
		
		return middle;
	}
	
	protected void buildFromSorted(int size, java.io.ObjectInputStream str)
	{
		this.size = size;
		try
		{
			this.root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), str);
		}
		catch (IOException | ClassNotFoundException ex)
		{
			// ignored
		}
	}
	
	private static final <K, V> TreeEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, java.io.ObjectInputStream str)
			throws java.io.IOException, ClassNotFoundException
	{
		if (hi < lo)
		{
			return null;
		}
		
		int mid = (lo + hi) >>> 1;
		
		TreeEntry<K, V> left = null;
		if (lo < mid)
		{
			left = buildFromSorted(level + 1, lo, mid - 1, redLevel, str);
		}
		
		// extract key and/or value from stream
		K key = (K) str.readObject();
		V value = (V) str.readObject();
		
		TreeEntry<K, V> middle = new TreeEntry<K, V>(key, value, null);
		
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
			TreeEntry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, str);
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
	public java.util.Map<K, V> toJava()
	{
		java.util.TreeMap<K, V> map = new java.util.TreeMap<>(this.comparator);
		for (TreeEntry<K, V> first = this.getFirstEntry(); first != null; first = successor(first))
		{
			map.put(first.key, first.value);
		}
		return map;
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeInt(this.size);
		
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			out.writeObject(entry.key);
			out.writeObject(entry.value);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		this.buildFromSorted(in.readInt(), in);
	}
}
