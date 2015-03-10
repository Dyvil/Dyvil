package dyvil.tools.compiler.ast.pattern;

public interface IPatternList
{
	public int patternCount();
	
	public void setPattern(int index, IPattern pattern);
	
	public void addPattern(IPattern pattern);
	
	public IPattern getPattern(int index);
}
