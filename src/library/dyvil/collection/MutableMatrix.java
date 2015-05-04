package dyvil.collection;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import dyvil.lang.Int;
import dyvil.lang.List;
import dyvil.lang.Matrix;
import dyvil.tuple.Tuple2;

public interface MutableMatrix<E> extends Matrix<E>
{
	// Accessors
	
	@Override
	public int rows();
	
	@Override
	public int columns();
	
	@Override
	public boolean $qmark(Object element);
	
	@Override
	public E apply(int row, int column);
	
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
	public List<E> flatten();
	
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
	public void update(int row, int column, E element);
	
	@Override
	public E set(int row, int column, E element);
	
	@Override
	public void removeRow(int index);
	
	@Override
	public void removeColumn();
	
	@Override
	public void clear();
	
	@Override
	public void transpose();
	
	@Override
	public void map(BinaryOperator<E> mapper);
	
	// Search Operations
	
	@Override
	public int rowOf(E element);
	
	@Override
	public int columnOf(E element);
	
	@Override
	public Tuple2<Int, Int> cellOf(E element);
	
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
	public MutableMatrix<E> mutable();
	
	@Override
	public ImmutableMatrix<E> immutable();
}
