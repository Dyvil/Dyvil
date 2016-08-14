package dyvil.ref.unboxed;

import dyvil.ref.ShortRef;
import dyvil.ref.ObjectRef;

public class UnboxedShortRef implements ShortRef
{
	private final ObjectRef<Short> objectRef;

	public UnboxedShortRef(ObjectRef<Short> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public short get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(short value)
	{
		this.objectRef.set(value);
	}
}
