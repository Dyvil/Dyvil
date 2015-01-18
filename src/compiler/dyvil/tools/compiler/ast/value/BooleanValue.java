package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BooleanValue extends ASTNode implements IConstantValue
{
	public static final BooleanValue	TRUE	= new BooleanValue(true);
	public static final BooleanValue	FALSE	= new BooleanValue(false);
	
	public boolean						value;
	
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
	public Type getType()
	{
		return Type.BOOLEAN;
	}
	
	@Override
	public int getValueType()
	{
		return BOOLEAN;
	}
	
	@Override
	public Boolean toObject()
	{
		return Boolean.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter visitor)
	{
		if (this.value)
		{
			visitor.visitInsn(Opcodes.ICONST_1, Type.INT);
		}
		else
		{
			visitor.visitInsn(Opcodes.ICONST_0, Type.INT);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.value ? 1231 : 1237);
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof BooleanValue))
		{
			return false;
		}
		BooleanValue other = (BooleanValue) obj;
		if (this.value != other.value)
		{
			return false;
		}
		return true;
	}
}
