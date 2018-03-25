package dyvilx.tools.compiler.ast.context;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.lang.Name;

public interface IDefaultContext extends IStaticContext
{
	IDefaultContext DEFAULT = new IDefaultContext() {};

	@Override
	default boolean isThisAvailable()
	{
		return false;
	}

	@Override
	default boolean isConstructor()
	{
		return false;
	}

	@Override
	default DyvilCompiler getCompilationContext()
	{
		return null;
	}

	@Override
	default IHeaderUnit getHeader()
	{
		return null;
	}

	@Override
	default Package resolvePackage(Name name)
	{
		return null;
	}

	@Override
	default IHeaderUnit resolveHeader(Name name)
	{
		return null;
	}

	@Override
	default IClass resolveClass(Name name)
	{
		return null;
	}

	@Override
	default void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
	}

	@Override
	default IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	default IType getReturnType()
	{
		return null;
	}

	@Override
	default byte checkException(IType type)
	{
		return PASS;
	}

	@Override
	default void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	@Override
	default void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}
}
