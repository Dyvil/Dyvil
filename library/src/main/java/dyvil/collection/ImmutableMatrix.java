package dyvil.collection;

import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.immutable.ArrayMatrix;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;
import dyvil.util.ImmutableException;

import java.util.function.Function;
import java.util.function.UnaryOperator;

@LiteralConvertible.FromArray
public interface ImmutableMatrix<@Covariant E> extends Matrix<E>
{
	@NonNull
	static <E> ImmutableMatrix<E> apply(E[]... elements)
	{
		return new ArrayMatrix(elements);
	}

	@NonNull
	static <E> ImmutableMatrix<E> create(int rows, int columns)
	{
		return new ArrayMatrix<E>(rows, columns);
	}

	// Accessors

	@Override
	default boolean isImmutable()
	{
		return true;
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

	@NonNull
	@Override
	ImmutableMatrix<E> subMatrix(int row, int rows, int column, int columns);

	@NonNull
	@Override
	ImmutableList<E> row(int row);

	@NonNull
	@Override
	ImmutableList<E> column(int column);

	// Non-mutating Operations

	@NonNull
	@Override
	ImmutableList<E> flatten();

	@NonNull
	@Override
	ImmutableMatrix<E> transposed();

	@NonNull
	@Override
	<R> ImmutableMatrix<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	// Mutating Operations

	@Override
	default void resize(int rows, int columns)
	{
		throw new ImmutableException("resize() on Immutable Matrix");
	}

	@Override
	default void addRow(List<E> row)
	{
		throw new ImmutableException("addRow() on Immutable Matrix");
	}

	@Override
	default void addColumn(List<E> column)
	{
		throw new ImmutableException("addColumn() on Immutable Matrix");
	}

	@Override
	default void insertRow(int index, @NonNull List<E> row)
	{
		throw new ImmutableException("insertRow() on Immutable Matrix");
	}

	@Override
	default void insertColumn(int index, @NonNull List<E> column)
	{
		throw new ImmutableException("insertColumn() on Immutable Matrix");
	}

	@Override
	default void subscript_$eq(int row, int column, E element)
	{
		throw new ImmutableException("update() on Immutable Matrix");
	}

	@NonNull
	@Override
	default E set(int row, int column, E element)
	{
		throw new ImmutableException("set() on Immutable Matrix");
	}

	@Override
	default void removeRow(int index)
	{
		throw new ImmutableException("removeRow() on Immutable Matrix");
	}

	@Override
	default void removeColumn(int column)
	{
		throw new ImmutableException("removeColumn() on Immutable Matrix");
	}

	@Override
	default void clear()
	{
		throw new ImmutableException("clear() on Immutable Matrix");
	}

	@Override
	default void transpose()
	{
		throw new ImmutableException("transpose() on Immutable Matrix");
	}

	@Override
	default void map(@NonNull UnaryOperator<E> mapper)
	{
		throw new ImmutableException("map() on Immutable Matrix");
	}

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
	ImmutableMatrix<E> copy();

	@NonNull
	@Override
	MutableMatrix<E> mutable();

	@NonNull
	@Override
	default MutableMatrix<E> mutableCopy()
	{
		return this.mutable();
	}

	@NonNull
	@Override
	default ImmutableMatrix<E> immutable()
	{
		return this;
	}

	@NonNull
	@Override
	default ImmutableMatrix<E> immutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	default ImmutableMatrix<E> view()
	{
		return this;
	}
}
