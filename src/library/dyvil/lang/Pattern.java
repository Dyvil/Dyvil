package dyvil.lang;

@FunctionalInterface
public interface Pattern
{
	public boolean match(Object t);
}
