package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class BooleanValue implements IConstantValue
{
	public static final BooleanValue TRUE  = new BooleanValue(true);
	public static final BooleanValue FALSE = new BooleanValue(false);

	protected ICodePosition position;
	protected boolean       value;

	public BooleanValue(boolean value)
	{
		this.value = value;
	}

	public BooleanValue(ICodePosition position, boolean value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return BOOLEAN;
	}

	@Override
	public boolean booleanValue()
	{
		return this.value;
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSuperType(type, Types.BOOLEAN))
		{
			return this;
		}

		final IAnnotation annotation = type.getTheClass().getAnnotation(Types.FROMBOOLEAN_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.BOOLEAN) || type.getAnnotation(Types.FROMBOOLEAN_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		final int i = IConstantValue.super.getTypeMatch(type);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(Types.FROMBOOLEAN_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	@Override
	public Boolean toObject()
	{
		return this.value;
	}

	@Override
	public int stringSize()
	{
		return this.value ? 4 : 5;
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append(this.value);
		return true;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.value)
		{
			writer.visitLdcInsn(1);
		}
		else
		{
			writer.visitLdcInsn(0);
		}

		if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		if (this.value)
		{
			writer.visitJumpInsn(Opcodes.GOTO, dest);
		}
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		if (!this.value)
		{
			writer.visitJumpInsn(Opcodes.GOTO, dest);
		}
	}

	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
