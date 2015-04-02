package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.member.Name;

public final class Operator
{
	public static final int	PREFIX		= 0;
	public static final int	INFIX_LEFT	= 1;
	public static final int	INFIX_NONE	= 2;
	public static final int	INFIX_RIGHT	= 3;
	public static final int	POSTFIX		= 4;
	
	public final Name		name;
	public int				precedence;
	public int				type;
	
	public Operator(Name name)
	{
		this.name = name;
	}
	
	Operator(Name name, int type)
	{
		this.name = name;
		this.type = type;
		Operators.map.put(name, this);
	}
	
	Operator(Name name, int precedence, int type)
	{
		this.name = name;
		this.type = type;
		this.precedence = precedence;
		Operators.map.put(name, this);
	}
	
	public void setPrecedence(int precedence)
	{
		this.precedence = precedence;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString(buf);
		return buf.toString();
	}
	
	public void toString(StringBuilder buffer)
	{
		buffer.append("operator ").append(this.name).append(" { ");
		switch (this.type)
		{
		case PREFIX:
			buffer.append("prefix");
			break;
		case INFIX_LEFT:
			buffer.append("left, ").append(this.precedence);
			break;
		case INFIX_NONE:
			buffer.append("none, ").append(this.precedence);
			break;
		case INFIX_RIGHT:
			buffer.append("right, ").append(this.precedence);
			break;
		case POSTFIX:
			buffer.append("postfix");
			break;
		}
		buffer.append(" }");
	}
}
