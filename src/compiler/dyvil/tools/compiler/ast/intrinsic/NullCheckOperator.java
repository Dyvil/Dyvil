package dyvil.tools.compiler.ast.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class NullCheckOperator implements IValue
{
	private IValue  value;
	private boolean isNull;
	
	public NullCheckOperator(IValue value, boolean isNull)
	{
		this.value = value;
		this.isNull = isNull;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.value.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return NULLCHECK;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.value.hasSideEffects();
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
		
		if (this.value.isPrimitive())
		{
			markers.add(Markers.semantic(this.value.getPosition(), "nullcheck.primitive"));
		}
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		Label label1 = new Label();
		Label label2 = new Label();
		
		writer.visitJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, label1);
		writer.visitLdcInsn(0);
		writer.visitJumpInsn(Opcodes.GOTO, label2);
		writer.visitLabel(label1);
		writer.visitLdcInsn(1);
		writer.visitLabel(label2);

		if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		writer.visitJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		writer.visitJumpInsn(this.isNull ? Opcodes.IFNONNULL : Opcodes.IFNULL, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(this.isNull ? " == null" : " != null");
	}
}
