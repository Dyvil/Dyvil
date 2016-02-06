package dyvil.tools.dpf.ast.value;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.converter.DPFValueVisitor;
import dyvil.tools.dpf.visitor.StringInterpolationVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;

public class StringInterpolation extends DPFValueVisitor implements Value, StringInterpolationVisitor, Expandable
{
	protected List<String> strings = new ArrayList<>();
	protected List<Value>  values  = new ArrayList<>();
	
	@Override
	public void visitStringPart(String string)
	{
		this.strings.add(string);
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.values.add(value);
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		return this;
	}
	
	@Override
	public void visitEnd()
	{
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		StringInterpolationVisitor stringVisitor = visitor.visitStringInterpolation();
		final int len = this.values.size();

		stringVisitor.visitStringPart(this.strings.get(0));
		
		for (int i = 0; i < len; i++)
		{
			this.values.get(i).accept(stringVisitor.visitValue());
			stringVisitor.visitStringPart(this.strings.get(i + 1));
		}
		
		stringVisitor.visitEnd();
	}
	
	@Override
	public Object expand(Map<String, Object> mappings, boolean mutate)
	{
		// StringInterpolation stringInterpolation = mutate ? this : new StringInterpolation();

		StringBuilder builder = new StringBuilder();

		int index = 0;
		for (Value value : this.values)
		{
			Object o = Expandable.expand(value, mappings, mutate);

			builder.append(this.strings.get(index++));
			Constant constant;

			if (o instanceof String)
			{
				builder.append(o.toString());
			}
			else if (o instanceof Constant && (constant = (Constant) o).isConstant())
			{
				constant.appendString(builder);
			}
			else if (o instanceof Value)
			{
				return this;
			}
			else
			{
				builder.append(o);
			}
		}

		// Add the last String
		builder.append(this.strings.get(index));

		return builder.toString();
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.values.size();
		buffer.append('"');
		LexerUtil.appendStringLiteralBody(this.strings.get(0), buffer);

		for (int i = 0; i < len; i++)
		{
			buffer.append("\\(");
			this.values.get(i).toString(prefix, buffer);
			buffer.append(')');
			LexerUtil.appendStringLiteralBody(this.strings.get(i + 1), buffer);
		}
		buffer.append('"');
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || !(o instanceof StringInterpolation))
		{
			return false;
		}

		final StringInterpolation that = (StringInterpolation) o;
		return this.strings.equals(that.strings) && this.values.equals(that.values);
	}

	@Override
	public int hashCode()
	{
		int result = this.strings.hashCode();
		result = 31 * result + (this.values != null ? this.values.hashCode() : 0);
		return result;
	}
}
