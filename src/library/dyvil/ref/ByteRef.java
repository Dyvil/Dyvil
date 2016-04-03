package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ByteRef
{
	byte get();

	void set(byte value);

	@DyvilModifiers(Modifiers.INLINE)
	static byte $times(ByteRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(ByteRef ref, byte value)
	{
		ref.set(value);
	}
}
