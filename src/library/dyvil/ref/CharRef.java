package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface CharRef
{
	char get();

	void set(char value);

	@DyvilModifiers(Modifiers.INLINE)
	static char $times(CharRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(CharRef intRef, char value)
	{
		intRef.set(value);
	}
}
