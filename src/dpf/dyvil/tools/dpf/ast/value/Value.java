package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public interface Value extends IASTNode
{
	@Override
	default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	default void setPosition(ICodePosition position)
	{
	}
	
	@Override
	default void expandPosition(ICodePosition position)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer);
	
	public void accept(ValueVisitor visitor);
}
