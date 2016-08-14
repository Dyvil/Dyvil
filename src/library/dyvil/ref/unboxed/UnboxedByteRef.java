package dyvil.ref.unboxed;

import dyvil.ref.ByteRef;
import dyvil.ref.ObjectRef;

public class UnboxedByteRef implements ByteRef
{
	private final ObjectRef<Byte> objectRef;

	public UnboxedByteRef(ObjectRef<Byte> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public byte get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(byte value)
	{
		this.objectRef.set(value);
	}
}
