package dyvil.tools.dpf.visitor;

public interface MapVisitor
{
	public ValueVisitor visitKey();
	
	public ValueVisitor visitValue();
}
