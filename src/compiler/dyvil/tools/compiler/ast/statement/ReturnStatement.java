package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ReturnStatement extends ASTNode implements IStatement, IValued
{
	protected IValue	value;
	
	public ReturnStatement(ICodePosition position)
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
	public IType getType()
	{
		return this.value == null ? Type.VOID : this.value.getType();
	}
	
	@Override
	public int getValueType()
	{
		return RETURN;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.value != null)
		{
			return this.value.foldConstants();
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value != null)
		{
			buffer.append("return ");
			this.value.toString("", buffer);
		}
		else
		{
			buffer.append("return");
		}
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.value != null)
		{
			this.value.writeExpression(writer);
			IType type = this.value.getType();
			writer.visitInsn(type.getReturnOpcode(), type);
		}
		else
		{
			writer.visitInsn(Opcodes.RETURN, null);
		}
	}
}
