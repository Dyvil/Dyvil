package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class FieldAccess implements IValue
{
	public Object instance;
	public Field field;
	
	public FieldAccess(Field field)
	{
		this(null, field);
	}
	
	public FieldAccess(Object instance, Field field)
	{
		this.instance = instance;
		this.field = field;
	}

	@Override
	public boolean isConstant()
	{
		return this.field.getValue().isConstant();
	}

	@Override
	public IValue fold()
	{
		return this;
	}

	@Override
	public Type getType()
	{
		return this.field.getType();
	}
}
