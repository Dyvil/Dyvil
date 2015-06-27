package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;

public interface IProperty extends IField, IClassMember
{
	public void setGetter(IValue get);
	
	public IValue getGetter();
	
	public void setSetter(IValue set);
	
	public IValue getSetter();
}
