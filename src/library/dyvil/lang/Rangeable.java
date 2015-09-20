package dyvil.lang;

public interface Rangeable<T extends Rangeable<T>> extends Ordered<T>
{
	public T next();
	
	public T previous();
	
	public int distanceTo(T o);
	
	@Override
	public int compareTo(T o);
}
