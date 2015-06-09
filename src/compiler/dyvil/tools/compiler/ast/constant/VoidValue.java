package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class VoidValue extends ASTNode implements IConstantValue
{
	public VoidValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return VOID;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public int stringSize()
	{
		return 0;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("()");
	}
}
