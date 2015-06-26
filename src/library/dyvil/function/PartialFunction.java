package dyvil.function;

public interface PartialFunction<P1, R> extends Function1<P1, R>
{
	@Override
	public R apply(P1 par1);
	
	public boolean isDefined(P1 par1);
}
