package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class FieldAccess implements IValue
{
	public IValue instance;
	public IField field;
	
	public FieldAccess(IField field)
	{
		this(null, field);
	}
	
	public FieldAccess(IValue instance, IField field)
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
