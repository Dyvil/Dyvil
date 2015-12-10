package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharValue implements IConstantValue
{
	private static final byte UNDEFINED = 0;
	private static final byte CHAR      = 1;
	private static final byte STRING    = 2;
	
	protected ICodePosition position;
	protected String        value;
	
	private byte type;
	
	public CharValue(String value)
	{
		this.value = value;
	}
	
	public CharValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	public CharValue(ICodePosition position, String value, boolean forceChar)
	{
		this.position = position;
		this.value = value;
		this.type = forceChar ? CHAR : STRING;
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
		return IValue.CHAR;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == CHAR)
		{
			return Types.CHAR;
		}
		return Types.STRING;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IClass iclass = type.getTheClass();
		IAnnotation annotation = null;
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (type == Types.CHAR || type.isSuperTypeOf(Types.CHAR))
			{
				this.type = CHAR;
				return this;
			}
			
			annotation = iclass.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS);
			if (annotation != null)
			{
				return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
			}
		}
		
		if (this.type == CHAR)
		{
			return null;
		}
		
		if (type == Types.STRING || type.isSuperTypeOf(Types.STRING))
		{
			this.type = STRING;
			return this;
		}
		
		annotation = iclass.getAnnotation(Types.STRING_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (type == Types.CHAR || type.isSuperTypeOf(Types.CHAR))
			{
				return true;
			}
			if (iclass.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null)
			{
				return true;
			}
		}
		
		if (this.type == CHAR)
		{
			return false;
		}
		if (type == Types.STRING || type.isSuperTypeOf(Types.STRING))
		{
			return true;
		}
		return iclass.getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		IClass iclass = type.getTheClass();
		if (this.value.length() == 1 && this.type != STRING)
		{
			float distance = type.getSubTypeDistance(Types.CHAR);
			if (distance > 0F)
			{
				return distance;
			}
			
			if (iclass.getAnnotation(Types.CHAR_CONVERTIBLE_CLASS) != null)
			{
				return CONVERSION_MATCH;
			}
		}
		
		if (this.type == CHAR)
		{
			return 0F;
		}
		float distance = type.getSubTypeDistance(Types.STRING);
		if (distance > 0F)
		{
			return distance;
		}
		if (iclass.getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null)
		{
			return CONVERSION_MATCH + 1F;
		}
		return 0F;
	}
	
	@Override
	public int intValue()
	{
		return this.value.charAt(0);
	}
	
	@Override
	public long longValue()
	{
		return this.value.charAt(0);
	}
	
	@Override
	public float floatValue()
	{
		return this.value.charAt(0);
	}
	
	@Override
	public double doubleValue()
	{
		return this.value.charAt(0);
	}
	
	@Override
	public String stringValue()
	{
		return this.value;
	}
	
	@Override
	public Object toObject()
	{
		if (this.type == CHAR)
		{
			return Character.valueOf(this.value.charAt(0));
		}
		return this.value;
	}
	
	@Override
	public int stringSize()
	{
		return this.value.length();
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
		if (this.type == CHAR)
		{
			writer.writeLDC(this.value.charAt(0));

			if (type == Types.VOID)
			{
				writer.writeInsn(Opcodes.IRETURN);
			}
			else if (type != null)
			{
				Types.CHAR.writeCast(writer, type, this.getLineNumber());
			}
			return;
		}

		writer.writeLDC(this.value);
		if (type == Types.VOID)
		{
			writer.writeInsn(Opcodes.ARETURN);
		}
		else if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendCharLiteral(this.value, buffer);
	}
}
