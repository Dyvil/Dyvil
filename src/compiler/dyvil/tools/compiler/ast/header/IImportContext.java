package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public interface IImportContext extends IImplicitContext
{
	Package resolvePackage(Name name);

	IDyvilHeader resolveHeader(Name name);

	ITypeAlias resolveTypeAlias(Name name, int arity);

	IOperator resolveOperator(Name name, int type);

	IClass resolveClass(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);
}
