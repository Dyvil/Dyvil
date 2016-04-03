package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ShortRef
{
	short get();

	void set(short value);

	@DyvilModifiers(Modifiers.INLINE)
	static short $times(ShortRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(ShortRef ref, short value)
	{
		ref.set(value);
	}
}
