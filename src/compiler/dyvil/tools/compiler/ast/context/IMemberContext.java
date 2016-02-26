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

public interface IMemberContext
{
	Package resolvePackage(Name name);

	IClass resolveClass(Name name);

	IType resolveType(Name name);

	ITypeParameter resolveTypeVariable(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);

	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);
}
