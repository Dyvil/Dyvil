package dyvilx.tools.compiler.ast.expression;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public class KeyCache
{
	// =============== Nested Classes ===============

	public static class Entry implements Comparable<Entry>
	{
		public int       key;
		public MatchCase matchCase;
		public Pattern   pattern;
		public Label     switchLabel;

		protected Entry next;

		public Entry(int key, MatchCase matchCase, Pattern pattern)
		{
			this.key = key;
			this.matchCase = matchCase;
			this.pattern = pattern;
		}

		protected Entry(int key)
		{
			this.key = key;
		}

		@Override
		public int compareTo(Entry o)
		{
			return Integer.compare(this.key, o.key);
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Entry && this.key == ((Entry) o).key;
		}

		@Override
		public int hashCode()
		{
			return this.key;
		}

		@Override
		public String toString()
		{
			return "Entry(" + this.key + " -> " + this.matchCase + ")";
		}
	}

	// =============== Fields ===============

	private TreeSet<Entry> entries;

	// =============== Constructors ===============

	public KeyCache()
	{
		this.entries = new TreeSet<>();
	}

	// =============== Properties ===============

	public int count()
	{
		return this.entries.size();
	}

	public SortedSet<Entry> entries()
	{
		return this.entries;
	}

	public int min()
	{
		return this.entries.isEmpty() ? 0 : this.entries.first().key;
	}

	public int max()
	{
		return this.entries.isEmpty() ? 0 : this.entries.last().key;
	}

	// =============== Contains ===============

	public boolean contains(int key)
	{
		return this.entries.contains(new Entry(key));
	}

	// =============== Iteration ===============

	public void forEachEntry(Consumer<Entry> action)
	{
		this.entries.forEach(top -> {
			for (Entry entry = top; entry != null; entry = entry.next)
			{
				action.accept(entry);
			}
		});
	}

	// ===============  ===============

	public Entry add(int key, MatchCase matchCase, Pattern pattern)
	{
		final Entry newEntry = new Entry(key, matchCase, pattern);

		final Entry existing = this.entries.floor(newEntry);
		if (existing != null && existing.key == newEntry.key)
		{
			newEntry.next = existing.next;
			existing.next = newEntry;
		}
		else
		{
			this.entries.add(newEntry);
		}

		return newEntry;
	}

	public SortedSet<Entry> uniqueEntries()
	{
		return this.entries;
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
