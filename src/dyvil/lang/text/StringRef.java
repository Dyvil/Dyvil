package dyvil.lang.text;

import dyvil.lang.String;

public class StringRef extends dyvil.lang.String
{
	protected StringRef(char[] value)
	{
		super(value);
	}
	
	@Override
	public String add$(char[] c)
	{
		int len1 = this.value.length;
		int len2 = c.length;
		char[] dest = new char[len1 + len2];
		System.arraycopy(this.value, 0, dest, 0, len1);
		System.arraycopy(c, 0, dest, len1, len2);
		this.value = dest;
		
		return this;
	}
}
