package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;

public interface IDefaultContext extends IStaticContext
{
	@Override
	default byte checkStatic()
	{
		return PASS;
	}

	@Override
	default DyvilCompiler getCompilationContext()
	{
		return null;
	}

	@Override
	default IDyvilHeader getHeader()
	{
		return null;
	}
	
	@Override
	default Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	default IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	default IType resolveType(Name name)
	{
		return null;
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
	default void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
	}
}
