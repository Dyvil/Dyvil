package dyvil.tools.compiler.ast.pattern;

public interface IPatternList
{
	int patternCount();
	
	void setPattern(int index, IPattern pattern);
	
	void addPattern(IPattern pattern);
	
	IPattern getPattern(int index);
}
