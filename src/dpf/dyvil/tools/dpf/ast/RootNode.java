package dyvil.tools.dpf.ast;

import dyvil.collection.Map;
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
		this.acceptBody(visitor);
	}

	@Override
	public RootNode expand(Map<String, Object> mappings, boolean mutate)
	{
		if (mutate)
		{
			this.expandChildren(mappings);
			return this;
		}
		else
		{
			RootNode node = new RootNode();
			this.expand(node, mappings);
			return node;
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.bodyToString(prefix, buffer);
	}
}
