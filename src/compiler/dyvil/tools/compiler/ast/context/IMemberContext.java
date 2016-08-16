package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IImportContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;

public interface IMemberContext extends IImportContext
{
	@Override
	Package resolvePackage(Name name);

	@Override
	IClass resolveClass(Name name);

	ITypeParameter resolveTypeParameter(Name name);

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments);
}
