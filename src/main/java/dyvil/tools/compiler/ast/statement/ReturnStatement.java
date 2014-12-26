package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
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
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return this.value == null ? Type.VOID : this.value.getType();
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.applyState(state, context);
			if (state == CompilerState.FOLD_CONSTANTS)
			{
				return this.value;
			}
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
			Type type = this.value.getType();
			writer.visitInsn(type.getReturnOpcode(), type);
		}
		else
		{
			writer.visitInsn(Opcodes.RETURN, null);
		}
	}
}
