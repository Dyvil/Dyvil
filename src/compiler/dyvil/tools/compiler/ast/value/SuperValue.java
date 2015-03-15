package dyvil.tools.compiler.ast.value;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class SuperValue extends ASTNode implements IConstantValue
{
	public IType	type;
	
	public SuperValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public SuperValue(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public int getValueType()
	{
		return SUPER;
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
		return Type.isSuperType(type, this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, this.type);
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
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			if (context.isStatic())
			{
				markers.add(this.position, "access.super.static");
			}
			else
			{
				IType thisType = context.getThisType();
				this.type = thisType.getSuperType();
				if (this.type == null)
				{
					Marker marker = markers.create(this.position, "access.super.type");
					marker.addInfo("Enclosing Type: " + thisType);
					
				}
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("super");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeInsn(Opcodes.ARETURN);
	}
}
