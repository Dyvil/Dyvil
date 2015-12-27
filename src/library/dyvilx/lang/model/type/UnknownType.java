package dyvilx.lang.model.type;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.NilConvertible;
import dyvil.reflect.Modifiers;

@NilConvertible
@DyvilModifiers(Modifiers.OBJECT_CLASS)
public class UnknownType<T> implements Type<T>
{
	public static final UnknownType instance = new UnknownType();
	
	public static <T> UnknownType<T> apply()
	{
		return instance;
	}
	
	@Override
	public Class erasure()
	{
		return null;
	}
	
	@Override
	public String name()
	{
		return "auto";
	}
	
	@Override
	public String qualifiedName()
	{
		return "auto";
	}
	
	@Override
	public String toString()
	{
		return "auto";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ljava/lang/Object;");
	}
}
