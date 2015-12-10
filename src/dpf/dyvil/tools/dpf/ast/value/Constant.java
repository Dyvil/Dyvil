package dyvil.tools.dpf.ast.value;

public interface Constant extends Value
{
	default boolean isConstant()
	{
		return true;
	}

	Object toObject();

	void appendString(StringBuilder builder);
}
