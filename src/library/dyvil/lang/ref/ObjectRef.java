package dyvil.lang.ref;

public class ObjectRef
{
	protected Object	value;
	
	protected ObjectRef(Object value)
	{
		this.value = value;
	}
	
	public static ObjectRef get(Object value)
	{
		return new ObjectRef(value);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.value == obj || obj != null && obj.equals(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return this.value == null ? 0 : this.value.hashCode();
	}
	
	@Override
	public String toString()
	{
		return this.value == null ? "null" : this.value.toString();
	}
}
