package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.expression.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class CombiningContext implements IContext
{
	private final IContext inner;
	private final IContext outer;

	public CombiningContext(IContext inner, IContext outer)
	{
		this.inner = inner;
		this.outer = outer;
	}

	@Override
	public IContext pop()
	{
		return this.outer;
	}

	@Override
	public byte checkStatic()
	{
		final byte innerResult = this.inner.checkStatic();
		if (innerResult != PASS)
		{
			return innerResult;
		}

		return this.outer.checkStatic();
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.outer.getCompilationContext();
	}

	@Override
	public IHeaderUnit getHeader()
	{
		IHeaderUnit header = this.inner.getHeader();
		return header == null ? this.outer.getHeader() : header;
	}

	@Override
	public IClass getThisClass()
	{
		IClass iclass = this.inner.getThisClass();
		return iclass == null ? this.outer.getThisClass() : iclass;
	}

	@Override
	public IType getThisType()
	{
		IType type = this.inner.getThisType();
		return type == null ? this.outer.getThisType() : type;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		Package pack = this.inner.resolvePackage(name);
		return pack == null ? this.outer.resolvePackage(name) : pack;
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		final IHeaderUnit inner = this.inner.resolveHeader(name);
		return inner != null ? inner : this.outer.resolveHeader(name);
	}

	@Override
	public IClass resolveClass(Name name)
	{
		IClass iclass = this.inner.resolveClass(name);
		return iclass == null ? this.outer.resolveClass(name) : iclass;
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		final ITypeAlias inner = this.inner.resolveTypeAlias(name, arity);
		return inner == null ? this.outer.resolveTypeAlias(name, arity) : inner;
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		ITypeParameter typeVar = this.inner.resolveTypeParameter(name);
		return typeVar == null ? this.outer.resolveTypeParameter(name) : typeVar;
	}

	@Override
	public IOperator resolveOperator(Name name, byte type)
	{
		final IOperator inner = this.inner.resolveOperator(name, type);
		if (inner == null || inner.getType() != type)
		{
			final IOperator outer = this.outer.resolveOperator(name, type);
			if (outer != null && outer.getType() == type)
			{
				return outer;
			}
		}

		return inner;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = this.inner.resolveField(name);
		return field == null ? this.outer.resolveField(name) : field;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		this.inner.getMethodMatches(list, receiver, name, arguments);

		if (!list.hasCandidate())
		{
			this.outer.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.inner.getImplicitMatches(list, value, targetType);

		if (!list.hasCandidate())
		{
			this.outer.getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
	{
		this.inner.getConstructorMatches(list, arguments);

		if (!list.hasCandidate())
		{
			this.outer.getConstructorMatches(list, arguments);
		}
	}

	@Override
	public byte checkException(IType type)
	{
		final byte innerResult = this.inner.checkException(type);
		if (innerResult != PASS)
		{
			return innerResult;
		}

		return this.outer.checkException(type);
	}

	@Override
	public IType getReturnType()
	{
		final IType innerReturnType = this.inner.getReturnType();
		return innerReturnType != null ? innerReturnType : this.outer.getReturnType();
	}

	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		IAccessible i = this.inner.getAccessibleThis(type);
		return i == null ? this.outer.getAccessibleThis(type) : i;
	}

	@Override
	public IValue getImplicit()
	{
		final IValue innerImplicit = this.inner.getImplicit();
		return innerImplicit == null ? this.outer.getImplicit() : innerImplicit;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.inner.isMember(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.inner.isMember(variable))
		{
			return variable;
		}
		if (this.outer.isMember(variable))
		{
			return this.inner.capture(variable);
		}

		IDataMember dm = this.outer.capture(variable);
		return dm.capture(this.inner);
	}
}
