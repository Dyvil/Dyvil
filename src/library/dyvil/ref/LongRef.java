package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface LongRef
{
	long get();

	void set(long value);

	@DyvilModifiers(Modifiers.INLINE)
	static long $times(LongRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times$eq(LongRef intRef, long value)
	{
		intRef.set(value);
	}
}
