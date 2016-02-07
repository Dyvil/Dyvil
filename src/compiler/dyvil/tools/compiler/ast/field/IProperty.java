package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IProperty extends IClassMember
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.PROPERTY;
	}

	void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments);

	// Getter

	IMethod getGetter();

	IMethod initGetter();

	// Setter

	void setSetterParameterName(Name name);

	IMethod getSetter();

	IMethod initSetter();

	// Initializer

	IValue getInitializer();

	void setInitializer(IValue value);

	ICodePosition getInitializerPosition();

	void setInitializerPosition(ICodePosition position);
}
