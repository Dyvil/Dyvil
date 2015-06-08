package dyvil.lang;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import dyvil.collection.ImmutableMatrix;
import dyvil.collection.MutableMatrix;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.tuple.Tuple2;

@ArrayConvertible
public interface Matrix<E> extends Iterable<E>
{
	public static <E> MutableMatrix<E> apply()
	{
		return MutableMatrix.<E> apply();
	}
	
	// Accessors
	
	public int rows();
	
	public int columns();
	
	public default int cells()
	{
		return this.rows() * this.columns();
	}
	
	public default boolean isEmpty()
	{
		return this.rows() * this.columns() == 0;
	}
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.cells(), Spliterator.SIZED);
	}
	
	public default Stream<E> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	public default Stream<E> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	public boolean $qmark(Object element);
	
	public E apply(int row, int column);
	
	public E get(int row, int column);
	
	// Sub-view Operations
	
	public Matrix<E> subMatrix(int row, int rows, int column, int columns);
	
	public List<E> row(int row);
	
	public List<E> column(int column);
	
	// Non-mutating Operations
	
	public List<E> flatten();
	
	public Matrix<E> transposed();
	
	public <R> Matrix<R> mapped(Function<? super E, ? extends R> mapper);
	
	// Mutating Operations
	
	public void resize(int rows, int columns);
	
	public void addRow(List<E> row);
	
	public void addColumn(List<E> column);
	
	public void insertRow(int index, List<E> row);
	
	public void insertColumn(int index, List<E> column);
	
	public void update(int row, int column, E element);
	
	public E set(int row, int column, E element);
	
	public void removeRow(int index);
	
	public void removeColumn(int column);
	
	public void clear();
	
	public void transpose();
	
	public void map(UnaryOperator<E> mapper);
	
	// Search Operations
	
	public int rowOf(Object element);
	
	public int columnOf(Object element);
	
	public Tuple2<Int, Int> cellOf(Object element);
	
	// toArray
	
	public default Object[] rowArray(int row)
	{
		Object[] array = new Object[this.columns()];
		this.rowArray(row, array);
		return array;
	}
	
	public default E[] rowArray(int row, Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.columns());
		this.rowArray(row, array);
		return array;
	}
	
	public void rowArray(int row, Object[] store);
	
	public default Object[] columnArray(int column)
	{
		Object[] array = new Object[this.rows()];
		this.columnArray(column, array);
		return array;
	}
	
	public default E[] columnArray(int column, Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.rows());
		return array;
	}
	
	public void columnArray(int column, Object[] store);
	
	public default Object[][] toArray()
	{
		Object[][] array = new Object[this.rows()][this.columns()];
		this.toArray(array);
		return array;
	}
	
	public default E[][] toArray(Class<E> type)
	{
		E[][] array = (E[][]) Array.newInstance(type, this.rows(), this.columns());
		this.toArray(array);
		return array;
	}
	
	public void toArray(Object[][] store);
	
	public default Object[] toFlatArray()
	{
		Object[] array = new Object[this.cells()];
		this.toCellArray(array);
		return array;
	}
	
	public default E[] toCellArray(Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.cells());
		this.toCellArray(array);
		return array;
	}
	
	public void toCellArray(Object[] store);
	
	// Copying
	
	public Matrix<E> copy();
	
	public MutableMatrix<E> mutable();
	
	public ImmutableMatrix<E> immutable();
}
