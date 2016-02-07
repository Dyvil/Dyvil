package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;

public class StringValue implements Constant
{
	protected String value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitString(this.value);
	}

	@Override
	public Object toObject()
	{
		return this.value;
	}

	@Override
	public void appendString(StringBuilder builder)
	{
		builder.append(this.value);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendStringLiteral(this.value, buffer);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || !(o instanceof StringValue))
		{
			return false;
		}

		StringValue that = (StringValue) o;

		return this.value.equals(that.value);
	}

	@Override
	public int hashCode()
	{
		return this.value.hashCode();
	}
}
