package dyvil.tools.dpf;

public interface MapVisitor
{
	public ValueVisitor visitKey();
	
	public ValueVisitor visitValue();
}
