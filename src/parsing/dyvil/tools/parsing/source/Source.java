package dyvil.tools.parsing.source;

public interface Source
{
	int lineCount();

	String getText();

	String getLine(int index);
}
