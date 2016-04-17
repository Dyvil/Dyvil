package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class CombiningContext implements IContext
{
	private final IContext inner;
	private final IContext outer;

	public CombiningContext(IContext context1, IContext context2)
	{
		this.inner = context1;
		this.outer = context2;
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
	public IDyvilHeader getHeader()
	{
		IDyvilHeader header = this.inner.getHeader();
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
	public IOperator resolveOperator(Name name, int type)
	{
		final IOperator inner = this.inner.resolveOperator(name, type);
		return inner != null ? inner : this.outer.resolveOperator(name, type);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = this.inner.resolveField(name);
		return field == null ? this.outer.resolveField(name) : field;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.inner.getMethodMatches(list, instance, name, arguments);

		if (list.isEmpty())
		{
			this.outer.getMethodMatches(list, instance, name, arguments);
		}
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		this.inner.getConstructorMatches(list, arguments);

		if (list.isEmpty())
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
