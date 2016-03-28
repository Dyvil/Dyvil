package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ShortRef
{
	short get();

	void set(short value);

	@DyvilModifiers(Modifiers.INLINE)
	static short $times(ShortRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times$eq(ShortRef intRef, short value)
	{
		intRef.set(value);
	}
}
