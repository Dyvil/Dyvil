package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IType extends IASTNode, INamed, IContext
{
	public default boolean isPrimitive()
	{
		return false;
	}
	
	// Full Name
	
	public void setFullName(String name);
	
	public String getFullName();
	
	// Container Class
	
	public void setClass(IClass theClass);
	
	public IClass getTheClass();
	
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
		if (arrayDimensions > 0 && thisClass == Type.OBJECT_CLASS)
		{
			return arrayDimensions > this.getArrayDimensions();
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
	
	public default boolean classEquals(IType type)
	{
		IClass class1 = this.getTheClass();
		IClass class2 = type.getTheClass();
		if (class1 == class2)
		{
			return true;
		}
		if (class1 == null)
		{
			return false;
		}
		return class1.equals(class2);
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
