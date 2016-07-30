package dyvil.ref.boxed;

import dyvil.ref.DoubleRef;
import dyvil.ref.ObjectRef;

public class BoxedDoubleRef implements ObjectRef<Double>
{
	private final DoubleRef doubleRef;

	public BoxedDoubleRef(DoubleRef doubleRef)
	{
		this.doubleRef = doubleRef;
	}

	@Override
	public Double get()
	{
		return this.doubleRef.get();
	}

	@Override
	public void set(Double value)
	{
		this.doubleRef.set(value);
	}
}
