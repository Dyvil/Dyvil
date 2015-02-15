package dyvil.tools.compiler.ast.constant;

public interface INumericValue extends IConstantValue
{
	@Override
	public default boolean isPrimitive()
	{
		return true;
	}
	
	public int intValue();
	
	public long longValue();
	
	public float floatValue();
	
	public double doubleValue();
}
