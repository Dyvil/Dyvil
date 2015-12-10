package dyvil.tools.dpf.converter.flatmapper;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.dpf.Parser;
import dyvil.tools.dpf.converter.DyvilValueVisitor;
import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class FlatMapConverter implements NodeVisitor
{
	private Map<String, Object>	map;
	private String				name;

	public static Map<String, Object> parse(String content)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		parse(content, map);
		return map;
	}

	public static void parse(String content, Map<String, Object> map)
	{
		Parser parser = new Parser(new MarkerList(), content);
		FlatMapConverter converter = new FlatMapConverter(map);
		parser.accept(converter);
	}

	public FlatMapConverter(Map<String, Object> map)
	{
		this.map = map;
		this.name = "";
	}
	
	private FlatMapConverter(Map<String, Object> map, String name)
	{
		this.map = map;
		this.name = name;
	}
	
	private String getName(Name name)
	{
		if (this.name.isEmpty())
		{
			return name.qualified;
		}
		return this.name + '.' + name.qualified;
	}
	
	@Override
	public NodeVisitor visitNode(Name name)
	{
		return new FlatMapConverter(this.map, this.getName(name));
	}
	
	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		return new FlatMapConverter(this.map, this.getName(name));
	}
	
	@Override
	public ValueVisitor visitProperty(Name name)
	{
		return new DyvilValueVisitor()
		{
			String propertyName = FlatMapConverter.this.getName(name);
			
			@Override
			protected void visitObject(Object o)
			{
				FlatMapConverter.this.map.put(this.propertyName, o);
			}
		};
	}
}
