package dyvil.tools.dpf.visitor;

import dyvil.tools.parsing.Name;

public interface BuilderVisitor
{
	public ValueVisitor visitParameter(Name name);
	
	public NodeVisitor visitNode();
	
	public void visitEnd();
}
