package dyvil.ref.boxed;

import dyvil.ref.CharRef;
import dyvil.ref.ObjectRef;

public class BoxedCharRef implements ObjectRef<Character>
{
	private final CharRef charRef;

	public BoxedCharRef(CharRef charRef)
	{
		this.charRef = charRef;
	}

	@Override
	public Character get()
	{
		return this.charRef.get();
	}

	@Override
	public void set(Character value)
	{
		this.charRef.set(value);
	}
}
