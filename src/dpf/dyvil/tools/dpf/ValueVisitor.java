package dyvil.tools.dpf;

import dyvil.tools.parsing.Name;

public interface ValueVisitor
{
	public void visitInt(int value);
	
	public void visitLong(long value);
	
	public void visitFloat(float value);
	
	public void visitDouble(double value);
	
	public void visitString(String value);
	
	public void visitName(Name name);
	
	public ListVisitor visitList();
	
	public MapVisitor visitMap();
	
	public BuilderVisitor visitBuilder(Name name);
}
