package dyvil.ref.boxed;

import dyvil.ref.LongRef;
import dyvil.ref.ObjectRef;

public class BoxedLongRef implements ObjectRef<Long>
{
	private final LongRef longRef;

	public BoxedLongRef(LongRef longRef)
	{
		this.longRef = longRef;
	}

	@Override
	public Long get()
	{
		return this.longRef.get();
	}

	@Override
	public void set(Long value)
	{
		this.longRef.set(value);
	}
}
