package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CatchBlock implements IValued, ITyped, IContext
{
	public ICodePosition	position;
	public IType			type;
	public String			varName;
	public IValue			action;
	
	protected Variable		variable;
	
	protected IContext		context;
	
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
	public IType getThisType()
	{
		return this.context.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.varName.equals(name))
		{
			if (this.variable == null)
			{
				this.variable = new Variable(this.type.getPosition());
				this.variable.name = this.variable.qualifiedName = this.varName;
				this.variable.type = this.type;
			}
			return new FieldMatch(this.variable, 1);
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
}
