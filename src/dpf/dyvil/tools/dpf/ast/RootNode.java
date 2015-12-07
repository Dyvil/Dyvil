package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.parsing.Name;

public class RootNode extends Node
{
	public RootNode()
	{
		super(Name.getQualified("root"));
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		for (Property property : this.properties)
		{
			property.accept(visitor);
		}
		for (Node node : this.nodes)
		{
			node.accept(visitor);
		}
		for (NodeAccess nodeAccess : this.nodeAccesses)
		{
			nodeAccess.accept(visitor);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.bodyToString(prefix, buffer);
	}
}
