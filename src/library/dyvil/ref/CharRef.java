package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.ref.boxed.BoxedCharRef;
import dyvil.reflect.Modifiers;

public interface CharRef
{
	char get();

	void set(char value);

	@DyvilModifiers(Modifiers.INLINE)
	static char $times(CharRef ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static void $times_$eq(CharRef ref, char value)
	{
		ref.set(value);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static ObjectRef<Character> boxed(CharRef charRef)
	{
		return new BoxedCharRef(charRef);
	}
}
