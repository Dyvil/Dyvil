package dyvil.tools.dpf.ast.builder;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.ast.Node;
import dyvil.tools.dpf.ast.value.Value;
import dyvil.tools.dpf.visitor.BuilderVisitor;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class Builder implements Value, BuilderVisitor, Expandable
{
	private Name name;
	private List<Parameter> parameters = new ArrayList<>();
	private Node node;
	
	public Builder(Name name)
	{
		this.name = name;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public ValueVisitor visitParameter(Name name)
	{
		Parameter parameter = new Parameter(name);
		this.parameters.add(parameter);
		return parameter;
	}
	
	@Override
	public NodeVisitor visitNode()
	{
		Node node = new Node(this.name);
		this.node = node;
		return node;
	}
	
	@Override
	public void visitEnd()
	{
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		BuilderVisitor v = visitor.visitBuilder(this.name);
		for (Parameter p : this.parameters)
		{
			p.getValue().accept(v.visitParameter(p.getName()));
		}
		if (this.node != null)
		{
			this.node.accept(v.visitNode());
		}
	}

	@Override
	public Builder expand(Map<String, Object> mappings, boolean mutate)
	{
		if (mutate)
		{
			for (Parameter parameter : this.parameters)
			{
				parameter.expand(mappings, true);
			}
			this.node = this.node.expand(mappings, true);
			return this;
		}
		else
		{
			Builder builder = new Builder(this.name);
			builder.parameters = this.parameters.mapped(parameter -> parameter.expand(mappings, false));
			builder.node = this.node.expand(mappings, false);
			return builder;
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		
		int parameterCount = this.parameters.size();
		if (parameterCount > 0)
		{
			buffer.append('(');
			
			this.parameters.get(0).toString(prefix, buffer);
			for (int i = 1; i < parameterCount; i++)
			{
				buffer.append(", ");
				this.parameters.get(i).toString(prefix, buffer);
			}
			
			buffer.append(')');
		}
		else if (this.node == null)
		{
			buffer.append("()");
			return;
		}
		
		if (this.node != null)
		{
			buffer.append(" {\n");
			this.node.bodyToString(prefix + '\t', buffer);
			buffer.append(prefix).append('}');
		}
	}
}
