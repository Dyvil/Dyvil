package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface FloatRef
{
	float get();

	void set(float value);

	@DyvilModifiers(Modifiers.INLINE)
	static float $times(FloatRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(FloatRef intRef, float value)
	{
		intRef.set(value);
	}
}
