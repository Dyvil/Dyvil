package dyvilx.tools.compiler.ast.context;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvil.lang.Name;

public interface IMemberContext extends IImplicitContext
{
	Package resolvePackage(Name name);

	IClass resolveClass(Name name);

	ITypeParameter resolveTypeParameter(Name name);

	IDataMember resolveField(Name name);

	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments);
}
