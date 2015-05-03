package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class LiteralExpression implements IValue
{
	private SingleArgument	argument;
	private IType			type;
	
	private IMethod			method;
	
	public LiteralExpression(IType type, IValue literal)
	{
		this.argument = new SingleArgument(literal);
		this.type = type;
	}
	
	@Override
	public int valueTag()
	{
		return BOXED;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type.isSuperTypeOf(this.type) ? 3 : 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.argument.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.argument.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IMethod match = IContext.resolveMethod(markers, this.type, null, Name.apply, this.argument);
		if (match == null)
		{
			IValue value = this.argument.getFirstValue();
			markers.add(value.getPosition(), "literal.method", value.getType().toString(), this.type.toString());
		}
		else
		{
			this.method = match;
			match.checkArguments(markers, null, this.argument, null);
		}
		
		this.argument.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.argument.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.argument.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.method.writeCall(writer, null, this.argument, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.argument.getFirstValue().writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.argument.getFirstValue().toString(prefix, buffer);
	}
}
