package dyvil.ref.unboxed;

import dyvil.ref.BooleanRef;
import dyvil.ref.ObjectRef;

public class UnboxedBooleanRef implements BooleanRef
{
	private final ObjectRef<Boolean> objectRef;

	public UnboxedBooleanRef(ObjectRef<Boolean> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public boolean get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(boolean value)
	{
		this.objectRef.set(value);
	}
}
