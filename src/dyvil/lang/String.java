package dyvil.lang;

public abstract class String
{
	protected char[] value;
	
	protected String(char[] value)
	{
		this.value = value;
	}
	
	public String add$(Object o)
	{
		char[] value = o.toString().toCharArray();
		return this.add$(value);
	}
	
	public String add$(String s)
	{
		return this.add$(s.value);
	}
	
	public abstract String add$(char[] c);
}
