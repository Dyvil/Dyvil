package dyvil.ref;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.DyvilName;
import dyvil.annotation.internal.NonNull;
import dyvil.ref.unboxed.*;
import dyvil.reflect.Modifiers;

public interface ObjectRef<T>
{
	T get();

	void set(T value);

	// De-referencing operators

	@DyvilModifiers(Modifiers.INLINE)
	static <T> T $times(@NonNull ObjectRef<T> ref)
	{
		return ref.get();
	}

	@DyvilModifiers(Modifiers.INLINE | Modifiers.INFIX)
	static <T> void $times_$eq(@NonNull ObjectRef<T> ref, T value)
	{
		ref.set(value);
	}

	// Unbox Conversion Methods

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static BooleanRef unboxedBoolean(ObjectRef<Boolean> booleanRef)
	{
		return new UnboxedBooleanRef(booleanRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static ByteRef unboxedByte(ObjectRef<Byte> byteRef)
	{
		return new UnboxedByteRef(byteRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static ShortRef unboxedShort(ObjectRef<Short> shortRef)
	{
		return new UnboxedShortRef(shortRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static CharRef unboxedChar(ObjectRef<Character> charRef)
	{
		return new UnboxedCharRef(charRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static IntRef unboxedInt(ObjectRef<Integer> intRef)
	{
		return new UnboxedIntRef(intRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static LongRef unboxedLong(ObjectRef<Long> longRef)
	{
		return new UnboxedLongRef(longRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static FloatRef unboxedFloat(ObjectRef<Float> floatRef)
	{
		return new UnboxedFloatRef(floatRef);
	}

	@NonNull
	@DyvilName("unboxed")
	@DyvilModifiers(Modifiers.INFIX)
	static DoubleRef unboxedDouble(ObjectRef<Double> doubleRef)
	{
		return new UnboxedDoubleRef(doubleRef);
	}
}
