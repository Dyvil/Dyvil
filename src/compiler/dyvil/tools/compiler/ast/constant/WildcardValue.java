package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class WildcardValue implements IConstantValue
{
	public ICodePosition position;
	
	private IType type = Types.UNKNOWN;
	
	public WildcardValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return WILDCARD;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.type = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return 1;
	}
	
	@Override
	public int stringSize()
	{
		return this.type.getDefaultValue().stringSize();
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return this.type.getDefaultValue().toStringBuilder(builder);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type == Types.UNKNOWN)
		{
			markers.add(MarkerMessages.createMarker(this.position, "wildcard.type"));
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == Types.VOID)
		{
			return;
		}
		if (type != null)
		{
			type.writeDefaultValue(writer);
			return;
		}

		this.type.writeDefaultValue(writer);
	}

	@Override
	public String toString()
	{
		return "_";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
	}
}
