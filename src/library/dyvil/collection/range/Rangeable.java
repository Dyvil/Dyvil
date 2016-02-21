package dyvil.collection.range;

public interface Rangeable<T extends Rangeable<T>> extends Comparable<T>
{
	T next();
	
	T previous();
	
	int distanceTo(T o);
	
	@Override
	int compareTo(T o);
}
