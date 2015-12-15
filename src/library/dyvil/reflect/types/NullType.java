package dyvil.reflect.types;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.Type;
import dyvil.lang.literal.NilConvertible;
import dyvil.reflect.Modifiers;

@NilConvertible
@DyvilModifiers(Modifiers.OBJECT_CLASS)
public class NullType implements Type<Object>
{
	public static final NullType instance = new NullType();
	
	public static NullType apply()
	{
		return instance;
	}
	
	@Override
	public Class<Object> getTheClass()
	{
		return null;
	}
	
	@Override
	public String getName()
	{
		return "null";
	}
	
	@Override
	public String toString()
	{
		return "null";
	}
	
	@Override
	public String getQualifiedName()
	{
		return "dyvil/lang/Null";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/lang/Null;");
	}
}
