package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CatchBlock implements IValued, ITyped, IDefaultContext
{
	public ICodePosition	position;
	public IType			type;
	public Name				varName;
	public IValue			action;
	
	protected Variable variable;
	
	public CatchBlock(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.action = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.action;
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
	public IDataMember resolveField(Name name)
	{
		if (this.varName == name)
		{
			if (this.variable == null)
			{
				this.variable = new Variable(this.type.getPosition());
				this.variable.name = this.varName;
				this.variable.type = this.type;
			}
			return this.variable;
		}
		
		return null;
	}
}
