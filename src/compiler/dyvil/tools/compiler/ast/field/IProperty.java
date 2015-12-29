package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;

public interface IProperty extends IField
{
	// Getter
	
	boolean hasGetter();

	void setGetterModifiers(ModifierSet modifiers);

	ModifierSet getGetterModifiers();

	void setGetter(IValue get);

	IValue getGetter();
	
	// Setter
	
	boolean hasSetter();

	void setSetterModifiers(ModifierSet modifiers);

	ModifierSet getSetterModifiers();

	void setSetter(IValue set);

	IValue getSetter();
}
