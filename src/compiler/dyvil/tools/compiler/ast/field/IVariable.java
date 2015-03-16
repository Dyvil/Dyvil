package dyvil.tools.compiler.ast.field;

public interface IVariable extends IField
{
	@Override
	public default boolean isVariable()
	{
		return true;
	}
	
	public void setIndex(int index);
	
	public int getIndex();
}
