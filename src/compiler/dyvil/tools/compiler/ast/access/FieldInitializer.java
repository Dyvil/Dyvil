package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FieldInitializer extends ASTNode implements IValue, IValued
{
	public Variable	variable;
	
	public FieldInitializer(ICodePosition position, String name, IType type)
	{
		this.position = position;
		this.variable = new Variable(this.position, name, type);
	}
	
	@Override
	public int getValueType()
	{
		return VARIABLE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.variable.type.isPrimitive();
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.variable.type;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, this.variable.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.variable.type.equals(type))
		{
			return 3;
		}
		else if (this.variable.type.isSuperTypeOf(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.variable.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.variable.value;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.variable.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.variable.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.variable.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.variable.value.writeExpression(writer);
		writer.writeInsn(Opcodes.DUP);
		this.variable.writeSet(writer, null, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.variable.writeSet(writer, null, this.variable.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
