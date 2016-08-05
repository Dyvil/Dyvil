package dyvilx.lang.model.type;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

@LiteralConvertible.FromNil
@DyvilModifiers(Modifiers.OBJECT_CLASS)
public class NullType implements Type<Object>
{
	public static final NullType instance = new NullType();
	
	public static NullType apply()
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
		return "null";
	}
	
	@Override
	public String toString()
	{
		return "null";
	}
	
	@Override
	public String qualifiedName()
	{
		return "dyvil/lang/Null";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ldyvil/lang/Null;");
	}
}
