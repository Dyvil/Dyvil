package dyvil.tools.compiler.ast.type;

import java.util.List;
import java.util.Map;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IType extends IASTNode, INamed, IContext
{
	public default boolean isPrimitive()
	{
		return false;
	}
	
	public default IValue box(IValue value)
	{
		return null;
	}
	
	public default IValue unbox(IValue value)
	{
		return null;
	}
	
	// Full Name
	
	public void setFullName(String name);
	
	public String getFullName();
	
	// Container Class
	
	public void setClass(IClass theClass);
	
	public IClass getTheClass();
	
	// Generics
	
	/**
	 * Returns true if this is an instance of {@link GenericType}
	 * 
	 * @return
	 */
	public boolean isGeneric();
	
	public default void addTypeVariables(IType type, Map<String, IType> typeVariables)
	{
	}
	
	/**
	 * Returns true if this is or contains any type variables.
	 * 
	 * @return
	 */
	public boolean hasTypeVariables();
	
	/**
	 * Returns a copy of this type with all type variables replaced.
	 * 
	 * @param typeVariables
	 *            the type variables
	 * @return
	 */
	public IType getConcreteType(Map<String, IType> typeVariables);
	
	// Arrays
	
	public void setArrayDimensions(int dimensions);
	
	public int getArrayDimensions();
	
	public default IType getElementType()
	{
		IType type1 = this.clone();
		type1.removeArrayDimension();
		return type1;
	}
	
	public default IType getArrayType()
	{
		IType type1 = this.clone();
		type1.addArrayDimension();
		return type1;
	}
	
	public default IType getArrayType(int dimensions)
	{
		IType type1 = this.clone();
		type1.setArrayDimensions(dimensions);
		return type1;
	}
	
	public default void addArrayDimension()
	{
	}
	
	public default void removeArrayDimension()
	{
	}
	
	public boolean isArrayType();
	
	// Super Type
	
	public IType getSuperType();
	
	/**
	 * Returns true if {@code type} is a subtype of this type
	 * 
	 * @param type
	 * @return
	 */
	public default boolean isSuperTypeOf(IType type)
	{
		IClass thisClass = this.getTheClass();
		IClass thatClass = type.getTheClass();
		int arrayDimensions = type.getArrayDimensions();
		if (thisClass == Type.OBJECT_CLASS)
		{
			if (arrayDimensions > 0)
			{
				return arrayDimensions > this.getArrayDimensions();
			}
			return true;
		}
		if (arrayDimensions != this.getArrayDimensions())
		{
			return false;
		}
		if (thatClass != null)
		{
			return thatClass == thisClass || thatClass.isSubTypeOf(this);
		}
		return false;
	}
	
	public default boolean isSuperTypeOf2(IType type)
	{
		IClass thisClass = this.getTheClass();
		IClass thatClass = type.getTheClass();
		if (thatClass != null)
		{
			return thatClass == thisClass || thatClass.isSubTypeOf(this);
		}
		return false;
	}
	
	public default boolean equals(IType type)
	{
		if (this.getArrayDimensions() != type.getArrayDimensions())
		{
			return false;
		}
		return this.getTheClass() == type.getTheClass();
	}
	
	public default boolean classEquals(IType type)
	{
		return this.getTheClass() == type.getTheClass();
	}
	
	// Resolve
	
	public IType resolve(List<Marker> markers, IContext context);
	
	public boolean isResolved();
	
	// IContext
	
	@Override
	public default IType getThisType()
	{
		return this;
	}
	
	@Override
	public default boolean isStatic()
	{
		return false;
	}
	
	// Compilation
	
	public String getInternalName();
	
	public default String getExtendedName()
	{
		StringBuilder buffer = new StringBuilder();
		this.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	public void appendExtendedName(StringBuilder buffer);
	
	public default String getSignature()
	{
		return null;
	}
	
	public void appendSignature(StringBuilder buffer);
	
	public default Object getFrameType()
	{
		StringBuilder buffer = new StringBuilder();
		this.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	// Compilation
	
	public int getLoadOpcode();
	
	public int getArrayLoadOpcode();
	
	public int getStoreOpcode();
	
	public int getArrayStoreOpcode();
	
	public int getReturnOpcode();
	
	public default void writeDefaultValue(MethodWriter writer)
	{
		writer.visitInsn(Opcodes.ACONST_NULL);
	}
	
	// Misc
	
	public IType clone();
}
