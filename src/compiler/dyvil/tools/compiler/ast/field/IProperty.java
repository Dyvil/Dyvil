package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;

public interface IProperty extends IField
{
	// Getter
	
	boolean hasGetter();
	
	void setGetterModifiers(int modifiers);
	
	void addGetterModifier(int modifier);
	
	int getGetterModifiers();
	
	void setGetter(IValue get);
	
	IValue getGetter();
	
	// Setter
	
	boolean hasSetter();
	
	void setSetterModifiers(int modifiers);
	
	void addSetterModifier(int modifier);
	
	int getSetterModifiers();
	
	void setSetter(IValue set);
	
	IValue getSetter();
}
