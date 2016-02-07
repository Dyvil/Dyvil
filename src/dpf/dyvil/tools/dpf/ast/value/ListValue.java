package dyvil.tools.dpf.ast.value;

import dyvil.collection.Collection;
import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.converter.DPFValueVisitor;
import dyvil.tools.dpf.visitor.ListVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.ast.IASTNode;

import java.util.function.Function;

public class ListValue extends DPFValueVisitor implements Value, ListVisitor, Expandable
{
	protected List<Value> elements;
	
	public ListValue()
	{
		this.elements = new ArrayList<>();
	}

	public ListValue(Iterable<?> iterable)
	{
		this.elements = new ArrayList<>();
		for (Object element : iterable)
		{
			this.elements.add(Value.wrap(element));
		}
	}

	public ListValue(Collection<?> collection)
	{
		this.elements = new ArrayList<>(collection.size());
		for (Object element : collection)
		{
			this.elements.add(Value.wrap(element));
		}
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.elements.add(value);
	}
	
	@Override
	public ValueVisitor visitElement()
	{
		return this;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		ListVisitor v = visitor.visitList();
		for (Value element : this.elements)
		{
			element.accept(v.visitElement());
		}
		
		v.visitEnd();
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.elements.size();
		if (len <= 0)
		{
			buffer.append("[]");
			return;
		}
		
		buffer.append("[ ");
		this.elements.get(0).toString(prefix, buffer);
		for (int i = 1; i < len; i++)
		{
			buffer.append(", ");
			this.elements.get(i).toString(prefix, buffer);
		}
		buffer.append(" ]");
	}

	@Override
	public ListValue expand(Map<String, Object> mappings, boolean mutate)
	{
		final Function<Value, Value> valueFunction = value -> Value.wrap(Expandable.expand(value, mappings, mutate));
		if (mutate)
		{
			this.elements.map(valueFunction);
			return this;
		}
		else
		{
			ListValue copy = new ListValue();
			copy.elements = this.elements.mapped(valueFunction);
			return copy;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || !(o instanceof ListValue))
		{
			return false;
		}

		final ListValue that = (ListValue) o;
		return this.elements.equals(that.elements);
	}

	@Override
	public int hashCode()
	{
		return this.elements.hashCode();
	}
}
