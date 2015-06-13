package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import dyvil.collection.ImmutableList;
import dyvil.collection.ImmutableMatrix;
import dyvil.collection.MutableMatrix;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Int;
import dyvil.lang.Matrix;
import dyvil.tuple.Tuple2;

public class FlatArrayMatrix<E> implements ImmutableMatrix<E>
{
	private int			rows;
	private int			columns;
	
	private Object[]	cells;
	
	public FlatArrayMatrix()
	{
		this.cells = new Object[0];
	}
	
	public FlatArrayMatrix(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = new Object[rows * columns];
	}
	
	public FlatArrayMatrix(int rows, int columns, E[] cells)
	{
		this.rows = rows;
		this.columns = columns;
		
		int len = rows * columns;
		this.cells = new Object[len];
		System.arraycopy(cells, 0, this.cells, 0, len);
	}
	
	public FlatArrayMatrix(int rows, int columns, Object[] cells, boolean trusted)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = cells;
	}
	
	public FlatArrayMatrix(Object[][] cells)
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
	
	private void rowRangeCheck(int row)
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
	
	private void columnRangeCheck(int column)
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
	
	private void rangeCheck(int row, int column)
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
	
	private int index(int row, int column)
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
	public boolean $qmark(Object element)
	{
		return this.indexOf(element) >= 0;
	}
	
	@Override
	public E apply(int row, int column)
	{
		this.rangeCheck(row, column);
		return (E) this.cells[this.index(row, column)];
	}
	
	@Override
	public E get(int row, int column)
	{
		return (E) this.cells[this.index(row, column)];
	}
	
	@Override
	public ImmutableMatrix<E> subMatrix(int row, int rows, int column, int columns)
	{
		this.rangeCheck(row, column);
		this.rangeCheck(row + rows - 1, column + columns - 1);
		Object[] newCells = new Object[rows * columns];
		for (int i = 0; i < rows; i++)
		{
			System.arraycopy(this.cells, this.index(row + i, column), newCells, columns * i, columns);
		}
		return new FlatArrayMatrix(rows, columns, newCells, true);
	}
	
	@Override
	public ImmutableList<E> row(int row)
	{
		this.rowRangeCheck(row);
		Object[] array = new Object[this.columns];
		System.arraycopy(this.cells, row * this.columns, array, 0, this.columns);
		return new ArrayList(array, this.columns, true);
	}
	
	@Override
	public ImmutableList<E> column(int column)
	{
		this.columnRangeCheck(column);
		Object[] array = new Object[this.rows];
		for (int i = 0; i < this.columns; i++)
		{
			array[i] = this.cells[column + i * this.rows];
		}
		return new ArrayList(array, this.rows, true);
	}
	
	@Override
	public ImmutableList<E> flatten()
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		System.arraycopy(this.cells, 0, array, 0, len);
		return new ArrayList(array, len, true);
	}
	
	@Override
	public ImmutableMatrix<E> transposed()
	{
		int len = this.rows * this.columns;
		Object[] newArray = new Object[len];
		for (int i = 0; i < this.rows; i++)
		{
			int i1 = i * this.columns;
			for (int j = 0; j < this.columns; j++)
			{
				newArray[i + j * this.rows] = this.cells[j + i1];
			}
		}
		return new FlatArrayMatrix(this.columns, this.rows, newArray, true);
	}
	
	@Override
	public <R> ImmutableMatrix<R> mapped(Function<? super E, ? extends R> mapper)
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = mapper.apply((E) this.cells[i]);
		}
		return new FlatArrayMatrix(this.rows, this.columns, array, true);
	}
	
	private int indexOf(Object element)
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
	public ImmutableMatrix<E> copy()
	{
		return new FlatArrayMatrix(this.rows, this.columns, this.cells);
	}
	
	@Override
	public MutableMatrix<E> mutable()
	{
		return new dyvil.collection.mutable.ArrayMatrix(this.rows, this.columns, this.cells);
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
		for (int i = 0;;)
		{
			builder.append(this.cells[i]);
			if (++i < cells)
			{
				if (i == this.columns)
				{
					builder.append("], [");
				}
				else
				{
					builder.append(", ");
				}
			}
			else
			{
				break;
			}
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
