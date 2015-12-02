package dyvil.tools.dpf.ast.value;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.MapVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;

public class MapValue extends ValueCreator implements Value, MapVisitor
{
	private List<Value> keys   = new ArrayList<Value>();
	private List<Value> values = new ArrayList<Value>();
	private boolean valueMode;
	
	public MapValue()
	{
	}
	
	@Override
	protected void setValue(Value value)
	{
		if (this.valueMode)
		{
			this.values.add(value);
		}
		else
		{
			this.keys.add(value);
		}
	}
	
	@Override
	public ValueVisitor visitKey()
	{
		this.valueMode = false;
		return this;
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		this.valueMode = true;
		return this;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		MapVisitor v = visitor.visitMap();
		
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			this.keys.get(i).accept(v.visitKey());
			this.values.get(i).accept(v.visitValue());
		}
		
		v.visitEnd();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.values.size();
		if (len <= 0)
		{
			buffer.append("{}");
			return;
		}
		
		String prefix1 = prefix + "\t";
		
		buffer.append("{\n").append(prefix1);
		this.keys.get(0).toString(prefix1, buffer);
		buffer.append(" : ");
		this.values.get(0).toString(prefix1, buffer);
		for (int i = 1; i < len; i++)
		{
			buffer.append(",\n").append(prefix1);
			this.keys.get(i).toString(prefix1, buffer);
			buffer.append(" : ");
			this.values.get(i).toString(prefix1, buffer);
		}
		buffer.append('\n').append(prefix).append('}');
	}
}
