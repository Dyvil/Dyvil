package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.BuilderVisitor;
import dyvil.tools.dpf.visitor.ListVisitor;
import dyvil.tools.dpf.visitor.MapVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;

public abstract class ValueCreator implements ValueVisitor
{
	public abstract void setValue(Value value);
	
	@Override
	public void visitInt(int value)
	{
		this.setValue(new IntValue(value));
	}
	
	@Override
	public void visitLong(long value)
	{
		this.setValue(new LongValue(value));
	}
	
	@Override
	public void visitFloat(float value)
	{
		this.setValue(new FloatValue(value));
	}
	
	@Override
	public void visitDouble(double value)
	{
		this.setValue(new DoubleValue(value));
	}
	
	@Override
	public void visitString(String value)
	{
		this.setValue(new StringValue(value));
	}
	
	@Override
	public void visitName(Name name)
	{
		this.setValue(new NameValue(name));
	}
	
	@Override
	public ListVisitor visitList()
	{
		ListValue list = new ListValue();
		this.setValue(list);
		return list;
	}
	
	@Override
	public MapVisitor visitMap()
	{
		MapValue map = new MapValue();
		this.setValue(map);
		return map;
	}
	
	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		return null;
	}
}
