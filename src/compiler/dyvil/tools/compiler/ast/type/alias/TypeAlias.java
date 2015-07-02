package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class TypeAlias implements ITypeAlias
{
	protected Name name;
	protected IType type;
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
	}
	
	@Override
	public String toString()
	{
		return "type " + this.name + " = " + this.type;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("type ").append(this.name).append(Formatting.Field.keyValueSeperator);
		this.type.toString(prefix, buffer);
	}
}
