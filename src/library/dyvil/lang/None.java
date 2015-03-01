package dyvil.lang;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class None implements Option
{
	public static final None	instance	= new None();
	
	private None()
	{
	}
	
	public static None apply()
	{
		return instance;
	}
	
	@Override
	public Object get()
	{
		throw new NoSuchElementException("None");
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public boolean isDefined()
	{
		return false;
	}
	
	@Override
	public void ifPresent(Consumer consumer)
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
		return obj == this;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}
