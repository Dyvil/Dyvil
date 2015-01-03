package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;

public interface IContext
{
	public static final int	INVISIBLE			= 0;
	public static final int	READ_ACCESS			= 1;
	public static final int	WRITE_ACCESS		= 2;
	public static final int	READ_WRITE_ACCESS	= 3;
	public static final int	STATIC				= 4;
	
	public boolean isStatic();
	
	/**
	 * Returns the type of this context. {@code null} in case of a package or
	 * compilation unit, this in case of a Class and the class this is contained
	 * in in case of a method.
	 * 
	 * @return the type of this context
	 */
	public IType getThisType();
	
	public IClass resolveClass(String name);
	
	public FieldMatch resolveField(IContext context, String name);
	
	public MethodMatch resolveMethod(IContext context, String name, IType... argumentTypes);
	
	public byte getAccessibility(IMember member);
}
