package dyvilx.lang.model.type;

public class UnionType<T> extends NamedType<T>
{
	private final Type left;
	private final Type right;

	public static <T> UnionType<T> apply(Class<T> theClass, Type left, Type right)
	{
		return new UnionType<>(theClass, left, right);
	}

	public UnionType(Class<T> theClass, Type left, Type right)
	{
		super(theClass);

		this.left = left;
		this.right = right;
	}

	public Type left()
	{
		return this.left;
	}

	public Type right()
	{
		return this.right;
	}

	@Override
	public String toString()
	{
		return this.left + " | " + this.right;
	}

	@Override
	public void toString(StringBuilder builder)
	{
		this.left.toString(builder);
		builder.append(" | ");
		this.right.toString(builder);
	}
}
