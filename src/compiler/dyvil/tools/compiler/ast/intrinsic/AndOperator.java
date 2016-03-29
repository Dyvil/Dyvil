package dyvil.tools.compiler.ast.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class AndOperator extends AbstractValue
{
	public IValue left;
	public IValue right;
	
	public AndOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
	}
	
	public AndOperator(ICodePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int valueTag()
	{
		return BOOLEAN_AND;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.left.hasSideEffects() || this.right.hasSideEffects();
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.left.checkTypes(markers, context);
		this.right.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.left.valueTag() == BOOLEAN && !this.left.booleanValue())
		{
			return BooleanValue.FALSE;
		}
		if (this.bothTrue())
		{
			return BooleanValue.TRUE;
		}
		
		this.left = this.left.foldConstants();
		this.right = this.right.foldConstants();
		
		return this;
	}
	
	private boolean bothTrue()
	{
		return this.left.valueTag() == BOOLEAN && this.left.booleanValue() && this.right.valueTag() == BOOLEAN
				&& this.right.booleanValue();
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.left = this.left.cleanup(context, compilableList);
		this.right = this.right.cleanup(context, compilableList);
		
		if (this.bothTrue())
		{
			return BooleanValue.TRUE;
		}
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		Label label = new Label();
		Label label2 = new Label();
		this.left.writeInvJump(writer, label);
		this.right.writeInvJump(writer, label);
		writer.visitLdcInsn(1);
		writer.visitJumpInsn(Opcodes.GOTO, label2);
		writer.visitLabel(label);
		writer.visitLdcInsn(0);
		writer.visitLabel(label2);

		if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.left.writeJump(writer, dest);
		this.right.writeJump(writer, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.left.writeInvJump(writer, dest);
		this.right.writeInvJump(writer, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" && ");
		this.right.toString(prefix, buffer);
	}
}
