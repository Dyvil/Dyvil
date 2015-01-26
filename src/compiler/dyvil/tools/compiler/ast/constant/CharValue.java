package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CharValue extends ASTNode implements INumericValue
{
	public char	value;
	
	public CharValue(char value)
	{
		this.value = value;
	}
	
	public CharValue(ICodePosition position, char value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return CHAR;
	}
	
	@Override
	public Type getType()
	{
		return Type.CHAR;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Type.CHAR ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.CHAR;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return type == Type.CHAR ? 3 : 0;
	}
	
	@Override
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public Character toObject()
	{
		return Character.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter visitor)
	{
		visitor.visitLdcInsn(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitLdcInsn(this.value);
		writer.visitInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('\'').append(this.value).append('\'');
	}
}
