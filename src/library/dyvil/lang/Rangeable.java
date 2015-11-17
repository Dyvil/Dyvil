package dyvil.lang;

public interface Rangeable<T extends Rangeable<T>> extends Ordered<T>
{
	T next();
	
	T previous();
	
	int distanceTo(T o);
	
	@Override
	int compareTo(T o);
}
