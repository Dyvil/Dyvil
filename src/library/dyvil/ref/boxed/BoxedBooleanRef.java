package dyvil.ref.boxed;

import dyvil.ref.BooleanRef;
import dyvil.ref.ObjectRef;

public class BoxedBooleanRef implements ObjectRef<Boolean>
{
	private final BooleanRef booleanRef;

	public BoxedBooleanRef(BooleanRef booleanRef)
	{
		this.booleanRef = booleanRef;
	}

	@Override
	public Boolean get()
	{
		return this.booleanRef.get();
	}

	@Override
	public void set(Boolean value)
	{
		this.booleanRef.set(value);
	}
}
