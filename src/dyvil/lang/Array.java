package dyvil.lang;

public abstract class Array<T>
{
	protected Class<T>	type;
	protected Object[]	data;
	
	protected Array(int size)
	{
		this.data = new Object[size];
	}
	
	protected Array(Object[] data)
	{
		this.data = data;
	}
	
	protected Array(Class<T> type, int size)
	{
		this.type = type;
		this.data = new Object[size];
	}
	
	public abstract Array<T> $set(Object[] data);
	
	public T get(int i)
	{
		return (T) this.data[i];
	}
	
	public void set(int i, T v)
	{
		this.data[i] = v;
	}
	
	public T setAndGet(int i, T v)
	{
		T o = (T) this.data[i];
		this.data[i] = v;
		return o;
	}
	
	public Array<T> $add(Object[] v)
	{
		int len1 = this.data.length;
		int len2 = v.length;
		Object[] newArray = new Object[len1 + len2];
		
		System.arraycopy(this.data, 0, newArray, 0, len1);
		System.arraycopy(v, 0, newArray, len1, len2);
		
		return this.$set(newArray);
	}
	
	public Array<T> $mul(int v)
	{
		int len1 = this.data.length;
		Object[] newArray = new Object[len1 * v];
		
		for (int i = 0; i < v; i++)
		{
			System.arraycopy(this.data, 0, newArray, len1 * i, len1);
		}
		
		return this.$set(newArray);
	}
}
