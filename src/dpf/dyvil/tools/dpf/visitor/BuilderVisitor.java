package dyvil.tools.dpf.visitor;

import dyvil.tools.parsing.Name;

public interface BuilderVisitor
{
	ValueVisitor visitParameter(Name name);
	
	NodeVisitor visitNode();
	
	void visitEnd();
}
