package dyvil.tools.compiler.ast.structure;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;

public interface IContext
{
	public static final byte	INVISIBLE			= 0;
	public static final byte	READ_ACCESS			= 1;
	public static final byte	WRITE_ACCESS		= 2;
	public static final byte	READ_WRITE_ACCESS	= 3;
	public static final byte	STATIC				= 4;
	public static final byte	SEALED				= 5;
	
	public default boolean isStatic()
	{
		return true;
	}
	
	public default int getVariableCount()
	{
		return 0;
	}
	
	/**
	 * Returns the type of this context. {@code null} in case of a package or
	 * compilation unit, this in case of a Class and the class this is contained
	 * in in case of a method.
	 * 
	 * @return the type of this context
	 */
	public IType getThisType();
	
	public Package resolvePackage(String name);
	
	public IClass resolveClass(String name);
	
	public FieldMatch resolveField(String name);
	
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments);
	
	public MethodMatch resolveConstructor(List<IValue> arguments);
	
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments);
	
	public byte getAccessibility(IMember member);
}
