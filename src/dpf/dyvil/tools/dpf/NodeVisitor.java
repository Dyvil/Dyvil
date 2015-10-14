package dyvil.tools.dpf;

import dyvil.tools.parsing.Name;

public interface NodeVisitor
{
	public NodeVisitor visitNode(Name name);
	
	public ValueVisitor visitProperty(Name name);
}
