package dyvil.collection;

import java.util.Objects;

import dyvil.tuple.Tuple3;

public interface Cell<R, C, V>
{
	/**
	 * Returns the row stored by this cell
	 * 
	 * @return the row
	 */
	public R getRow();
	
	/**
	 * Returns the column stored by this cell
	 * 
	 * @return the column
	 */
	public C getColumn();
	
	/**
	 * Returns the value stored by this cell
	 * 
	 * @return the value
	 */
	public V getValue();
	
	/**
	 * Converts this entry to a {@link Tuple3 Tuple}.
	 * 
	 * @return a tuple with this cell's row, column and value
	 */
	public default Tuple3<R, C, V> toTuple()
	{
		return new Tuple3<R, C, V>(this.getRow(), this.getColumn(), this.getValue());
	}
	
	public static String cellToString(Cell<?, ?, ?> cell)
	{
		return "(" + cell.getRow() + ", " + cell.getColumn() + ") -> " + cell.getValue();
	}
	
	public static boolean cellEquals(Cell<?, ?, ?> cell, Object obj)
	{
		if (!(obj instanceof Cell))
		{
			return false;
		}
		
		return cellEquals(cell, (Cell) obj);
	}
	
	public static boolean cellEquals(Cell<?, ?, ?> cell1, Cell<?, ?, ?> cell2)
	{
		return Objects.equals(cell1.getRow(), cell2.getRow()) && Objects.equals(cell1.getColumn(), cell2.getColumn())
				&& Objects.equals(cell1.getValue(), cell2.getValue());
	}
	
	public static int cellHashCode(Cell<?, ?, ?> cell)
	{
		Object row = cell.getRow();
		Object column = cell.getColumn();
		Object value = cell.getValue();
		int keyHash = (row == null ? 0 : row.hashCode() * 31) + (column == null ? 0 : column.hashCode());
		int hash = (keyHash * 31) + (value == null ? 0 : value.hashCode());
		return hash * 31 + hash;
	}
}
