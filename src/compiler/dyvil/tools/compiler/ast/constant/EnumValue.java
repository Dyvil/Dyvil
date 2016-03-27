package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class EnumValue implements IConstantValue, INamed
{
	public IType type;
	public Name  name;
	
	public EnumValue()
	{
	}
	
	public EnumValue(IType type, Name name)
	{
		this.type = type;
		this.name = name;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return ENUM_ACCESS;
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.type.isResolved())
		{
			this.type = this.type.resolveType(markers, context);
		}
		return this.isType(type) ? this : null;
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
	public int stringSize()
	{
		return this.name.qualified.length();
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		String owner = this.type.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		writer.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);

		if (type != null)
		{
			this.type.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('.');
		buffer.append(this.name);
	}
}
