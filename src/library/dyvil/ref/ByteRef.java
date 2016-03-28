package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;

public interface ByteRef
{
	byte get();

	void set(byte value);

	@DyvilModifiers(Modifiers.INLINE)
	static byte $times(ByteRef intRef)
	{
		return intRef.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(ByteRef intRef, byte value)
	{
		intRef.set(value);
	}
}
