package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class Access implements NodeVisitor, NodeElement
{
	protected Name name;
	protected NodeElement element;
	
	private ICodePosition position;
	
	public Access(Name name)
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
	public void setName(Name name)
	{
		this.name = name;
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
	public NodeVisitor visitAccess(Name name)
	{
		Access access = new Access(name);
		this.element = access;
		return access;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append('.');
		this.element.toString(prefix, buffer);
	}
}
