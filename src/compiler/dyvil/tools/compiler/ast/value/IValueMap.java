package dyvil.tools.compiler.ast.value;

public interface IValueMap
{
	public void addValue(String key, IValue value);
	
	public IValue getValue(String key);
	
	public static class KeyValuePair
	{
		public String	key;
		public IValue	value;
		
		public KeyValuePair(String key, IValue value)
		{
			this.key = key;
			this.value = value;
		}
	}
}
