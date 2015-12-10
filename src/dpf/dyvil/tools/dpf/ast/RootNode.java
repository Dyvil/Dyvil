package dyvil.tools.dpf.ast;

import dyvil.collection.Map;
import dyvil.tools.dpf.Parser;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class RootNode extends Node
{
	public RootNode()
	{
		super(Name.getQualified("root"));
	}

	public static RootNode parse(String content)
	{
		RootNode rootNode = new RootNode();
		new Parser(new MarkerList(), content).accept(rootNode);
		return rootNode;
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
