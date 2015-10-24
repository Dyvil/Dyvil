package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class NodeAccess implements NodeVisitor, NodeElement
{
	protected Name name;
	protected NodeElement element;
	
	private ICodePosition position;
	
	public NodeAccess(Name name)
	{
		this.name = name;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public NodeVisitor visitNode(Name name)
	{
		Node node = new Node(name);
		this.element = node;
		return node;
	}
	
	@Override
	public ValueVisitor visitProperty(Name name)
	{
		Property property = new Property(name);
		this.element = property;
		return property;
	}
	
	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		NodeAccess access = new NodeAccess(name);
		this.element = access;
		return access;
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		this.element.accept(visitor.visitNodeAccess(this.name));
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append('.');
		this.element.toString(prefix, buffer);
	}
}
