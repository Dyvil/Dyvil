package dyvilx.tools.compiler.ast.pattern;

public interface PatternList
{
	int patternCount();

	Pattern get(int index);

	void set(int index, Pattern pattern);

	void add(Pattern pattern);
}
