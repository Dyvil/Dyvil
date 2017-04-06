package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.mutable.FlatArrayMatrix;
import dyvil.collection.view.MatrixView;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;

import java.util.function.Function;
import java.util.function.UnaryOperator;

@LiteralConvertible.FromArray
public interface MutableMatrix<E> extends Matrix<E>
{
	@NonNull
	static <E> MutableMatrix<E> apply()
	{
		return new FlatArrayMatrix();
	}

	@NonNull
	static <E> MutableMatrix<E> apply(int rows, int columns)
	{
		return new FlatArrayMatrix(rows, columns);
	}

	@NonNull
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

	@NonNull
	@Override
	E subscript(int row, int column);

	@NonNull
	@Override
	E get(int row, int column);

	// Sub-view Operations

	@NonNull
	@Override
	MutableMatrix<E> subMatrix(int row, int rows, int column, int columns);

	@NonNull
	@Override
	MutableList<E> row(int row);

	@NonNull
	@Override
	MutableList<E> column(int column);

	// Non-mutating Operations

	@NonNull
	@Override
	MutableList<E> flatten();

	@NonNull
	@Override
	MutableMatrix<E> transposed();

	@NonNull
	@Override
	<R> MutableMatrix<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	// Mutating Operations

	@Override
	void resize(int rows, int columns);

	@Override
	void addRow(List<E> row);

	@Override
	void addColumn(List<E> column);

	@Override
	void insertRow(int index, @NonNull List<E> row);

	@Override
	void insertColumn(int index, @NonNull List<E> column);

	@Override
	void subscript_$eq(int row, int column, E element);

	@NonNull
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
	void map(@NonNull UnaryOperator<E> mapper);

	// Search Operations

	@Override
	int rowOf(Object element);

	@Override
	int columnOf(Object element);

	@Override
	Tuple.@Nullable Of2<Integer, Integer> cellOf(Object element);

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

	@NonNull
	@Override
	MutableMatrix<E> copy();

	@NonNull
	@Override
	default MutableMatrix<E> mutable()
	{
		return this;
	}

	@NonNull
	@Override
	default MutableMatrix<E> mutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	ImmutableMatrix<E> immutable();

	@NonNull
	@Override
	default ImmutableMatrix<E> immutableCopy()
	{
		return this.immutable();
	}

	@NonNull
	@Override
	default ImmutableMatrix<E> view()
	{
		return new MatrixView(this);
	}
}
