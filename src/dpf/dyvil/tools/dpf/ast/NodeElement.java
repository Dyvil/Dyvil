package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;

public interface NodeElement extends IASTNode
{
	public Name getName();
	
	public void setName(Name name);
	
	public void accept(NodeVisitor visitor);
}
