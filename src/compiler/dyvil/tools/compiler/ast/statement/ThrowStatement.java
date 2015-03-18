package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ThrowStatement extends ASTNode implements IValue, IValued
{
	private IValue	value;
	
	public ThrowStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public int getValueType()
	{
		return THROW;
	}
	
	@Override
	public IType getType()
	{
		return Type.NONE;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.VOID;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IValue value1 = this.value.withType(TryStatement.getThrowable());
		if (value1 == null)
		{
			Marker marker = markers.create(this.value.getPosition(), "throw.type");
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
		}
		
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.value.writeExpression(writer);
		writer.writeInsn(Opcodes.ATHROW);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.value.writeExpression(writer);
		writer.writeInsn(Opcodes.ATHROW);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("throw ");
		this.value.toString(prefix, buffer);
	}
}
