package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
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
