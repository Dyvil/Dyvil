package dyvilx.tools.compiler.ast.expression.operator;

import dyvil.lang.Name;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class Operator implements IOperator
{
	public static final Operator DEFAULT       = new Operator(null, LEFT, 100000);
	public static final Operator DEFAULT_RIGHT = new Operator(null, RIGHT, 100000);

	protected static final int ID_PREFIX      = 0;
	protected static final int ID_INFIX_LEFT  = 1;
	protected static final int ID_INFIX_NONE  = 2;
	protected static final int ID_INFIX_RIGHT = 3;
	protected static final int ID_POSTFIX     = 4;
	protected static final int ID_TERNARY     = 5;
	protected static final int ID_CIRCUMFIX   = 6;

	protected Name name;
	protected Name name2;
	protected int  precedence;
	protected byte id;

	public Operator()
	{
	}

	public Operator(Name name)
	{
		this.name = name;
	}

	public Operator(Name name, byte type)
	{
		this.name = name;
		this.setType(type);
	}

	public Operator(Name name, byte associativity, int precedence)
	{
		this.name = name;
		this.precedence = precedence;

		switch (associativity)
		{
		case LEFT:
			this.id = ID_INFIX_LEFT;
			return;
		case RIGHT:
			this.id = ID_INFIX_RIGHT;
			return;
		default:
			this.id = ID_INFIX_NONE;
		}
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
	public Name getName2()
	{
		return this.name2;
	}

	@Override
	public void setName2(Name name)
	{
		this.name2 = name;
	}

	@Override
	public byte getType()
	{
		switch (this.id)
		{
		case ID_PREFIX:
			return PREFIX;
		case ID_POSTFIX:
			return POSTFIX;
		case ID_TERNARY:
			return TERNARY;
		case ID_CIRCUMFIX:
			return CIRCUMFIX;
		}
		return INFIX;
	}

	@Override
	public void setType(byte type)
	{
		switch (type)
		{
		case PREFIX:
			this.id = ID_PREFIX;
			return;
		case POSTFIX:
			this.id = ID_POSTFIX;
			return;
		case INFIX:
			this.id = ID_INFIX_NONE;
			return;
		case TERNARY:
			this.id = ID_TERNARY;
			return;
		case CIRCUMFIX:
			this.id = ID_CIRCUMFIX;
			return;
		}
	}

	@Override
	public boolean isType(byte type)
	{
		final byte thisType = this.getType();
		return thisType == type //
		       || thisType == TERNARY && type == INFIX //
		       || thisType == CIRCUMFIX && (type == PREFIX || type == POSTFIX);
	}

	@Override
	public byte getAssociativity()
	{
		switch (this.id)
		{
		case ID_INFIX_LEFT:
			return LEFT;
		case ID_INFIX_RIGHT:
			return RIGHT;
		}
		return NONE;
	}

	@Override
	public void setAssociativity(byte associativity)
	{
		switch (associativity)
		{
		case LEFT:
			this.id = ID_INFIX_LEFT;
			return;
		case RIGHT:
			this.id = ID_INFIX_RIGHT;
			return;
		case NONE:
			this.id = ID_INFIX_NONE;
		}
	}

	@Override
	public int getPrecedence()
	{
		return this.precedence;
	}

	@Override
	public void setPrecedence(int precedence)
	{
		this.precedence = precedence;
	}

	@Override
	public int comparePrecedence(IOperator other)
	{
		return Integer.compare(this.precedence, other.getPrecedence());
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		Name.write(this.name, out);
		out.writeByte(this.id);
		if (this.id != ID_PREFIX && this.id != ID_POSTFIX && this.id != ID_CIRCUMFIX)
		{
			out.writeInt(this.precedence);
		}
		if (this.id == ID_TERNARY || this.id == ID_CIRCUMFIX)
		{
			Name.write(this.name2, out);
		}
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.name = Name.read(in);

		this.id = in.readByte();
		if (this.id != ID_PREFIX && this.id != ID_POSTFIX && this.id != ID_CIRCUMFIX)
		{
			this.precedence = in.readInt();
		}
		if (this.id == ID_TERNARY || this.id == ID_CIRCUMFIX)
		{
			this.name2 = Name.read(in);
		}
	}

	public static Operator read(DataInput in) throws IOException
	{
		final Operator operator = new Operator();
		operator.readData(in);
		return operator;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString(buf);
		return buf.toString();
	}

	@Override
	public void toString(StringBuilder builder)
	{
		switch (this.id)
		{
		case ID_PREFIX:
			builder.append("prefix operator ").append(this.name);
			return;
		case ID_POSTFIX:
			builder.append("postfix operator ").append(this.name);
			return;
		case ID_INFIX_NONE:
			builder.append("infix operator ").append(this.name).append(" { precedence ").append(this.precedence)
			       .append(" }");
			return;
		case ID_INFIX_LEFT:
			builder.append("infix operator ").append(this.name).append(" { associativity left, precedence ")
			       .append(this.precedence).append(" }");
			return;
		case ID_INFIX_RIGHT:
			builder.append("infix operator ").append(this.name).append(" { associativity right, precedence ")
			       .append(this.precedence).append(" }");
			return;
		case ID_TERNARY:
			builder.append("infix operator ").append(this.name).append(' ').append(this.name2).append(" { precedence ")
			       .append(this.precedence).append(" }");
			return;
		case ID_CIRCUMFIX:
			builder.append("prefix postfix operator ").append(this.name).append(' ').append(this.name2);
		}
	}
}
