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
	public boolean isPrimitive()
	{
		return true;
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
		if (type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN))
		{
			return this;
		}
		
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.BOOLEAN_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN)
				|| type.getTheClass().getAnnotation(Types.BOOLEAN_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type == Types.BOOLEAN)
		{
			return 1;
		}
		if (type.getTheClass().getAnnotation(Types.BOOLEAN_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.BOOLEAN);
	}
	
	@Override
	public Boolean toObject()
	{
		return Boolean.valueOf(this.value);
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
			writer.writeLDC(1);
		}
		else
		{
			writer.writeLDC(0);
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
			writer.writeJumpInsn(Opcodes.GOTO, dest);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		if (!this.value)
		{
			writer.writeJumpInsn(Opcodes.GOTO, dest);
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
