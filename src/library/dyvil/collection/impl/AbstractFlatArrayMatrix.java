package dyvil.collection.impl;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.lang.Int;

import dyvil.collection.Matrix;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.tuple.Tuple2;

public abstract class AbstractFlatArrayMatrix<E> implements Matrix<E>
{
	protected int		rows;
	protected int		columns;
	
	protected Object[]	cells;
	
	public AbstractFlatArrayMatrix()
	{
		this.cells = new Object[0];
	}
	
	public AbstractFlatArrayMatrix(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = new Object[rows * columns];
	}
	
	public AbstractFlatArrayMatrix(int rows, int columns, E[] cells)
	{
		this.rows = rows;
		this.columns = columns;
		
		int len = rows * columns;
		this.cells = new Object[len];
		System.arraycopy(cells, 0, this.cells, 0, len);
	}
	
	public AbstractFlatArrayMatrix(int rows, int columns, Object[] cells, boolean trusted)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = cells;
	}
	
	public AbstractFlatArrayMatrix(Object[]... cells)
	{
		this.rows = cells.length;
		if (this.rows == 0)
		{
			this.cells = new Object[0];
			return;
		}
		
		this.columns = cells[0].length;
		this.cells = new Object[this.rows * this.columns];
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = cells[row];
			System.arraycopy(rowArray, 0, this.cells, row * this.columns, this.columns);
		}
	}
	
	public AbstractFlatArrayMatrix(int rows, int columns, Object[][] cells)
	{
		this.rows = rows;
		this.columns = columns;
		
		this.cells = new Object[rows * columns];
		for (int row = 0; row < rows; row++)
		{
			Object[] rowArray = cells[row];
			System.arraycopy(rowArray, 0, this.cells, row * columns, columns);
		}
	}
	
	protected void rowRangeCheck(int row)
	{
		if (row < 0)
		{
			throw new IndexOutOfBoundsException("Matrix Row out of Bounds: " + row + " < 0");
		}
		if (row >= this.rows)
		{
			throw new IndexOutOfBoundsException("Matrix Row out of Bounds: " + row + " >= " + this.rows);
		}
	}
	
	protected void columnRangeCheck(int column)
	{
		if (column < 0)
		{
			throw new IndexOutOfBoundsException("Matrix Column out of Bounds: " + column + " < 0");
		}
		if (column >= this.columns)
		{
			throw new IndexOutOfBoundsException("Matrix Column out of Bounds: " + column + " >= " + this.columns);
		}
	}
	
	protected void rangeCheck(int row, int column)
	{
		if (row < 0)
		{
			throw new IndexOutOfBoundsException("Matrix Row out of Bounds: " + row + " < 0");
		}
		if (row >= this.rows)
		{
			throw new IndexOutOfBoundsException("Matrix Row out of Bounds: " + row + " >= " + this.rows);
		}
		if (column < 0)
		{
			throw new IndexOutOfBoundsException("Matrix Column out of Bounds: " + column + " < 0");
		}
		if (column >= this.columns)
		{
			throw new IndexOutOfBoundsException("Matrix Column out of Bounds: " + column + " >= " + this.columns);
		}
	}
	
	protected int index(int row, int column)
	{
		return column + row * this.columns;
	}
	
	@Override
	public int rows()
	{
		return this.rows;
	}
	
	@Override
	public int columns()
	{
		return this.columns;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new ArrayIterator(this.cells, this.rows * this.columns);
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		int cells = this.rows * this.columns;
		for (int i = 0; i < cells; i++)
		{
			action.accept((E) this.cells[i]);
		}
	}
	
	@Override
	public boolean contains(Object element)
	{
		return this.indexOf(element) >= 0;
	}
	
	@Override
	public E subscript(int row, int column)
	{
		this.rangeCheck(row, column);
		return (E) this.cells[this.index(row, column)];
	}
	
	@Override
	public E get(int row, int column)
	{
		return (E) this.cells[this.index(row, column)];
	}
	
	protected int indexOf(Object element)
	{
		int cells = this.rows * this.columns;
		if (element == null)
		{
			for (int i = 0; i < cells; i++)
			{
				if (this.cells[i] == null)
				{
					return i;
				}
			}
			return -1;
		}
		for (int i = 0; i < cells; i++)
		{
			if (element.equals(this.cells[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int rowOf(Object element)
	{
		return this.indexOf(element) / this.columns;
	}
	
	@Override
	public int columnOf(Object element)
	{
		return this.indexOf(element) % this.rows;
	}
	
	@Override
	public Tuple2<Int, Int> cellOf(Object element)
	{
		int index = this.indexOf(element);
		return index == -1 ? null : new Tuple2(Int.apply(index / this.columns), Int.apply(index % this.rows));
	}
	
	@Override
	public void rowArray(int row, Object[] store)
	{
		this.rowRangeCheck(row);
		System.arraycopy(this.cells, row * this.columns, store, 0, this.columns);
	}
	
	@Override
	public void columnArray(int column, Object[] store)
	{
		this.columnRangeCheck(column);
		for (int i = 0; i < this.columns; i++)
		{
			store[i] = this.cells[column + i * this.rows];
		}
	}
	
	@Override
	public void toArray(Object[][] store)
	{
		int cells = this.rows * this.columns;
		for (int i = 0; i < cells; i++)
		{
			store[i / this.columns][i % this.rows] = this.cells[i];
		}
	}
	
	@Override
	public void toCellArray(Object[] store)
	{
		System.arraycopy(this.cells, 0, store, 0, this.rows * this.columns);
	}
	
	@Override
	public String toString()
	{
		if (this.rows == 0 || this.columns == 0)
		{
			return "[[]]";
		}
		
		int cells = this.rows * this.columns;
		StringBuilder builder = new StringBuilder(cells * 10).append("[[");
		for (int i = 0, column = 0;;)
		{
			builder.append(this.cells[i]);
			if (++i < cells)
			{
				if (++column == this.columns)
				{
					builder.append("], [");
					column = 0;
				}
				else
				{
					builder.append(", ");
				}
				continue;
			}
			break;
		}
		return builder.append("]]").toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Matrix.matrixEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Matrix.matrixHashCode(this);
	}
}
