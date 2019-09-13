package dyvil.reflect.types;

import dyvil.annotation.internal.ClassParameters;
import dyvil.util.Option;

@ClassParameters(names = { "type" })
public class OptionType<T> implements Type<Option<T>>
{
	protected final Type<T> type;

	public static <T> OptionType<T> apply(Type<T> type)
	{
		return new OptionType<>(type);
	}

	public OptionType(Type<T> type)
	{
		this.type = type;
	}

	public Type<T> type()
	{
		return this.type;
	}

	@Override
	public Class<Option<T>> erasure()
	{
		return (Class<Option<T>>) (Class) Option.class;
	}

	@Override
	public int typeArgumentCount()
	{
		return 1;
	}

	@Override
	public <R> Type<R> typeArgument(int index)
	{
		return (Type<R>) (Type) this.type;
	}

	@Override
	public String name()
	{
		return "Option";
	}

	@Override
	public String qualifiedName()
	{
		return "dyvil.util.Option";
	}

	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/util/Option;");
	}

	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/util/Option<");
		this.type.appendGenericSignature(builder);
		builder.append(">;");
	}

	@Override
	public String toString()
	{
		return this.type.toString() + "?";
	}

	@Override
	public void toString(StringBuilder builder)
	{
		this.type.toString(builder);
		builder.append('?');
	}
}
