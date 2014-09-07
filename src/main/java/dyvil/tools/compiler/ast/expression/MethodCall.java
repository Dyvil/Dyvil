package dyvil.tools.compiler.ast.expression;

import java.util.List;

import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.util.Modifiers;

public class MethodCall implements IValue
{
	public IValue instance;
	public IMethod descriptor;
	public List<IValue> args;
	
	public MethodCall(IMethod descriptor, List<IValue> args)
	{
		this(null, descriptor, args);
	}
	
	public MethodCall(IValue instance, IMethod descriptor, List<IValue> args)
	{
		if (descriptor.hasModifier(Modifiers.IMPLICIT))
		{
			args.add(0, instance);
			instance = null;
		}
		
		this.instance = instance;
		this.descriptor = descriptor;
		this.args = args;
	}
	
	@Override
	public boolean isConstant()
	{
		if (!this.instance.isConstant())
		{
			return false;
		}
		for (IValue arg : this.args)
		{
			if (!arg.isConstant())
			{
				return false;
			}
		}
		return false;
	}

	@Override
	public IValue fold()
	{
		// TODO Constant Folding
		return this;
	}

	@Override
	public Type getType()
	{
		return this.descriptor.getType();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}
