package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constant.NullValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IType extends IASTNode, INamed, IContext, ITypeContext
{
	public default boolean isPrimitive()
	{
		return false;
	}
	
	public default boolean isGenericType()
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
	
	@Override
	public void setName(Name name);
	
	@Override
	public Name getName();
	
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
		if (thisClass == Types.OBJECT_CLASS)
		{
			if (arrayDimensions > 0)
			{
				return arrayDimensions >= this.getArrayDimensions();
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
	
	public boolean isResolved();
	
	public IType resolve(MarkerList markers, IContext context);
	
	// Generics
	
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
	public IType getConcreteType(ITypeContext context);
	
	@Override
	public default IType resolveType(ITypeVariable typeVar)
	{
		return null;
	}
	
	public default IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		return null;
	}
	
	// IContext
	
	@Override
	public default boolean isStatic()
	{
		return true;
	}
	
	@Override
	public default IClass getThisClass()
	{
		return this.getTheClass();
	}
	
	@Override
	public Package resolvePackage(Name name);
	
	@Override
	public IClass resolveClass(Name name);
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name);
	
	@Override
	public IField resolveField(Name name);
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	@Override
	public byte getAccessibility(IMember member);
	
	public IMethod getFunctionalMethod();
	
	// Compilation
	
	public void setInternalName(String name);
	
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
	
	// Compilation
	
	public default int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	public default int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	public default int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	public default int getArrayStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	public default int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	public default void writeDefaultValue(MethodWriter writer)
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	public default IConstantValue getDefaultValue()
	{
		return NullValue.getNull();
	}
	
	// Misc
	
	public IType clone();
}
