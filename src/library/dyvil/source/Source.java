package dyvil.source;

public interface Source
{
	int lineCount();

	String text();

	String line(int index);
}
