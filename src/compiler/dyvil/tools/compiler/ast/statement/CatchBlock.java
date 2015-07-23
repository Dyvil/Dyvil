package dyvil.tools.compiler.ast.statement;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CatchBlock implements IValued, ITyped, IContext
{
	public ICodePosition	position;
	public IType			type;
	public Name				varName;
	public IValue			action;
	
	protected Variable variable;
	
	protected IContext context;
	
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
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.context.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.context.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return this.context.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
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
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return this.context.handleException(type);
	}
}
