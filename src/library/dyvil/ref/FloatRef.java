package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface FloatRef
{
	float get();

	void set(float value);

	@DyvilModifiers(Modifiers.INLINE)
	static float $times(FloatRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(FloatRef ref, float value)
	{
		ref.set(value);
	}
}
