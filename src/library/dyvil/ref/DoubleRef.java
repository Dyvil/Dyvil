package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.ref.boxed.BoxedDoubleRef;
import dyvil.reflect.Modifiers;

public interface DoubleRef
{
	double get();

	void set(double value);

	@DyvilModifiers(Modifiers.INLINE)
	static double $times(DoubleRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(DoubleRef ref, double value)
	{
		ref.set(value);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static ObjectRef<Double> boxed(DoubleRef doubleRef)
	{
		return new BoxedDoubleRef(doubleRef);
	}
}
