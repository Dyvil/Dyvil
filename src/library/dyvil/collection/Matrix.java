package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.annotation.internal.Primitive;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@LiteralConvertible.FromArray
public interface Matrix<E> extends Iterable<E>, Serializable
{
	static <E> @NonNull MutableMatrix<E> apply()
	{
		return MutableMatrix.apply();
	}

	static <E> @NonNull MutableMatrix<E> apply(int rows, int columns)
	{
		return MutableMatrix.apply(rows, columns);
	}

	static <E> @NonNull ImmutableMatrix<E> apply(E @NonNull []... elements)
	{
		return ImmutableMatrix.apply(elements);
	}

	// Accessors

	boolean isImmutable();

	int rows();

	int columns();

	default int cells()
	{
		return this.rows() * this.columns();
	}

	default boolean isEmpty()
	{
		return this.rows() == 0 || this.columns() == 0;
	}

	@NonNull
	@Override
	Iterator<E> iterator();

	@NonNull
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.cells(), Spliterator.SIZED);
	}

	@NonNull
	default Stream<E> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}

	@NonNull
	default Stream<E> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}

	boolean contains(Object element);

	E subscript(int row, int column);

	E get(int row, int column);

	// Sub-view Operations

	@NonNull Matrix<E> subMatrix(int row, int rows, int column, int columns);

	@NonNull List<E> row(int row);

	@NonNull List<E> column(int column);

	// Non-mutating Operations

	@NonNull List<E> flatten();

	@NonNull Matrix<E> transposed();

	<R> @NonNull Matrix<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	// Mutating Operations

	void resize(int rows, int columns);

	void addRow(List<E> row);

	void addColumn(List<E> column);

	void insertRow(int index, @NonNull List<E> row);

	void insertColumn(int index, @NonNull List<E> column);

	void subscript_$eq(int row, int column, E element);

	E set(int row, int column, E element);

	void removeRow(int index);

	void removeColumn(int column);

	void clear();

	void transpose();

	void map(@NonNull UnaryOperator<E> mapper);

	// Search Operations

	int rowOf(Object element);

	int columnOf(Object element);

	Tuple.@Nullable Of2<@Primitive Integer, @Primitive Integer> cellOf(Object element);

	// toArray

	default Object @NonNull [] rowArray(int row)
	{
		Object[] array = new Object[this.columns()];
		this.rowArray(row, array);
		return array;
	}

	default E @NonNull [] rowArray(int row, @NonNull Class<E> type)
	{
		@SuppressWarnings("unchecked") E[] array = (E[]) Array.newInstance(type, this.columns());
		this.rowArray(row, array);
		return array;
	}

	void rowArray(int row, Object[] store);

	default Object @NonNull [] columnArray(int column)
	{
		Object[] array = new Object[this.rows()];
		this.columnArray(column, array);
		return array;
	}

	default E @NonNull [] columnArray(int column, @NonNull Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.rows());
		return array;
	}

	void columnArray(int column, Object @NonNull [] store);

	@NonNull
	default Object @NonNull [] @NonNull [] toArray()
	{
		Object[][] array = new Object[this.rows()][this.columns()];
		this.toArray(array);
		return array;
	}

	@NonNull
	default E @NonNull [] @NonNull [] toArray(@NonNull Class<E> type)
	{
		@SuppressWarnings("unchecked") E[][] array = (E[][]) Array.newInstance(type, this.rows(), this.columns());
		this.toArray(array);
		return array;
	}

	void toArray(Object @NonNull [] @NonNull [] store);

	default Object @NonNull [] toFlatArray()
	{
		Object[] array = new Object[this.cells()];
		this.toCellArray(array);
		return array;
	}

	default E @NonNull [] toCellArray(@NonNull Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.cells());
		this.toCellArray(array);
		return array;
	}

	void toCellArray(Object @NonNull [] store);

	// Copying and Views

	@NonNull Matrix<E> copy();

	@NonNull MutableMatrix<E> mutable();

	@NonNull MutableMatrix<E> mutableCopy();

	@NonNull ImmutableMatrix<E> immutable();

	@NonNull ImmutableMatrix<E> immutableCopy();

	@NonNull ImmutableMatrix<E> view();

	// toString, equals and hashCode

	@NonNull
	@Override
	String toString();

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	static <E> String matrixToString(@NonNull Matrix<E> matrix)
	{
		if (matrix.isEmpty())
		{
			return "[[]]";
		}

		int columns = matrix.columns();
		int column = 0;

		StringBuilder builder = new StringBuilder("[[");
		Iterator<E> iterator = matrix.iterator();
		while (true)
		{
			builder.append(iterator.next());
			if (iterator.hasNext())
			{
				if (++column >= columns)
				{
					builder.append("], [");
					column = 0;
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

	static <E> boolean matrixEquals(@NonNull Matrix<E> matrix, Object o)
	{
		if (!(o instanceof Matrix))
		{
			return false;
		}

		return matrixEquals(matrix, (Matrix) o);
	}

	static <E> boolean matrixEquals(@NonNull Matrix<E> m1, @NonNull Matrix<E> m2)
	{
		int rows = m1.rows();
		if (rows != m2.rows())
		{
			return false;
		}
		int columns = m1.columns();
		if (columns != m2.columns())
		{
			return false;
		}

		Iterator<E> iterator1 = m1.iterator();
		Iterator<E> iterator2 = m2.iterator();
		while (iterator1.hasNext())
		{
			E e1 = iterator1.next();
			E e2 = iterator2.next();
			if (!Objects.equals(e1, e2))
			{
				return false;
			}
		}
		return true;
	}

	static <E> int matrixHashCode(@NonNull Matrix<E> matrix)
	{
		int result = matrix.rows() * 31 + matrix.columns();
		for (Object o : matrix)
		{
			result = result * 31 + (o == null ? 0 : o.hashCode());
		}
		return result;
	}
}
