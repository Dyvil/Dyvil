package dyvil.ref.unboxed;

import dyvil.ref.DoubleRef;
import dyvil.ref.ObjectRef;

public class UnboxedDoubleRef implements DoubleRef
{
	private final ObjectRef<Double> objectRef;

	public UnboxedDoubleRef(ObjectRef<Double> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public double get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(double value)
	{
		this.objectRef.set(value);
	}
}
