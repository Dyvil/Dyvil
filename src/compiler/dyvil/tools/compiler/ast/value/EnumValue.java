package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class EnumValue extends ASTNode implements IConstantValue
{
	public IType	type;
	public String	name;
	
	public EnumValue(IType type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public EnumValue(ICodePosition position, Type type, String name)
	{
		this.position = position;
		this.type = type;
		this.name = name;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public int getValueType()
	{
		return ENUM;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		String owner = this.type.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.visitGetStatic(owner, name, desc, this.type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.type).append('.').append(this.name);
	}
}
