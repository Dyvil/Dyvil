package dyvil.ref.boxed;

import dyvil.ref.ObjectRef;
import dyvil.ref.ShortRef;

public class BoxedShortRef implements ObjectRef<Short>
{
	private final ShortRef shortRef;

	public BoxedShortRef(ShortRef shortRef)
	{
		this.shortRef = shortRef;
	}

	@Override
	public Short get()
	{
		return this.shortRef.get();
	}

	@Override
	public void set(Short value)
	{
		this.shortRef.set(value);
	}
}
