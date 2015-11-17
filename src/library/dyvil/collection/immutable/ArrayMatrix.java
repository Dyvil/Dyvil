package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import dyvil.lang.Int;

import dyvil.collection.ImmutableList;
import dyvil.collection.ImmutableMatrix;
import dyvil.collection.Matrix;
import dyvil.collection.MutableMatrix;
import dyvil.tuple.Tuple2;

public class ArrayMatrix<E> implements ImmutableMatrix<E>
{
	private static final long serialVersionUID = 7258516530768096953L;
	
	private int	rows;
	private int	columns;
	
	private Object[][] cells;
	
	public ArrayMatrix(E[]... cells)
	{
		this.rows = cells.length;
		if (this.rows == 0)
		{
			this.columns = 0;
			return;
		}
		
		this.columns = cells[0].length;
		this.cells = new Object[this.rows][this.columns];
		for (int i = 0; i < this.rows; i++)
		{
			System.arraycopy(cells[i], 0, this.cells[i], 0, this.columns);
		}
	}
	
	public ArrayMatrix(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = new Object[rows][columns];
	}
	
	public ArrayMatrix(int rows, int columns, E[][] cells)
	{
		this.rows = rows;
		this.columns = columns;
		
		this.cells = new Object[rows][columns];
		for (int i = 0; i < rows; i++)
		{
			System.arraycopy(cells[i], 0, this.cells[i], 0, columns);
		}
	}
	
	public ArrayMatrix(int rows, int columns, Object[][] cells, boolean trusted)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = cells;
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
		return new Iterator<E>()
		{
			private int row;
			private int column;
			
			@Override
			public boolean hasNext()
			{
				return this.column < ArrayMatrix.this.columns && this.row < ArrayMatrix.this.rows;
			}
			
			@Override
			public E next()
			{
				int row = this.row;
				int column = this.column;
				if (this.column + 1 == ArrayMatrix.this.columns)
				{
					this.column = 0;
					this.row++;
				}
				else
				{
					this.column++;
				}
				return (E) ArrayMatrix.this.cells[row][column];
			}
			
			@Override
			public String toString()
			{
				return "ArrayMatrixIterator(matrix: " + ArrayMatrix.this + ", row: " + this.row + ", column: " + this.column + ")";
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = this.cells[row];
			for (int column = 0; column < this.columns; column++)
			{
				action.accept((E) rowArray[column]);
			}
		}
	}
	
	@Override
	public boolean contains(Object element)
	{
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = this.cells[row];
			for (int column = 0; column < this.columns; column++)
			{
				if (Objects.equals(rowArray[column], element))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public E subscript(int row, int column)
	{
		this.rangeCheck(row, column);
		return (E) this.cells[row][column];
	}
	
	@Override
	public E get(int row, int column)
	{
		return (E) this.cells[row][column];
	}
	
	@Override
	public ImmutableMatrix<E> subMatrix(int row, int rows, int column, int columns)
	{
		this.rangeCheck(row, column);
		this.rangeCheck(row + rows - 1, column + columns - 1);
		
		Object[][] newCells = new Object[rows][columns];
		for (int i = 0; i < rows; i++)
		{
			System.arraycopy(this.cells[row + i], column, newCells[i], 0, columns);
		}
		return new ArrayMatrix(rows, columns, newCells, true);
	}
	
	@Override
	public ImmutableList<E> row(int row)
	{
		this.rowRangeCheck(row);
		Object[] array = new Object[this.columns];
		System.arraycopy(this.cells[row], 0, array, 0, this.columns);
		return new ArrayList(array, this.columns, true);
	}
	
	@Override
	public ImmutableList<E> column(int column)
	{
		this.columnRangeCheck(column);
		Object[] array = new Object[this.rows];
		for (int i = 0; i < this.rows; i++)
		{
			array[i] = this.cells[i][column];
		}
		return new ArrayList(array, this.rows, true);
	}
	
	@Override
	public ImmutableList<E> flatten()
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		for (int i = 0; i < this.rows; i++)
		{
			System.arraycopy(this.cells[i], 0, array, i * this.columns, this.columns);
		}
		return new ArrayList(array, len, true);
	}
	
	@Override
	public ImmutableMatrix<E> transposed()
	{
		Object[][] newArray = new Object[this.columns][this.rows];
		for (int i = 0; i < this.rows; i++)
		{
			Object[] newRow = newArray[i];
			for (int j = 0; j < this.columns; j++)
			{
				newRow[j] = this.cells[j][i];
			}
		}
		return new ArrayMatrix(this.columns, this.rows, newArray, true);
	}
	
	@Override
	public <R> ImmutableMatrix<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[][] newArray = new Object[this.columns][this.rows];
		for (int row = 0; row < this.rows; row++)
		{
			Object[] oldRow = this.cells[row];
			Object[] newRow = newArray[row];
			for (int column = 0; column < this.columns; column++)
			{
				newRow[column] = mapper.apply((E) oldRow[column]);
			}
		}
		return new ArrayMatrix(this.rows, this.columns, newArray, true);
	}
	
	@Override
	public int rowOf(Object element)
	{
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = this.cells[row];
			for (int column = 0; column < this.columns; column++)
			{
				if (Objects.equals(rowArray[column], element))
				{
					return row;
				}
			}
		}
		return -1;
	}
	
	@Override
	public int columnOf(Object element)
	{
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = this.cells[row];
			for (int column = 0; column < this.columns; column++)
			{
				if (Objects.equals(rowArray[column], element))
				{
					return column;
				}
			}
		}
		return -1;
	}
	
	@Override
	public Tuple2<Int, Int> cellOf(Object element)
	{
		for (int row = 0; row < this.rows; row++)
		{
			Object[] rowArray = this.cells[row];
			for (int column = 0; column < this.columns; column++)
			{
				if (Objects.equals(rowArray[column], element))
				{
					return new Tuple2(Int.apply(row), Int.apply(column));
				}
			}
		}
		return null;
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
		return new ArrayMatrix(this.rows, this.columns, this.cells);
	}
	
	@Override
	public MutableMatrix<E> mutable()
	{
		return new dyvil.collection.mutable.FlatArrayMatrix(this.rows, this.columns, this.cells);
	}
	
	private void rowToString(StringBuilder builder, Object[] row)
	{
		if (this.columns == 0)
		{
			builder.append("[]");
			return;
		}
		
		builder.append('[');
		builder.append(row[0]);
		for (int i = 1; i < this.columns; i++)
		{
			builder.append(", ").append(row[i]);
		}
		builder.append(']');
	}
	
	@Override
	public String toString()
	{
		if (this.rows == 0)
		{
			return "[[]]";
		}
		
		StringBuilder builder = new StringBuilder(this.rows * this.columns * 10).append('[');
		this.rowToString(builder, this.cells[0]);
		for (int i = 1; i < this.rows; i++)
		{
			builder.append(", ");
			this.rowToString(builder, this.cells[i]);
		}
		return builder.append(']').toString();
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
