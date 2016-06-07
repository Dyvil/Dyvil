package dyvil.tools.compiler.ast.context;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;

public interface IMemberContext extends IImplicitContext
{
	Package resolvePackage(Name name);

	IClass resolveClass(Name name);

	ITypeParameter resolveTypeParameter(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments);

	@Override
	void getImplicitMatches(MethodMatchList list, IValue value, IType targetType);

	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);
}
