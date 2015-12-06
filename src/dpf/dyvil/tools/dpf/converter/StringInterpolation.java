package dyvil.tools.dpf.converter;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.StringInterpolationVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;

public class StringInterpolation implements StringInterpolationVisitor, Expandable
{
	private List<String>	strings	= new ArrayList<>();
	private List<Object>	values	= new ArrayList<>();
	
	@Override
	public void visitStringPart(String string)
	{
		this.strings.add(string);
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		return new DyvilValueVisitor()
		{
			@Override
			protected void visitObject(Object o)
			{
				values.add(o);
			}
		};
	}
	
	@Override
	public void visitEnd()
	{
	}
	
	@Override
	public Object expand(Map<String, Object> mappings, boolean mutate)
	{
		StringBuilder builder = new StringBuilder(this.strings.get(0));
		
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			Object o = Expandable.expand(this.values.get(i), mappings, mutate);
			builder.append(o);
			builder.append(this.strings.get(i + 1));
		}
		
		return builder.toString();
	}
}
