package dyvil.reflect.types;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

@DyvilModifiers(Modifiers.OBJECT_CLASS)
public class NoneType implements Type<Object>
{
	public static final NoneType instance = new NoneType();
	
	public static NoneType apply()
	{
		return instance;
	}
	
	@Override
	public Class<Object> erasure()
	{
		return null;
	}
	
	@Override
	public String name()
	{
		return "none";
	}
	
	@Override
	public String toString()
	{
		return "none";
	}
	
	@Override
	public String qualifiedName()
	{
		return "dyvil/lang/internal/None";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/lang/internal/None;");
	}
}
