package dyvilx.tools.compiler.ast.context;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.field.IAccessible;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;

public interface IStaticContext extends IContext
{
	@Override
	default boolean hasStaticAccess()
	{
		return true;
	}

	@Override
	default byte checkStatic()
	{
		return TRUE;
	}

	@Override
	IHeaderUnit getHeader();

	@Override
	default IClass getThisClass()
	{
		return null;
	}

	@Override
	default IType getThisType()
	{
		return null;
	}

	@Override
	Package resolvePackage(Name name);

	@Override
	IHeaderUnit resolveHeader(Name name);

	@Override
	IClass resolveClass(Name name);

	@Override
	void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments);

	@Override
	default ITypeParameter resolveTypeParameter(Name name)
	{
		return null;
	}

	@Override
	default IOperator resolveOperator(Name name, byte type)
	{
		return null;
	}

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	@Override
	default void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	default byte checkException(IType type)
	{
		return FALSE;
	}

	@Override
	default IType getReturnType()
	{
		return null;
	}

	@Override
	default boolean isMember(IVariable variable)
	{
		return false;
	}

	@Override
	default IDataMember capture(IVariable variable)
	{
		return variable;
	}

	@Override
	default IAccessible getAccessibleThis(IClass type)
	{
		return null;
	}

	@Override
	default IValue resolveImplicit(IType type)
	{
		return null;
	}
}
