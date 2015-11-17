package dyvil.tools.dpf.visitor;

public interface MapVisitor
{
	ValueVisitor visitKey();
	
	ValueVisitor visitValue();
	
	default void visitEnd()
	{
	}
}
