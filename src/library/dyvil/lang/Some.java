package dyvil.lang;

public final class Some<T> implements Option<T>
{
	private final T value;
	
	public Some(T value)
	{
		this.value = value;
	}

	@Override
	public T get()
	{
		return this.value;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Some(" + this.value + ")";
	}

	@Override
	public int hashCode()
	{
		return 31 + (this.value == null ? 0 : this.value.hashCode());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Some))
		{
			return false;
		}
		Some other = (Some) obj;
		if (this.value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!this.value.equals(other.value))
		{
			return false;
		}
		return true;
	}
}
