package dyvil.tools.compiler.ast.context;

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
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public interface IStaticContext extends IContext
{
	@Override
	default boolean isStatic()
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
	ITypeAlias resolveTypeAlias(Name name, int arity);
	
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
	default IValue getImplicit()
	{
		return null;
	}
}
