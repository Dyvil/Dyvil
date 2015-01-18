package dyvil.tools.compiler.ast.type;

public interface ITyped
{
	public void setType(IType type);
	
	public IType getType();
	
	public default boolean hasType()
	{
		return this.getType() != null;
	}
	
	public default boolean isType(IType type)
	{
		return Type.isSuperType(type, this.getType());
	}
	
	public default int getTypeMatch(IType type)
	{
		IType t = this.getType();
		if (t == null)
		{
			return 0;
		}
		if (type.equals(t))
		{
			return 3;
		}
		else if (Type.isSuperType(type, t))
		{
			return 2;
		}
		else if (type.classEquals(Type.OBJECT))
		{
			return 1;
		}
		return 0;
	}
}
