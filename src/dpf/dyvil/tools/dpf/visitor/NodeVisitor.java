package dyvil.tools.dpf.visitor;

import dyvil.tools.parsing.Name;

public interface NodeVisitor
{
	public NodeVisitor visitNode(Name name);
	
	public ValueVisitor visitProperty(Name name);

	public NodeVisitor visitAccess(Name name);
}
