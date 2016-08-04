package dyvil.tools.parsing.marker;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.util.I18n;

import java.util.Arrays;
import java.util.Iterator;

public final class MarkerList implements Iterable<Marker>
{
	private Marker[] markers;
	private int      markerCount;

	private int warnings;
	private int errors;

	private I18n i18n;

	public MarkerList(I18n i18n)
	{
		this.i18n = i18n;
		this.markers = new Marker[1];
	}

	public I18n getI18n()
	{
		return this.i18n;
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
		if (marker == null)
		{
			return;
		}

		if (marker.isError())
		{
			this.errors++;
		}
		if (marker.isWarning())
		{
			this.warnings++;
		}

		final int index = this.markerCount++;
		if (index >= this.markers.length)
		{
			Marker[] temp = new Marker[this.markerCount << 1];
			System.arraycopy(this.markers, 0, temp, 0, index);
			this.markers = temp;
		}
		this.markers[index] = marker;
	}

	public void addAll(MarkerList markers)
	{
		final int newLength = this.markerCount + markers.markerCount;
		if (newLength >= this.markerCount)
		{
			Marker[] temp = new Marker[newLength];
			System.arraycopy(this.markers, 0, temp, 0, this.markerCount);
			this.markers = temp;
		}
		System.arraycopy(markers.markers, 0, this.markers, this.markerCount, markers.markerCount);

		this.markerCount += markers.markerCount;
		this.warnings += markers.warnings;
		this.errors += markers.errors;
	}

	@Override
	public Iterator<Marker> iterator()
	{
		return new ArrayIterator<>(this.markers, this.markerCount);
	}

	@Override
	public String toString()
	{
		return "MarkerList(count: " + this.markerCount + ", errors: " + this.errors + ", warnings: " + this.warnings
			       + ")";
	}
}
