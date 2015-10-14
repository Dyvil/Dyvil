package dyvil.tools.dpf.visitor;

import dyvil.tools.parsing.Name;

public interface BuilderVisitor
{
	public ValueVisitor visitParameter(int index, Name name);
	
	public NodeVisitor visitNode();
}
