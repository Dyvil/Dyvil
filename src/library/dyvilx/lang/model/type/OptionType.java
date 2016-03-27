package dyvilx.lang.model.type;

import dyvil.annotation._internal.ClassParameters;
import dyvil.util.Option;

@ClassParameters(names = { "type" })
public class OptionType implements Type
{
	protected final Type type;

	public static OptionType apply(Type type)
	{
		return new OptionType(type);
	}

	public OptionType(Type type)
	{
		this.type = type;
	}

	public Type type()
	{
		return this.type;
	}

	@Override
	public Class erasure()
	{
		return Option.class;
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
