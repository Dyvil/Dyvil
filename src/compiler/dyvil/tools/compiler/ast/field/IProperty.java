package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IProperty extends IField
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
}
