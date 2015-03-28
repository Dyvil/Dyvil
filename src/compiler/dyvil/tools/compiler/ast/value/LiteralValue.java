package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class LiteralValue implements IValue
{
	private SingleArgument	argument;
	private IType			type;
	
	private IConstructor	constructor;
	
	public LiteralValue(IType type, IValue literal)
	{
		this.argument = new SingleArgument(literal);
		this.type = type;
	}
	
	@Override
	public int getValueType()
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
		ConstructorMatch match = this.type.resolveConstructor(this.argument);
		if (match == null)
		{
			IValue value = this.argument.getFirstValue();
			Marker marker = markers.create(value.getPosition(), "literal.constructor");
			marker.addInfo("Literal Type: " + value.getType());
			marker.addInfo("Target Type: " + this.type);
		}
		else
		{
			this.constructor = match.constructor;
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
		this.constructor.writeCall(writer, this.argument, null);
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
