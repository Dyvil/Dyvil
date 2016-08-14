package dyvil.ref.unboxed;

import dyvil.ref.LongRef;
import dyvil.ref.ObjectRef;

public class UnboxedLongRef implements LongRef
{
	private final ObjectRef<Long> objectRef;

	public UnboxedLongRef(ObjectRef<Long> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public long get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(long value)
	{
		this.objectRef.set(value);
	}
}
