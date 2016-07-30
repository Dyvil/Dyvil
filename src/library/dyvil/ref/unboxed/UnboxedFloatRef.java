package dyvil.ref.unboxed;

import dyvil.ref.FloatRef;
import dyvil.ref.ObjectRef;

public class UnboxedFloatRef implements FloatRef
{
	private final ObjectRef<Float> objectRef;

	public UnboxedFloatRef(ObjectRef<Float> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public float get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(float value)
	{
		this.objectRef.set(value);
	}
}
