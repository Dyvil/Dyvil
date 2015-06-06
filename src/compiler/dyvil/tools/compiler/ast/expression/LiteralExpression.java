package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class LiteralExpression implements IValue
{
	private IArguments	arguments;
	private IType		type;
	
	private IMethod		method;
	
	public LiteralExpression(IType type, IValue literal)
	{
		this.arguments = new SingleArgument(literal);
		this.type = type;
	}
	
	public LiteralExpression(IType type, IArguments arguments)
	{
		this.type = type;
		this.arguments = arguments;
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
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IMethod match = IContext.resolveMethod(this.type, null, Name.apply, this.arguments);
		if (match == null)
		{
			IValue value = this.arguments.getFirstValue();
			StringBuilder builder = new StringBuilder();
			this.arguments.typesToString(builder);
			markers.add(value.getPosition(), "literal.method", value.getType().getName(), this.type.toString(), builder);
		}
		else
		{
			this.method = match;
			GenericData data = match.getGenericData(null, null, this.arguments);
			match.checkArguments(markers, null, context, null, this.arguments, data);
			this.type = match.getType().getConcreteType(data);
		}
		
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, null, this.arguments, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.arguments.getFirstValue().writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.arguments.getFirstValue().toString(prefix, buffer);
	}
}
