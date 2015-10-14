package dyvil.tools.dpf.ast;

public class DPFFile extends Node
{
	public DPFFile()
	{
		super(null);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (NodeElement p : this.nodeElements)
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
