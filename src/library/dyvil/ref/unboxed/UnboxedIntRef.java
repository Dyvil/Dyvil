package dyvil.ref.unboxed;

import dyvil.ref.IntRef;
import dyvil.ref.ObjectRef;

public class UnboxedIntRef implements IntRef
{
	private final ObjectRef<Integer> objectRef;

	public UnboxedIntRef(ObjectRef<Integer> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public int get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(int value)
	{
		this.objectRef.set(value);
	}
}
