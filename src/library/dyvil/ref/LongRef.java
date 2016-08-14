package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.ref.boxed.BoxedLongRef;
import dyvil.reflect.Modifiers;

public interface LongRef
{
	long get();

	void set(long value);

	@DyvilModifiers(Modifiers.INLINE)
	static long $times(LongRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(LongRef ref, long value)
	{
		ref.set(value);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static ObjectRef<Long> boxed(LongRef longRef)
	{
		return new BoxedLongRef(longRef);
	}
}
