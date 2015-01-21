package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IType extends IASTNode, INamed, IContext
{
	// Full Name
	
	public void setFullName(String name);
	
	public String getFullName();
	
	// Container Class
	
	public void setClass(IClass theClass);
	
	public IClass getTheClass();
	
	// Arrays
	
	public void setArrayDimensions(int dimensions);
	
	public int getArrayDimensions();
	
	public void addArrayDimension();
	
	public boolean isArrayType();
	
	// Super Type
	
	public IType getSuperType();
	
	public default boolean isAssignableFrom(IType type)
	{
		if (this.getArrayDimensions() != type.getArrayDimensions())
		{
			return false;
		}
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			return iclass == this.getTheClass() || iclass.isSuperType(this);
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
	
	public IType resolve(IContext context);
	
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
		writer.visitInsn(Opcodes.ACONST_NULL, this);
	}
	
	// Misc
	
	public IType clone();
}
