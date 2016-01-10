package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IProperty extends IClassMember
{
	// Getter

	void setGetterValue(IValue value);

	void setGetterModifiers(ModifierSet modifiers);

	void setGetterPosition(ICodePosition position);
	
	IMethod getGetter();
	
	// Setter

	void setSetterValue(IValue value);

	void setSetterModifiers(ModifierSet modifiers);

	void setSetterPosition(ICodePosition position);

	void setSetterParameterName(Name name);

	IMethod getSetter();
	
	void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments);
}
