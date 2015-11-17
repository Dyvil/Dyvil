package dyvil.tools.dpf.visitor;

public interface StringInterpolationVisitor
{
	void visitStringPart(String string);
	
	ValueVisitor visitValue();
	
	void visitEnd();
}
