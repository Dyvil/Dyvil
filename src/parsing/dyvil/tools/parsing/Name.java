package dyvil.tools.parsing;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.literal.StringConvertible;
import dyvil.tools.parsing.name.Qualifier;

@StringConvertible
public final class Name
{
	/**
	 * This is about the size of the CACHE after the REPL has been initialized. Eagerly creating a large Hash Table may
	 * improve performance by reducing the number of resize operations.
	 */
	private static final int CACHE_CAPACITY = 1024;

	private static final Map<String, Name> CACHE = new HashMap<>(CACHE_CAPACITY);

	public final String qualified;
	public final String unqualified;

	public static Name apply(String literal)
	{
		return from(literal);
	}

	public static Name wrap(Object value)
	{
		if (value.getClass() == Name.class)
		{
			return (Name) value;
		}
		return from(value.toString());
	}

	public Name(String qualified)
	{
		this.qualified = this.unqualified = qualified;
		CACHE.put(qualified, this);
	}

	public Name(String unqualified, String qualified)
	{
		this.qualified = qualified;
		this.unqualified = unqualified;

		CACHE.put(qualified, this);
		CACHE.put(unqualified, this);
	}

	public static Name from(String value)
	{
		Name name = CACHE.get(value);
		if (name != null)
		{
			return name;
		}

		return from(Qualifier.unqualify(value), Qualifier.qualify(value));
	}

	@Deprecated
	public static Name fromUnqualified(String unqualified)
	{
		return from(unqualified);
	}

	public static Name fromQualified(String qualified)
	{
		Name name = CACHE.get(qualified);
		if (name != null)
		{
			return name;
		}

		final String unqualified = Qualifier.unqualify(qualified);
		name = CACHE.get(unqualified);
		if (name != null) {
			return name;
		}

		return new Name(unqualified, qualified);
	}

	public static Name fromRaw(String value)
	{
		final Name name = CACHE.get(value);
		if (name != null)
		{
			return name;
		}

		return new Name(value);
	}

	public static Name from(String unqualified, String qualified)
	{
		final Name name = CACHE.get(qualified);
		if (name != null)
		{
			return name;
		}

		return new Name(unqualified, qualified);
	}

	public boolean equals(String name)
	{
		return this.qualified.equals(name);
	}

	public boolean startWith(String name)
	{
		return this.qualified.startsWith(name);
	}

	public boolean endsWith(String name)
	{
		return this.qualified.endsWith(name);
	}

	@Override
	public String toString()
	{
		return this.unqualified;
	}
}
