package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;

public interface IProperty extends IField
{
	// Getter
	
	public boolean hasGetter();
	
	public void setGetterModifiers(int modifiers);
	
	public void addGetterModifier(int modifier);
	
	public int getGetterModifiers();
	
	public void setGetter(IValue get);
	
	public IValue getGetter();
	
	// Setter
	
	public boolean hasSetter();
	
	public void setSetterModifiers(int modifiers);
	
	public void addSetterModifier(int modifier);
	
	public int getSetterModifiers();
	
	public void setSetter(IValue set);
	
	public IValue getSetter();
}
