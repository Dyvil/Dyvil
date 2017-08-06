package dyvilx.tools.compiler.ast.imports;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.lang.Name;

public interface IImportContext extends IImplicitContext
{
	Package resolvePackage(Name name);

	IHeaderUnit resolveHeader(Name name);

	void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments);

	IOperator resolveOperator(Name name, byte type);

	IClass resolveClass(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);
}
