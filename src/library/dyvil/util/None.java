package dyvil.util;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@LiteralConvertible.FromNil
@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class None implements Option
{
	private static final long serialVersionUID = 1853796001996785317L;
	
	public static final None instance = new None();
	
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
	public boolean isPresent()
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
	public Object orElse(Object value)
	{
		return value;
	}
	
	@Override
	public Object orElse(Supplier supplier)
	{
		return supplier.get();
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
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}
