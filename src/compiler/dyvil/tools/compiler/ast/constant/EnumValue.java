package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class EnumValue extends ASTNode implements IConstantValue, INamed
{
	public IType	type;
	public Name		name;
	
	public EnumValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public EnumValue(IType type, Name name)
	{
		this.type = type;
		this.name = name;
	}
	
	public EnumValue(ICodePosition position, Type type, Name name)
	{
		this.position = position;
		this.type = type;
		this.name = name;
	}
	
	@Override
	public int getValueType()
	{
		return ENUM;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type.isSuperTypeOf(this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.type.equals(type))
		{
			return 3;
		}
		else if (this.type.getTheClass().isSubTypeOf(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		String owner = this.type.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('.');
		buffer.append(this.name);
	}
}
