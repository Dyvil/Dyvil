package dyvil.lang;

@FunctionalInterface
public interface Pattern<T, R>
{
	public Option<R> match(T t);
}
