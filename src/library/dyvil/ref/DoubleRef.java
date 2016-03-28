package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface DoubleRef
{
	double get();

	void set(double value);

	@DyvilModifiers(Modifiers.INLINE)
	static double $times(DoubleRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(DoubleRef intRef, double value)
	{
		intRef.set(value);
	}
}
