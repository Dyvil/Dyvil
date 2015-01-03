package dyvil.tools.compiler.parser.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class TypeVariable extends Type
{
	protected List<IType>	upperBounds;
	protected List<IType>	lowerBounds;
	
	public TypeVariable(ICodePosition position)
	{
		super();
		this.position = position;
	}
	
	public TypeVariable(ICodePosition position, String name)
	{
		super(position, name);
	}
	
	public void addUpperBound(IType bound)
	{
		if (this.upperBounds == null)
		{
			this.upperBounds = new ArrayList(1);
		}
		this.upperBounds.add(bound);
	}
	
	public void addLowerBound(IType bound)
	{
		if (this.lowerBounds == null)
		{
			this.lowerBounds = new ArrayList(1);
		}
		this.lowerBounds.add(bound);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name == null)
		{
			buffer.append('_');
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.upperBounds != null)
		{
			for (IType t : this.upperBounds)
			{
				buffer.append(Formatting.Type.genericUpperBound);
				t.toString("", buffer);
			}
		}
		if (this.lowerBounds != null)
		{
			for (IType t : this.lowerBounds)
			{
				buffer.append(Formatting.Type.genericLowerBound);
				t.toString("", buffer);
			}
		}
	}
}
