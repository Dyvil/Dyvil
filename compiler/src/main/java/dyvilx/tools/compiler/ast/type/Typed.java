package dyvilx.tools.compiler.ast.type;

public interface Typed
{
	IType getType();

	void setType(IType type);

	@Deprecated
	default boolean isType(IType type)
	{
		return this.getType() == type;
	}
}
