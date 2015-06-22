package dyvil.collection;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import dyvil.lang.Int;
import dyvil.lang.List;
import dyvil.lang.Matrix;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.FlatArrayMatrix;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public interface MutableMatrix<E> extends Matrix<E>
{
	public static <E> MutableMatrix<E> apply()
	{
		return new FlatArrayMatrix();
	}
	
	public static <E> MutableMatrix<E> apply(int rows, int columns)
	{
		return new FlatArrayMatrix(rows, columns);
	}
	
	public static <E> MutableMatrix<E> apply(E[]... cells)
	{
		return new FlatArrayMatrix(cells);
	}
	
	// Accessors
	
	@Override
	public int rows();
	
	@Override
	public int columns();
	
	@Override
	public boolean contains(Object element);
	
	@Override
	public E subscript(int row, int column);
	
	@Override
	public E get(int row, int column);
	
	// Sub-view Operations
	
	@Override
	public MutableMatrix<E> subMatrix(int row, int rows, int column, int columns);
	
	@Override
	public MutableList<E> row(int row);
	
	@Override
	public MutableList<E> column(int column);
	
	// Non-mutating Operations
	
	@Override
	public MutableList<E> flatten();
	
	@Override
	public MutableMatrix<E> transposed();
	
	@Override
	public <R> MutableMatrix<R> mapped(Function<? super E, ? extends R> mapper);
	
	// Mutating Operations
	
	@Override
	public void resize(int rows, int columns);
	
	@Override
	public void addRow(List<E> row);
	
	@Override
	public void addColumn(List<E> column);
	
	@Override
	public void insertRow(int index, List<E> row);
	
	@Override
	public void insertColumn(int index, List<E> column);
	
	@Override
	public void subscript_$eq(int row, int column, E element);
	
	@Override
	public E set(int row, int column, E element);
	
	@Override
	public void removeRow(int index);
	
	@Override
	public void removeColumn(int column);
	
	@Override
	public void clear();
	
	@Override
	public void transpose();
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	// Search Operations
	
	@Override
	public int rowOf(Object element);
	
	@Override
	public int columnOf(Object element);
	
	@Override
	public Tuple2<Int, Int> cellOf(Object element);
	
	// toArray
	
	@Override
	public void rowArray(int row, Object[] store);
	
	@Override
	public void columnArray(int column, Object[] store);
	
	@Override
	public void toArray(Object[][] store);
	
	@Override
	public void toCellArray(Object[] store);
	
	// Copying
	
	@Override
	public MutableMatrix<E> copy();
	
	@Override
	public default MutableMatrix<E> mutable()
	{
		return this;
	}
	
	@Override
	public default MutableMatrix<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public ImmutableMatrix<E> immutable();
	
	@Override
	public default ImmutableMatrix<E> immutableCopy()
	{
		return this.immutable();
	}
}
