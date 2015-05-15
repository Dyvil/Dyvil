package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class NullValue implements IConstantValue
{
	private static NullValue	NULL;
	
	private ICodePosition		position;
	
	public NullValue()
	{
	}
	
	public NullValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public static NullValue getNull()
	{
		if (NULL == null)
		{
			NULL = new NullValue();
		}
		return NULL;
	}
	
	@Override
	public int valueTag()
	{
		return IValue.NULL;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return Types.UNKNOWN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type.isPrimitive() ? null : this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type.isPrimitive() ? 0 : 2;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public int stringSize()
	{
		return 4;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append("null");
		return true;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
		writer.writeInsn(Opcodes.RETURN);
	}
	
	@Override
	public String toString()
	{
		return "null";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
}
