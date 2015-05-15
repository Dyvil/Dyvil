package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.NestedMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MethodStatement extends ASTNode implements IValue
{
	private NestedMethod	method;
	
	@Override
	public int valueTag()
	{
		return NESTED_METHOD;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.resolveTypes(markers, context);
		this.method.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.resolve(markers, context);
		this.method.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.checkTypes(markers, context);
		this.method.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.method.context = context;
		this.method.check(markers, context);
		this.method.context = null;
	}
	
	@Override
	public IValue foldConstants()
	{
		this.method.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.method.toString(prefix, buffer);
	}
}
