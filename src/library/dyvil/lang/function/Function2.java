package dyvil.lang.function;

@FunctionalInterface
public interface Function2<P1, P2, R>
{
	public R apply(P1 par1, P2 par2);
}
