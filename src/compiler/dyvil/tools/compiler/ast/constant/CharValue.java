package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CharValue extends ASTNode implements INumericValue
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
	public int valueTag()
	{
		return CHAR;
	}
	
	@Override
	public Type getType()
	{
		return Types.CHAR;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.CHAR)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.CHAR) ? new BoxedValue(this, Types.CHAR.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.CHAR || type.isSuperTypeOf(Types.CHAR);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.CHAR)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.CHAR))
		{
			return 2;
		}
		return 0;
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
		visitor.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + 4);
		buffer.append('\'');
		switch (this.value)
		{
		case '\'':
			buffer.append("\\'");
			break;
		case '\\':
			buffer.append("\\\\");
			break;
		case '\n':
			buffer.append("\\n");
			break;
		case '\t':
			buffer.append("\\t");
			break;
		case '\r':
			buffer.append("\\r");
			break;
		case '\b':
			buffer.append("\\b");
			break;
		case '\f':
			buffer.append("\\f");
			break;
		default:
			buffer.append(this.value);
		}
		buffer.append('\'');
	}
}
