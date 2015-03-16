package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BoxedValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class InstanceOfOperator extends ASTNode implements IValue
{
	public IValue	value;
	public IType	type;
	
	public InstanceOfOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int getValueType()
	{
		return ISOF_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Type.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.BOOLEAN)
		{
			return this;
		}
		return type.isSuperTypeOf(Type.BOOLEAN) ? new BoxedValue(this, Type.BOOLEAN.boxMethod) : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.BOOLEAN || type.isSuperTypeOf(Type.BOOLEAN);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.BOOLEAN)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Type.BOOLEAN))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.type.isPrimitive())
		{
			markers.add(this.position, "instanceof.primitive");
		}
		else if (this.value.isType(this.type))
		{
			markers.add(this.position, "instanceof.unnecessary");
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.value.writeExpression(writer);
		writer.writeTypeInsn(Opcodes.INSTANCEOF, this.type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" <: ");
		this.type.toString(prefix, buffer);
	}
}
