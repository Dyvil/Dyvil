package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ReturnStatement extends ASTNode implements IStatement, IValued
{
	public IValue		value;
	private IStatement	parent;
	
	public ReturnStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return RETURN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.value.isPrimitive();
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
		return this.value == null ? Types.VOID : this.value.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (this.value == null)
		{
			return type == Types.UNKNOWN || type == Types.VOID ? this : null;
		}
		IValue value1 = this.value.withType(type);
		if (value1 == null)
		{
			return null;
		}
		this.value = value1;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.value == null ? type == Types.UNKNOWN || type == Types.VOID : this.value.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.value == null)
		{
			return 0;
		}
		
		return this.value.getTypeMatch(type);
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.value.writeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.value != null)
		{
			this.value.writeExpression(writer);
			IType type = this.value.getType();
			writer.writeInsn(type.getReturnOpcode());
			return;
		}
		writer.writeInsn(Opcodes.RETURN);
	}
}
