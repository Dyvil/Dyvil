package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.ImmutableList;
import dyvil.collection.ImmutableMatrix;
import dyvil.collection.MutableMatrix;
import dyvil.collection.impl.AbstractFlatArrayMatrix;

import java.util.function.Function;

@Immutable
public class FlatArrayMatrix<E> extends AbstractFlatArrayMatrix<E> implements ImmutableMatrix<E>
{
	private static final long serialVersionUID = 7265281981107132533L;

	public FlatArrayMatrix()
	{
		super();
	}

	public FlatArrayMatrix(int rows, int columns, E @NonNull [] cells)
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

	@NonNull
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

	@NonNull
	@Override
	public ImmutableList<E> row(int row)
	{
		this.rowRangeCheck(row);
		Object[] array = new Object[this.columns];
		System.arraycopy(this.cells, row * this.columns, array, 0, this.columns);
		return new ArrayList(array, this.columns, true);
	}

	@NonNull
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

	@NonNull
	@Override
	public ImmutableList<E> flatten()
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		System.arraycopy(this.cells, 0, array, 0, len);
		return new ArrayList(array, len, true);
	}

	@NonNull
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

	@NonNull
	@Override
	public <R> ImmutableMatrix<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		int len = this.rows * this.columns;
		Object[] array = new Object[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = mapper.apply((E) this.cells[i]);
		}
		return new FlatArrayMatrix(this.rows, this.columns, array, true);
	}

	@NonNull
	@Override
	public ImmutableMatrix<E> copy()
	{
		return new FlatArrayMatrix(this.rows, this.columns, this.cells);
	}

	@NonNull
	@Override
	public MutableMatrix<E> mutable()
	{
		return new dyvil.collection.mutable.FlatArrayMatrix(this.rows, this.columns, this.cells);
	}
}
