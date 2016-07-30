package dyvil.ref.boxed;

import dyvil.ref.FloatRef;
import dyvil.ref.ObjectRef;

public class BoxedFloatRef implements ObjectRef<Float>
{
	private final FloatRef floatRef;

	public BoxedFloatRef(FloatRef floatRef)
	{
		this.floatRef = floatRef;
	}

	@Override
	public Float get()
	{
		return this.floatRef.get();
	}

	@Override
	public void set(Float value)
	{
		this.floatRef.set(value);
	}
}
