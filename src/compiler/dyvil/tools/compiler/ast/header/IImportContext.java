package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;

public interface IImportContext
{
	Package resolvePackage(Name name);

	IClass resolveClass(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments);

	void getImplicitMatches(MethodMatchList list, IValue value, IType targetType);
}
