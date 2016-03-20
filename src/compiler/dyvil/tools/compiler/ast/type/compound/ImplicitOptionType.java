package dyvil.tools.compiler.ast.type.compound;

import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ImplicitOptionType extends OptionType
{
	public ImplicitOptionType(IType type)
	{
		super(type);
	}

	@Override
	public boolean isConvertibleTo(IType type)
	{
		return Types.isSuperType(type, this.type);
	}

	@Override
	public IValue convertValueTo(IValue value, IType targetType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isConvertibleTo(targetType))
		{
			return null;
		}

		return new MethodCall(value.getPosition(), value, LazyFields.GET_METHOD, EmptyArguments.INSTANCE);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType type = this.type.getConcreteType(context);
		if (type != this.type)
		{
			return new ImplicitOptionType(type);
		}
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.type.getMethodMatches(list, instance, name, arguments);
		super.getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public String toString()
	{
		return this.type.toString() + '!';
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('!');
	}
}
