package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ObjectRef<T>
{
	T get();

	void set(T value);

	@DyvilModifiers(Modifiers.INLINE)
	static <T> T $times(ObjectRef<T> intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static <T> void $times$eq(ObjectRef<T> intRef, T value)
	{
		intRef.set(value);
	}
}
