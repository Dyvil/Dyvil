package dyvil.collection.view;

import dyvil.annotation.Immutable;
import dyvil.collection.ImmutableList;
import dyvil.collection.ImmutableMatrix;
import dyvil.collection.Matrix;
import dyvil.collection.MutableMatrix;
import dyvil.collection.iterator.ImmutableIterator;
import dyvil.tuple.Tuple;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

@Immutable
public class MatrixView<E> implements ImmutableMatrix<E>
{
	private static final long serialVersionUID = -3291167467848562079L;
	
	protected final Matrix<E> matrix;
	
	public MatrixView(Matrix<E> matrix)
	{
		this.matrix = matrix;
	}
	
	@Override
	public int rows()
	{
		return this.matrix.rows();
	}
	
	@Override
	public int columns()
	{
		return this.matrix.columns();
	}
	
	@Override
	public int cells()
	{
		return this.matrix.cells();
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return this.matrix.isImmutable() ? this.matrix.iterator() : new ImmutableIterator(this.matrix.iterator());
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		this.matrix.forEach(action);
	}
	
	@Override
	public boolean contains(Object element)
	{
		return this.matrix.contains(element);
	}
	
	@Override
	public E subscript(int row, int column)
	{
		return this.matrix.subscript(row, column);
	}
	
	@Override
	public E get(int row, int column)
	{
		return this.matrix.get(row, column);
	}
	
	@Override
	public ImmutableMatrix<E> subMatrix(int row, int rows, int column, int columns)
	{
		return new MatrixView(this.matrix.subMatrix(row, rows, column, columns));
	}
	
	@Override
	public ImmutableList<E> row(int row)
	{
		return this.matrix.row(row).view();
	}
	
	@Override
	public ImmutableList<E> column(int column)
	{
		return this.matrix.column(column).view();
	}
	
	@Override
	public ImmutableList<E> flatten()
	{
		return this.matrix.flatten().view();
	}
	
	@Override
	public ImmutableMatrix<E> transposed()
	{
		return new MatrixView(this.matrix.transposed());
	}
	
	@Override
	public <R> ImmutableMatrix<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new MatrixView(this.matrix.mapped(mapper));
	}
	
	@Override
	public int rowOf(Object element)
	{
		return this.matrix.rowOf(element);
	}
	
	@Override
	public int columnOf(Object element)
	{
		return this.matrix.columnOf(element);
	}
	
	@Override
	public Tuple.Of2<Integer, Integer> cellOf(Object element)
	{
		return this.matrix.cellOf(element);
	}
	
	@Override
	public void rowArray(int row, Object[] store)
	{
		this.matrix.rowArray(row, store);
	}
	
	@Override
	public void columnArray(int column, Object[] store)
	{
		this.matrix.columnArray(column, store);
	}
	
	@Override
	public void toArray(Object[][] store)
	{
		this.matrix.toArray(store);
	}
	
	@Override
	public void toCellArray(Object[] store)
	{
		this.matrix.toCellArray(store);
	}
	
	@Override
	public ImmutableMatrix<E> copy()
	{
		return new MatrixView(this.matrix.copy());
	}
	
	@Override
	public MutableMatrix<E> mutable()
	{
		return this.matrix.mutable();
	}
	
	@Override
	public String toString()
	{
		return "view " + this.matrix.toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.matrix.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return this.matrix.hashCode();
	}
}
