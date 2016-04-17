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

	public void remove(int count)
	{
		int warnings = 0;
		int errors = 0;
		for (int i = this.markerCount - count; i < this.markerCount; i++)
		{
			final Marker marker = this.markers[i];
			if (marker.isError())
			{
				errors++;
			}
			if (marker.isWarning())
			{
				warnings++;
			}
		}

		this.markerCount -= count;
		this.warnings -= warnings;
		this.errors -= errors;
	}

	@Override
	public Iterator<Marker> iterator()
	{
		return new ArrayIterator<>(this.markers, this.markerCount);
	}
}
