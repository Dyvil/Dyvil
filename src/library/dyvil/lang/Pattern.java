package dyvil.lang;

@FunctionalInterface
public interface Pattern
{
	public Option match(Object t);
}
