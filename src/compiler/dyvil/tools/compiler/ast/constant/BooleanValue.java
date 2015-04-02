package dyvil.tools.compiler.ast.constant;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class BooleanValue extends ASTNode implements IConstantValue
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
	public int getValueType()
	{
		return BOOLEAN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.BOOLEAN)
		{
			return this;
		}
		return type.isSuperTypeOf(Types.BOOLEAN) ? new BoxedValue(this, Types.BOOLEAN.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.BOOLEAN)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.BOOLEAN))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public Boolean toObject()
	{
		return Boolean.valueOf(this.value);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.value)
		{
			writer.writeLDC(1);
		}
		else
		{
			writer.writeLDC(0);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		if (this.value)
		{
			writer.writeJumpInsn(Opcodes.GOTO, dest);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		if (!this.value)
		{
			writer.writeJumpInsn(Opcodes.GOTO, dest);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
