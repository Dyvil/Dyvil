package dyvil.reflect.types;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

@DyvilModifiers(Modifiers.OBJECT_CLASS)
public class UnknownType<T> implements Type<T>
{
	public static final UnknownType instance = new UnknownType();
	
	public static <T> UnknownType<T> apply()
	{
		return (UnknownType<T>) instance;
	}
	
	@Override
	public Class<T> erasure()
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
