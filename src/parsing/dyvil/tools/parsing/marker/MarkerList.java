package dyvil.tools.parsing.marker;

import dyvil.collection.iterator.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;

public final class MarkerList implements Iterable<Marker>
{
	private Marker[] markers;
	private int      markerCount;
	
	private int warnings;
	private int errors;
	
	public MarkerList()
	{
		this.markers = new Marker[1];
	}
	
	public void clear()
	{
		for (int i = 0; i < this.markerCount; i++)
		{
			this.markers[i] = null;
		}
		this.markerCount = this.warnings = this.errors = 0;
	}
	
	public int size()
	{
		return this.markerCount;
	}
	
	public int getErrors()
	{
		return this.errors;
	}
	
	public int getWarnings()
	{
		return this.warnings;
	}
	
	public boolean isEmpty()
	{
		return this.markerCount == 0;
	}
	
	public void sort()
	{
		Arrays.sort(this.markers, 0, this.markerCount);
	}
	
	public void add(Marker marker)
	{
		if (marker.isError())
		{
			this.errors++;
		}
		if (marker.isWarning())
		{
			this.warnings++;
		}
		
		int index = this.markerCount++;
		if (index >= this.markers.length)
		{
			Marker[] temp = new Marker[this.markerCount];
			System.arraycopy(this.markers, 0, temp, 0, this.markers.length);
			this.markers = temp;
		}
		this.markers[index] = marker;
	}
	
	@Override
	public Iterator<Marker> iterator()
	{
		return new ArrayIterator<Marker>(this.markers, this.markerCount);
	}
}
