package dyvil.ref;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.ref.unboxed.*;
import dyvil.reflect.Modifiers;

public interface ObjectRef<T>
{
	T get();

	void set(T value);

	// De-referencing operators

	@DyvilModifiers(Modifiers.INLINE)
	static <T> T $times(ObjectRef<T> ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static <T> void $times_$eq(ObjectRef<T> ref, T value)
	{
		ref.set(value);
	}

	// Unbox Conversion Methods

	@DyvilModifiers(Modifiers.INFIX)
	static BooleanRef unboxed_$_boolean(ObjectRef<Boolean> booleanRef)
	{
		return new UnboxedBooleanRef(booleanRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static ByteRef unboxed_$_byte(ObjectRef<Byte> byteRef)
	{
		return new UnboxedByteRef(byteRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static ShortRef unboxed_$_short(ObjectRef<Short> shortRef)
	{
		return new UnboxedShortRef(shortRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static CharRef unboxed_$_char(ObjectRef<Character> charRef)
	{
		return new UnboxedCharRef(charRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static IntRef unboxed_$_int(ObjectRef<Integer> intRef)
	{
		return new UnboxedIntRef(intRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static LongRef unboxed_$_long(ObjectRef<Long> longRef)
	{
		return new UnboxedLongRef(longRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static FloatRef unboxed_$_float(ObjectRef<Float> floatRef)
	{
		return new UnboxedFloatRef(floatRef);
	}

	@DyvilModifiers(Modifiers.INFIX)
	static DoubleRef unboxed_$_double(ObjectRef<Double> doubleRef)
	{
		return new UnboxedDoubleRef(doubleRef);
	}
}
