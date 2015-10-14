package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.visitor.NodeVisitor;

public class DPFFile extends Node
{
	public DPFFile()
	{
		super(null);
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		for (NodeElement element : this.elements)
		{
			element.accept(visitor);
		}
		for (Node node : this.nodes)
		{
			node.accept(visitor);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (NodeElement p : this.elements)
		{
			p.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		for (Node n : this.nodes)
		{
			buffer.append('\n');
			n.toString(prefix, buffer);
			buffer.append('\n');
		}
	}
}
