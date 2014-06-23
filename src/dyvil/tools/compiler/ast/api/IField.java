package dyvil.tools.compiler.ast.api;

public interface IField extends ITyped
{
	public static final Object NULL = new Object();
	
	public void setValue(Object value);
	
	public Object getValue();
	
	public default boolean hasValue()
	{
		return this.getValue() != null;
	}
}
