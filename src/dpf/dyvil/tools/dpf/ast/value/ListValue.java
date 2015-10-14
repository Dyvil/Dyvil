package dyvil.tools.dpf.ast.value;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.ListVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;

public class ListValue extends ValueCreator implements Value, ListVisitor
{
	private List<Value> elements = new ArrayList<Value>();
	
	public ListValue()
	{
	}
	
	@Override
	public void setValue(Value value)
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
}
