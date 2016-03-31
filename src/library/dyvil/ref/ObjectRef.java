package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ObjectRef<T>
{
	T get();

	void set(T value);

	@DyvilModifiers(Modifiers.INLINE)
	static <T> T $times(ObjectRef<T> ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static <T> void $times_$eq(ObjectRef<T> ref, T value)
	{
		ref.set(value);
	}
}
