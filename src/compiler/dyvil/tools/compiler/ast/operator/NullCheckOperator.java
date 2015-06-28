package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import org.objectweb.asm.Label;

public final class NullCheckOperator implements IValue
{
	private IValue	value;
	private boolean	isNull;
	
	public NullCheckOperator(IValue value, boolean isNull)
	{
		this.value = value;
		this.isNull = isNull;
	}
	
	@Override
	public int valueTag()
	{
		return NULLCHECK;
	}
	
	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.BOOLEAN ? this : IValue.autoBox(this, Types.BOOLEAN, type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type == Types.BOOLEAN ? 3 : 0;
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
			markers.add(this.value.getPosition(), "nullcheck.primitive");
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
		this.value.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.value.writeExpression(writer);
		Label label1 = new Label();
		Label label2 = new Label();
		
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, label1);
		writer.writeLDC(0);
		writer.writeJumpInsn(Opcodes.GOTO, label2);
		writer.writeLabel(label1);
		writer.writeLDC(1);
		writer.writeLabel(label2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer);
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer);
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNONNULL : Opcodes.IFNULL, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(this.isNull ? " == null" : " != null");
	}
}
