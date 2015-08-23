package dyvil.tools.compiler.ast.operator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.member.Name;

public final class Operator
{
	public static final int	PREFIX		= 0;
	public static final int	INFIX_LEFT	= 1;
	public static final int	INFIX_NONE	= 2;
	public static final int	INFIX_RIGHT	= 3;
	public static final int	POSTFIX		= 4;
	
	public final Name	name;
	public int			precedence;
	public int			type;
	
	public Operator(Name name)
	{
		this.name = name;
	}
	
	Operator(Name name, int type)
	{
		this.name = name;
		this.type = type;
	}
	
	Operator(Name name, int precedence, int type)
	{
		this.name = name;
		this.type = type;
		this.precedence = precedence;
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
	
	public void write(DataOutput dos) throws IOException
	{
		dos.writeUTF(this.name.unqualified);
		dos.writeByte(this.type);
		if (this.type > PREFIX && this.type < POSTFIX)
		{
			dos.writeInt(this.precedence);
		}
	}
	
	public static Operator read(DataInput dis) throws IOException
	{
		Name name = Name.get(dis.readUTF());
		byte type = dis.readByte();
		if (type > PREFIX && type < POSTFIX)
		{
			return new Operator(name, dis.readInt(), type);
		}
		return new Operator(name, type);
	}
	
	public void toString(StringBuilder buffer)
	{
		switch (this.type)
		{
		case PREFIX:
			buffer.append("prefix operator ").append(this.name);
			return;
		case POSTFIX:
			buffer.append("postfix operator ").append(this.name);
			return;
		case INFIX_LEFT:
			buffer.append("infix operator ").append(this.name).append(" { left, ").append(this.precedence).append(" }");
			return;
		case INFIX_NONE:
			buffer.append("infix operator ").append(this.name).append(" { none, ").append(this.precedence).append(" }");
			return;
		case INFIX_RIGHT:
			buffer.append("infix operator ").append(this.name).append(" { right, ").append(this.precedence).append(" }");
			return;
		}
	}
}
