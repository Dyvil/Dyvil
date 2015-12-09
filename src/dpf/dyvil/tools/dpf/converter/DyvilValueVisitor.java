package dyvil.tools.dpf.converter;

import dyvil.lang.*;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;

/**
 * An abstract {@link ValueVisitor} that converts nodes to Dyvil and Java objects / wrappers according to the following
 * table:
 * <p>
 * <table summary="" border="1">
 * <th>Value Type</th><th>Object Type</th>
 * <tr><td>int</td><td>dyvil.lang.Int</td></tr>
 * <tr><td>long</td><td>dyvil.lang.Long</td></tr>
 * <tr><td>float</td><td>dyvil.lang.Float</td></tr>
 * <tr><td>double</td><td>dyvil.lang.String</td></tr>
 * <tr><td>String</td><td>java.lang.String</td></tr>
 * <tr><td>Identifier (Access)</td><td>dyvil.tools.dpf.converter.NameAccess</td></tr>
 * <tr><td>List</td><td>dyvil.collection.List</td></tr>
 * <tr><td>Map</td><td>dyvil.collection.Map</td></tr>
 * <tr><td>String Interpolation</td><td>dyvil.tools.dpf.converter.StringInterpolation</td></tr>
 * <tr><td>Builder</td><td>-none-</td></tr>
 * </table>
 */
public abstract class DyvilValueVisitor implements ValueVisitor
{
	protected abstract void visitObject(Object o);
	
	@Override
	public void visitInt(int v)
	{
		this.visitObject(Int.apply(v));
	}
	
	@Override
	public void visitLong(long v)
	{
		this.visitObject(Long.apply(v));
	}
	
	@Override
	public void visitFloat(float v)
	{
		this.visitObject(Float.apply(v));
	}
	
	@Override
	public void visitDouble(double v)
	{
		this.visitObject(Double.apply(v));
	}
	
	@Override
	public void visitString(String v)
	{
		this.visitObject(v);
	}
	
	@Override
	public void visitName(Name v)
	{
		switch (v.qualified)
		{
		case "true":
			this.visitObject(dyvil.lang.Boolean.apply(true));
			return;
		case "false":
			this.visitObject(dyvil.lang.Boolean.apply(false));
			return;
		case "null":
			this.visitObject(null);
			return;
		}

		this.visitObject(new NameAccess(v.qualified));
	}
	
	@Override
	public ListVisitor visitList()
	{
		List<Object> list = new ArrayList<>();
		return new ListVisitor()
		{
			@Override
			public ValueVisitor visitElement()
			{
				return new DyvilValueVisitor()
				{
					@Override
					protected void visitObject(Object o)
					{
						list.add(o);
					}
				};
			}
			
			@Override
			public void visitEnd()
			{
				DyvilValueVisitor.this.visitObject(list);
			}
		};
	}
	
	@Override
	public MapVisitor visitMap()
	{
		Map<Object, Object> map = new HashMap<>();
		return new MapVisitor()
		{
			private Object key;
			
			@Override
			public ValueVisitor visitKey()
			{
				return new DyvilValueVisitor()
				{
					@Override
					protected void visitObject(Object o)
					{
						key = o;
					}
				};
			}
			
			@Override
			public ValueVisitor visitValue()
			{
				return new DyvilValueVisitor()
				{
					@Override
					protected void visitObject(Object o)
					{
						map.put(key, o);
					}
				};
			}
			
			@Override
			public void visitEnd()
			{
				DyvilValueVisitor.this.visitObject(map);
			}
		};
	}
	
	@Override
	public StringInterpolationVisitor visitStringInterpolation()
	{
		StringInterpolation interpolation = new StringInterpolation();
		this.visitObject(interpolation);
		return interpolation;
	}
	
	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		return null;
	}
	
	@Override
	public ValueVisitor visitValueAccess(Name v)
	{
		NameAccess access = new NameAccess(v.qualified);
		this.visitObject(access);
		return access;
	}
}
