package dyvil.tools.dpf.visitor;

public interface StringInterpolationVisitor
{
	public void visitStringPart(String string);
	
	public ValueVisitor visitValue();
	
	public void visitEnd();
}
