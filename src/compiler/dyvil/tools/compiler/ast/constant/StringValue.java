package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StringValue extends ASTNode implements IConstantValue
{
	public String	value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public StringValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return STRING;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return Type.STRING;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return Type.isSuperType(type, Type.STRING) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, Type.STRING);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.equals(Type.STRING))
		{
			return 3;
		}
		else if (Type.STRING_CLASS.isSubTypeOf(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public String toObject()
	{
		return this.value;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
		writer.visitInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}
