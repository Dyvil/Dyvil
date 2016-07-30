package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.ref.boxed.BoxedByteRef;
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

	@DyvilModifiers(Modifiers.INFIX)
	static ObjectRef<Byte> boxed(ByteRef byteRef)
	{
		return new BoxedByteRef(byteRef);
	}
}
