package dyvil.tools.compiler.ast.expression;

import dyvil.tools.parsing.Name;

public interface IValueMap
{
	public void addValue(Name key, IValue value);
	
	public IValue getValue(Name key);
	
	public static class KeyValuePair
	{
		public Name		key;
		public IValue	value;
		
		public KeyValuePair(Name key, IValue value)
		{
			this.key = key;
			this.value = value;
		}
	}
}
