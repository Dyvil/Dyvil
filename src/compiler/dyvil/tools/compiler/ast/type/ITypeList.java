package dyvil.tools.compiler.ast.type;

public interface ITypeList
{
	public int typeCount();
	
	public void setType(int index, IType type);
	
	public void addType(IType type);
	
	public IType getType(int index);
}
