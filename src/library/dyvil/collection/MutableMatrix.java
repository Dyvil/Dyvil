package dyvil.collection;

import dyvil.collection.mutable.FlatArrayMatrix;
import dyvil.collection.view.MatrixView;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;

import java.util.function.Function;
import java.util.function.UnaryOperator;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
public interface MutableMatrix<E> extends Matrix<E>
{
	static <E> MutableMatrix<E> apply()
	{
		return new FlatArrayMatrix();
	}
	
	static <E> MutableMatrix<E> apply(int rows, int columns)
	{
		return new FlatArrayMatrix(rows, columns);
	}
	
	static <E> MutableMatrix<E> apply(E[]... cells)
	{
		return new FlatArrayMatrix(cells);
	}
	
	// Accessors
	
	@Override
	default boolean isImmutable()
	{
		return false;
	}
	
	@Override
	int rows();
	
	@Override
	int columns();
	
	@Override
	boolean contains(Object element);
	
	@Override
	E subscript(int row, int column);
	
	@Override
	E get(int row, int column);
	
	// Sub-view Operations
	
	@Override
	MutableMatrix<E> subMatrix(int row, int rows, int column, int columns);
	
	@Override
	MutableList<E> row(int row);
	
	@Override
	MutableList<E> column(int column);
	
	// Non-mutating Operations
	
	@Override
	MutableList<E> flatten();
	
	@Override
	MutableMatrix<E> transposed();
	
	@Override
	<R> MutableMatrix<R> mapped(Function<? super E, ? extends R> mapper);
	
	// Mutating Operations
	
	@Override
	void resize(int rows, int columns);
	
	@Override
	void addRow(List<E> row);
	
	@Override
	void addColumn(List<E> column);
	
	@Override
	void insertRow(int index, List<E> row);
	
	@Override
	void insertColumn(int index, List<E> column);
	
	@Override
	void subscript_$eq(int row, int column, E element);
	
	@Override
	E set(int row, int column, E element);
	
	@Override
	void removeRow(int index);
	
	@Override
	void removeColumn(int column);
	
	@Override
	void clear();
	
	@Override
	void transpose();
	
	@Override
	void map(UnaryOperator<E> mapper);
	
	// Search Operations
	
	@Override
	int rowOf(Object element);
	
	@Override
	int columnOf(Object element);
	
	@Override
	Tuple.Of2<Integer, Integer> cellOf(Object element);
	
	// toArray
	
	@Override
	void rowArray(int row, Object[] store);
	
	@Override
	void columnArray(int column, Object[] store);
	
	@Override
	void toArray(Object[][] store);
	
	@Override
	void toCellArray(Object[] store);
	
	// Copying
	
	@Override
	MutableMatrix<E> copy();
	
	@Override
	default MutableMatrix<E> mutable()
	{
		return this;
	}
	
	@Override
	default MutableMatrix<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	ImmutableMatrix<E> immutable();
	
	@Override
	default ImmutableMatrix<E> immutableCopy()
	{
		return this.immutable();
	}
	
	@Override
	default ImmutableMatrix<E> view()
	{
		return new MatrixView(this);
	}
}
