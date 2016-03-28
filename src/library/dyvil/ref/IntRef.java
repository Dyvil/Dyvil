package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface IntRef
{
	int get();

	void set(int value);

	@DyvilModifiers(Modifiers.INLINE)
	static int $times(IntRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(IntRef intRef, int value)
	{
		intRef.set(value);
	}
}
