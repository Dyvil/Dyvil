package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN) ? this : null;
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
			markers.add(I18n.createMarker(this.value.getPosition(), "nullcheck.primitive"));
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
		
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, label1);
		writer.writeLDC(0);
		writer.writeJumpInsn(Opcodes.GOTO, label2);
		writer.writeLabel(label1);
		writer.writeLDC(1);
		writer.writeLabel(label2);

		if (type == Types.VOID)
		{
			writer.writeInsn(Opcodes.IRETURN);
		}
		else if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNULL : Opcodes.IFNONNULL, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.value.writeExpression(writer, Types.OBJECT);
		writer.writeJumpInsn(this.isNull ? Opcodes.IFNONNULL : Opcodes.IFNULL, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(this.isNull ? " == null" : " != null");
	}
}
