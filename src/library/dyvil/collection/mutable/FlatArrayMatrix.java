package dyvil.collection.mutable;

import java.util.BitSet;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import dyvil.lang.List;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMatrix;
import dyvil.collection.MutableList;
import dyvil.collection.MutableMatrix;
import dyvil.collection.impl.AbstractFlatArrayMatrix;

@NilConvertible
public class FlatArrayMatrix<E> extends AbstractFlatArrayMatrix<E> implements MutableMatrix<E>
{
	public static <E> FlatArrayMatrix<E> apply()
	{
		return new FlatArrayMatrix();
	}
	
	public FlatArrayMatrix()
	{
		super();
	}
	
	public FlatArrayMatrix(int rows, int columns, E[] cells)
	{
		super(rows, columns, cells);
	}
	
	public FlatArrayMatrix(int rows, int columns, Object[] cells, boolean trusted)
	{
		super(rows, columns, cells, trusted);
	}
	
	public FlatArrayMatrix(int rows, int columns, Object[][] cells)
	{
		super(rows, columns, cells);
	}
	
	public FlatArrayMatrix(int rows, int columns)
	{
		super(rows, columns);
	}
	
	public FlatArrayMatrix(Object[]... cells)
	{
		super(cells);
	}
	
	@Override
	public MutableMatrix<E> subMatrix(int row, int rows, int column, int columns)
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
		return new FlatArrayMatrix(this.columns, this.rows, newArray, true);
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
		return new FlatArrayMatrix(this.rows, this.columns, array, true);
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
				System.arraycopy(this.cells, i * this.columns, newCells, i * newColumns, index);
			}
			newCells[i * newColumns + index] = column.get(i);
			System.arraycopy(this.cells, i * this.columns + index, newCells, i * newColumns + index + 1, this.columns - index);
		}
		this.columns = newColumns;
		this.cells = newCells;
	}
	
	@Override
	public void subscript_$eq(int row, int column, E element)
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
				System.arraycopy(this.cells, i * this.columns, newCells, i * newColumns, column);
			}
			System.arraycopy(this.cells, i * this.columns + column + 1, newCells, i * newColumns + column, this.columns - column - 1);
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
	
	@Override
	public MutableMatrix<E> copy()
	{
		return new FlatArrayMatrix(this.rows, this.columns, this.cells);
	}
	
	@Override
	public ImmutableMatrix<E> immutable()
	{
		return new dyvil.collection.immutable.FlatArrayMatrix(this.rows, this.columns, this.cells);
	}
}
