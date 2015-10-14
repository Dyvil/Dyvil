package dyvil.tools.dpf.visitor;

public interface ListVisitor
{
	public ValueVisitor visitElement();
	
	public default void visitEnd()
	{
	}
}
