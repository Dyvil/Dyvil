package dyvil.tools.compiler.ast.context;

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
	IDyvilHeader getHeader();
	
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
	IClass resolveClass(Name name);
	
	@Override
	IType resolveType(Name name);
	
	@Override
	default ITypeParameter resolveTypeVariable(Name name)
	{
		return null;
	}

	@Override
	default IOperator resolveOperator(Name name)
	{
		return null;
	}

	@Override
	IDataMember resolveField(Name name);
	
	@Override
	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);
	
	@Override
	default void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
	default IDataMember capture(IVariable capture)
	{
		return capture;
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
