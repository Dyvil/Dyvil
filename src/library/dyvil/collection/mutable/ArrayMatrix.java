package dyvil.collection.mutable;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import dyvil.collection.ArrayIterator;
import dyvil.collection.ImmutableMatrix;
import dyvil.collection.MutableList;
import dyvil.collection.MutableMatrix;
import dyvil.lang.Int;
import dyvil.lang.List;
import dyvil.lang.Matrix;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;

@NilConvertible
public class ArrayMatrix<E> implements MutableMatrix<E>
{
	private int			rows;
	private int			columns;
	
	private Object[]	cells;
	
	public static <E> ArrayList<E> apply()
	{
		return new ArrayList();
	}
	
	public ArrayMatrix()
	{
		this.cells = new Object[0];
	}
	
	public ArrayMatrix(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = new Object[rows * columns];
	}
	
	public ArrayMatrix(int rows, int columns, E[] cells)
	{
		this.rows = rows;
		this.columns = columns;
		
		int len = rows * columns;
		this.cells = new Object[len];
		System.arraycopy(cells, 0, this.cells, 0, len);
	}
	
	public ArrayMatrix(int rows, int columns, Object[] cells, boolean trusted)
	{
		this.rows = rows;
		this.columns = columns;
		this.cells = cells;
	}
	
	public ArrayMatrix(Object[][] cells)
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
	public MutableMatrix<E> subMatrix(int row, int rows, int column, int columns)
	{
		this.rangeCheck(row, column);
		this.rangeCheck(row + rows - 1, column + columns - 1);
		Object[] newCells = new Object[rows * columns];
		for (int i = 0; i < columns; i++)
		{
			System.arraycopy(this.cells, this.rows * i, newCells, rows * i, columns);
		}
		return new ArrayMatrix(rows, columns, this.cells, true);
	}
	
	@Override
	public MutableList<E> row(int row)
	{
		this.rowRangeCheck(row);
		Object[] array = new Object[this.columns];
		System.arraycopy(this.cells, row * this.columns, array, 0, this.columns);
		return new ArrayList(array, this.columns, true);
	}
	
	@Override
	public MutableList<E> column(int column)
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
	public MutableList<E> flatten()
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		System.arraycopy(this.cells, 0, array, 0, len);
		return new ArrayList(array, len, true);
	}
	
	@Override
	public MutableMatrix<E> transposed()
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
		return new ArrayMatrix(this.columns, this.rows, newArray, true);
	}
	
	@Override
	public <R> MutableMatrix<R> mapped(Function<? super E, ? extends R> mapper)
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = mapper.apply((E) this.cells[i]);
		}
		return new ArrayMatrix(this.rows, this.columns, array, true);
	}
	
	@Override
	public void resize(int rows, int columns)
	{
		if (columns == this.columns)
		{
			Object[] newArray = new Object[rows * columns];
			System.arraycopy(this.cells, 0, newArray, 0, this.rows * columns);
			this.cells = newArray;
			this.rows = rows;
			return;
		}
		
		Object[] newArray = new Object[rows * columns];
		for (int i = 0; i < this.rows; i++)
		{
			// Copy row by row
			System.arraycopy(this.cells, i * this.columns, newArray, i * columns, this.columns);
		}
		this.cells = newArray;
		this.columns = columns;
		this.rows = rows;
	}
	
	@Override
	public void addRow(List<E> row)
	{
		int size = row.size();
		if (this.columns == 0 || this.rows == 0)
		{
			this.columns = size;
			this.rows = 0;
		}
		else if (size > this.columns)
		{
			throw new IllegalArgumentException("Invalid Row: " + size + " > " + this.columns);
		}
		
		int oldRows = this.rows;
		this.resize(this.rows + 1, this.columns);
		
		// Let the row do it's thing, but with the base offset.
		row.toArray(oldRows * this.columns, this.cells);
	}
	
	@Override
	public void addColumn(List<E> column)
	{
		int size = column.size();
		if (this.columns == 0 || this.rows == 0)
		{
			this.rows = size;
			this.columns = 0;
		}
		else if (size > this.rows)
		{
			throw new IllegalArgumentException("Invalid Column: " + size + " > " + this.rows);
		}
		
		int oldColumns = this.columns;
		this.resize(this.rows, this.columns + 1);
		
		for (int i = 0; i < size; i++)
		{
			this.cells[oldColumns + i * this.columns] = column.get(i);
		}
	}
	
	@Override
	public void insertRow(int index, List<E> row)
	{
		if (index == this.rows)
		{
			this.addRow(row);
			return;
		}
		
		this.rowRangeCheck(index);
		int size = row.size();
		if (this.columns == 0 || this.rows == 0)
		{
			this.columns = size;
			this.rows = 0;
		}
		else if (size > this.columns)
		{
			throw new IllegalArgumentException("Invalid Row: " + size + " > " + this.columns);
		}
		
		int oldRows = this.rows;
		int cellIndex = this.columns * index;
		this.resize(this.rows + 1, this.columns);
		System.arraycopy(this.cells, cellIndex, this.cells, cellIndex + this.columns, oldRows * this.columns - cellIndex);
		row.toArray(cellIndex, this.cells);
	}
	
	@Override
	public void insertColumn(int index, List<E> column)
	{
		if (index == this.columns)
		{
			this.addColumn(column);
			return;
		}
		
		this.columnRangeCheck(index);
		int size = column.size();
		if (this.columns == 0 || this.rows == 0)
		{
			this.rows = size;
			this.columns = 0;
		}
		else if (size > this.rows)
		{
			throw new IllegalArgumentException("Invalid Column: " + size + " > " + this.rows);
		}
		
		int newColumns = this.columns + 1;
		Object[] newCells = new Object[this.rows * newColumns];
		for (int i = 0; i < this.rows; i++)
		{
			if (index > 0)
			{
				System.arraycopy(this.cells, i * columns, newCells, i * newColumns, index);
			}
			newCells[i * newColumns + index] = column.get(i);
			System.arraycopy(this.cells, i * columns + index, newCells, i * newColumns + index + 1, this.columns - index);
		}
		this.columns = newColumns;
		this.cells = newCells;
	}
	
	@Override
	public void update(int row, int column, E element)
	{
		this.rangeCheck(row, column);
		this.cells[this.index(row, column)] = element;
	}
	
	@Override
	public E set(int row, int column, E element)
	{
		int index = this.index(row, column);
		E prev = (E) this.cells[index];
		this.cells[index] = element;
		return prev;
	}
	
	@Override
	public void removeRow(int index)
	{
		this.rowRangeCheck(index);
		
		int start = (index + 1) * this.columns;
		int end = index * this.columns;
		int cells = this.rows * this.columns;
		System.arraycopy(this.cells, start, this.cells, end, cells - start);
		for (int i = cells - end; i < cells; i++)
		{
			// Null the remaining cells to let GC do its thing
			this.cells[i] = null;
		}
		this.rows--;
	}
	
	@Override
	public void removeColumn(int column)
	{
		this.columnRangeCheck(column);
		
		int newColumns = this.columns - 1;
		Object[] newCells = new Object[this.rows * newColumns];
		for (int i = 0; i < this.rows; i++)
		{
			if (column > 0)
			{
				System.arraycopy(this.cells, i * columns, newCells, i * newColumns, column);
			}
			System.arraycopy(this.cells, i * columns + column + 1, newCells, i * newColumns + column, this.columns - column - 1);
		}
		this.columns = newColumns;
		this.cells = newCells;
	}
	
	@Override
	public void clear()
	{
		this.rows = this.columns = 0;
	}
	
	private void swap(int i, int j)
	{
		Object o = this.cells[i];
		this.cells[i] = this.cells[j];
		this.cells[j] = o;
	}
	
	@Override
	public void transpose()
	{
		if (this.rows == this.columns)
		{
			// Square Matrix
			for (int i = 0; i < this.rows - 1; i++)
			{
				for (int j = i + 1; j < this.rows; j++)
				{
					this.swap(this.index(i, j), this.index(j, i));
				}
			}
			
			return;
		}
		
		int rows = this.rows;
		int size = rows * this.columns - 1;
		// holds element to be replaced, eventually becomes next element to move
		int prev;
		// location of 't' to be moved
		int next;
		// holds start of cycle
		int cycleBegin;
		BitSet cycle = new BitSet(size); // hash to mark moved elements
		cycle.set(0);
		cycle.set(size);
		
		for (int i = 1; i < size;)
		{
			cycleBegin = i;
			prev = i;
			do
			{
				next = i * rows % size;
				
				this.swap(next, prev);
				cycle.set(i);
				i = next;
			}
			while (i != cycleBegin);
			
			for (i = 1; i < size && cycle.get(i); i++)
			{
				;
			}
		}
		
		this.rows = this.columns;
		this.columns = rows;
	}
	
	@Override
	public void map(UnaryOperator<E> mapper)
	{
		int cells = this.rows * this.columns;
		for (int i = 0; i < cells; i++)
		{
			this.cells[i] = mapper.apply((E) this.cells[i]);
		}
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
	public MutableMatrix<E> copy()
	{
		return new ArrayMatrix(this.rows, this.columns, this.cells);
	}
	
	@Override
	public ImmutableMatrix<E> immutable()
	{
		// FIXME
		return null;
	}
	
	private void rowToString(StringBuilder builder, int row)
	{
		if (this.columns == 0)
		{
			builder.append("[]");
			return;
		}
		
		builder.append('[');
		int index = row * this.columns;
		builder.append(this.cells[index]);
		for (int i = 1; i < this.columns; i++)
		{
			builder.append(", ").append(this.cells[index + i]);
		}
		builder.append(']');
	}
	
	@Override
	public String toString()
	{
		if (this.rows == 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder(this.rows * this.columns * 10).append('[');
		this.rowToString(builder, 0);
		for (int i = 1; i < this.rows; i++)
		{
			builder.append(", ");
			this.rowToString(builder, i);
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
