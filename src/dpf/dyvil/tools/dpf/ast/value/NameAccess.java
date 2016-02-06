package dyvil.tools.dpf.ast.value;

import dyvil.collection.Map;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.converter.DPFValueVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;

import java.util.Objects;

public class NameAccess extends DPFValueVisitor implements Value, Expandable
{
	protected final Name  name;
	protected       Value value;

	public NameAccess(Name name)
	{
		this.name = name;
	}
	
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.value = value;
	}
	
	public Value getValue()
	{
		return this.value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		if (this.value == null)
		{
			visitor.visitName(this.name);
			return;
		}

		this.value.accept(visitor.visitValueAccess(this.name));
	}

	@Override
	public Object expand(Map<String, Object> mappings, boolean mutate)
	{
		StringBuilder builder = new StringBuilder();

		Value value = this.value;
		while (value != null)
		{
			if (value instanceof NameAccess)
			{
				NameAccess nameAccess = (NameAccess) value;
				builder.insert(0, '.').insert(0, nameAccess.getName());
				value = nameAccess.getValue();
				continue;
			}
			break;
		}

		builder.append(this.name);

		Object result = mappings.get(builder.toString());
		return result == null ? this : result;
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
			buffer.append('.');
		}
		buffer.append(this.name);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || !(o instanceof NameAccess))
		{
			return false;
		}

		final NameAccess that = (NameAccess) o;
		return this.name == that.name && Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode()
	{
		int result = this.name.hashCode();
		result = 31 * result + this.value.hashCode();
		return result;
	}
}
