package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface BooleanRef
{
	boolean get();

	void set(boolean value);

	@DyvilModifiers(Modifiers.INLINE)
	static boolean $times(BooleanRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(BooleanRef ref, boolean value)
	{
		ref.set(value);
	}
}
