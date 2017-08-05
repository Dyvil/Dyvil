package dyvilx.tools.compiler.ast.field;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public interface IProperty extends IClassMember
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.PROPERTY;
	}

	void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

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

	SourcePosition getInitializerPosition();

	void setInitializerPosition(SourcePosition position);
}
