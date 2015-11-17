package dyvil.tools.dpf.visitor;

public interface ListVisitor
{
	ValueVisitor visitElement();
	
	default void visitEnd()
	{
	}
}
