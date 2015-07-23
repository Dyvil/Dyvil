package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

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
			markers.add(this.position, "wildcard.type");
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeDefaultValue(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("...");
	}
}
