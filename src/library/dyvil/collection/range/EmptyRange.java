package dyvil.collection.range;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Range;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

import java.util.Iterator;
import java.util.function.Consumer;

@LiteralConvertible.FromNil
@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptyRange<T> implements Range<T>
{
	private static final long serialVersionUID = 5914222536371440711L;

	public static final EmptyRange instance = new EmptyRange();

	@NonNull
	public static <E> EmptyRange<E> apply()
	{
		return (EmptyRange<E>) instance;
	}

	private EmptyRange()
	{
	}

	@NonNull
	@Override
	public Range<T> asHalfOpen()
	{
		return this;
	}

	@NonNull
	@Override
	public Range<T> asClosed()
	{
		return this;
	}

	@Override
	public T first()
	{
		return null;
	}

	@Override
	public T last()
	{
		return null;
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean isHalfOpen()
	{
		return false;
	}

	@NonNull
	@Override
	public Iterator<T> iterator()
	{
		return (Iterator<T>) EmptyIterator.instance;
	}

	@Override
	public void forEach(@NonNull Consumer<? super T> action)
	{
	}

	@Override
	public boolean contains(Object o)
	{
		return false;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
	}

	@NonNull
	@Override
	public Range<T> copy()
	{
		return this;
	}

	@NonNull
	@Override
	public String toString()
	{
		return "[]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Range && ((Range) obj).size() == 0;
	}

	@Override
	public int hashCode()
	{
		return 0;
	}

	@NonNull
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}

	@NonNull
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}
