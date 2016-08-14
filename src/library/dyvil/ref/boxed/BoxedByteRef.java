package dyvil.ref.boxed;

import dyvil.ref.ByteRef;
import dyvil.ref.ObjectRef;

public class BoxedByteRef implements ObjectRef<Byte>
{
	private final ByteRef byteRef;

	public BoxedByteRef(ByteRef byteRef)
	{
		this.byteRef = byteRef;
	}

	@Override
	public Byte get()
	{
		return this.byteRef.get();
	}

	@Override
	public void set(Byte value)
	{
		this.byteRef.set(value);
	}
}
