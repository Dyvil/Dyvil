package dyvil.lang;

public abstract class String
{
	protected char[] value;
	
	protected String(char[] value)
	{
		this.value = value;
	}
	
	public abstract String $set(char[] value);
	
	public String add$(Object o)
	{
		char[] value = o.toString().toCharArray();
		return this.add$(value);
	}
	
	public String add$(String s)
	{
		return this.add$(s.value);
	}
	
	public String add$(char[] c)
	{
		int len1 = this.value.length;
		int len2 = c.length;
		char[] newArray = new char[len1 + len2];
		System.arraycopy(this.value, 0, newArray, 0, len1);
		System.arraycopy(c, 0, newArray, len1, len2);
		return this.$set(newArray);
	}
}
