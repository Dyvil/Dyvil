package dyvilx.tools.compiler.ast.expression;

import dyvil.collection.Collection;
import dyvil.collection.mutable.TreeSet;
import dyvil.math.MathUtils;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.IPattern;

public class KeyCache
{
	public static class Entry implements Comparable<Entry>
	{
		public int       key;
		public MatchCase matchCase;
		public IPattern  pattern;
		public Label     switchLabel;

		protected Entry next;

		public Entry(int key, MatchCase matchCase, IPattern pattern)
		{
			this.key = key;
			this.matchCase = matchCase;
			this.pattern = pattern;
		}

		@Override
		public int compareTo(Entry o)
		{
			return java.lang.Integer.compare(this.key, o.key);
		}

		@Override
		public String toString()
		{
			return "Entry(" + this.key + " -> " + this.matchCase + ")";
		}
	}

	private Entry[]           entries;
	private int               uniqueKeys;
	private Collection<Entry> uniqueEntries;

	public KeyCache(int cases)
	{
		this.entries = new Entry[MathUtils.nextPowerOf2(cases)];
	}

	public boolean contains(int key)
	{
		int index = index(key, this.entries.length);
		for (Entry entry = this.entries[index]; entry != null; entry = entry.next)
		{
			if (entry.key == key)
			{
				return true;
			}
		}
		return false;
	}

	private static int index(int key, int len) {return key & (len - 1);}

	public void add(int key, MatchCase matchCase, IPattern pattern)
	{
		this.uniqueEntries = null;

		final Entry newEntry = new Entry(key, matchCase, pattern);

		final int index = index(key, this.entries.length);
		final Entry topEntry = this.entries[index];

		for (Entry entry = topEntry; entry != null; entry = entry.next)
		{
			if (entry.key == key)
			{
				newEntry.next = entry.next;
				entry.next = newEntry;
				return;
			}
		}

		newEntry.next = topEntry;
		this.entries[index] = newEntry;
		this.uniqueKeys++;
	}

	public int uniqueKeyCount()
	{
		return this.uniqueKeys;
	}

	public Collection<Entry> uniqueEntries()
	{
		if (this.uniqueEntries != null)
		{
			return this.uniqueEntries;
		}

		Collection<Entry> result = new TreeSet<>();
		for (Entry topEntry : this.entries)
		{
			if (topEntry == null)
			{
				continue;
			}

			int currentKey = topEntry.key;
			result.add(topEntry);
			for (Entry entry = topEntry; entry != null; entry = entry.next)
			{
				if (entry.key != currentKey)
				{
					currentKey = entry.key;
					result.add(entry);
				}
			}
		}

		return this.uniqueEntries = result;
	}

	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder().append("{\n");
		for (Entry topEntry : this.uniqueEntries())
		{
			if (topEntry == null)
			{
				continue;
			}

			final int key = topEntry.key;
			stringBuilder.append(key).append(" -> [\n");
			for (Entry entry = topEntry; entry != null && entry.key == key; entry = entry.next)
			{
				stringBuilder.append('\t').append(entry.matchCase).append(" @ ").append(entry.pattern).append('\n');
			}
			stringBuilder.append("]\n");
		}
		return stringBuilder.append('}').toString();
	}
}
