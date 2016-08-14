package dyvil.ref.unboxed;

import dyvil.ref.CharRef;
import dyvil.ref.ObjectRef;

public class UnboxedCharRef implements CharRef
{
	private final ObjectRef<Character> objectRef;

	public UnboxedCharRef(ObjectRef<Character> objectRef)
	{
		this.objectRef = objectRef;
	}

	@Override
	public char get()
	{
		return this.objectRef.get();
	}

	@Override
	public void set(char value)
	{
		this.objectRef.set(value);
	}
}
