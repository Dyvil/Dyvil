package dyvil.ref.boxed;

import dyvil.ref.IntRef;
import dyvil.ref.ObjectRef;

public class BoxedIntRef implements ObjectRef<Integer>
{
	private final IntRef intRef;

	public BoxedIntRef(IntRef intRef)
	{
		this.intRef = intRef;
	}

	@Override
	public Integer get()
	{
		return this.intRef.get();
	}

	@Override
	public void set(Integer value)
	{
		this.intRef.set(value);
	}
}
