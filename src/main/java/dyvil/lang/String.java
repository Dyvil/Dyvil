package dyvil.lang;

public abstract class String
{
	protected char[] value;
	
	protected String(char[] value)
	{
		this.value = value;
	}
	
	public abstract String $eq(char[] value);
	
	public String $plus(Object o)
	{
		char[] value = o.toString().toCharArray();
		return this.$plus(value);
	}
	
	public String $plus(String s)
	{
		return this.$plus(s.value);
	}
	
	public String $plus(char[] c)
	{
		int len1 = this.value.length;
		int len2 = c.length;
		char[] newArray = new char[len1 + len2];
		System.arraycopy(this.value, 0, newArray, 0, len1);
		System.arraycopy(c, 0, newArray, len1, len2);
		return this.$eq(newArray);
	}
}
