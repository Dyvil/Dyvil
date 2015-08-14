package dyvil.util;

import dyvil.annotation.object;
import dyvil.lang.literal.NilConvertible;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@NilConvertible
public final @object class None implements Option
{
	public static final None instance = new None();
	
	public static None apply()
	{
		return instance;
	}
	
	@Override
	public Object $bang()
	{
		throw new NoSuchElementException("None");
	}
	
	@Override
	public boolean $qmark()
	{
		return false;
	}
	
	@Override
	public void forEach(Consumer consumer)
	{
	}
	
	@Override
	public Option filter(Predicate predicate)
	{
		return this;
	}
	
	@Override
	public Option map(Function mapper)
	{
		return this;
	}
	
	@Override
	public Option flatMap(Function mapper)
	{
		return this;
	}
	
	@Override
	public Object orElse(Object other)
	{
		return other;
	}
	
	@Override
	public Object orElse(Supplier other)
	{
		return other.get();
	}
	
	@Override
	public String toString()
	{
		return "None";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}
