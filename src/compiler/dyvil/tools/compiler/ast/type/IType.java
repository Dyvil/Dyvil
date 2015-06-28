package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constant.NullValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IType extends IASTNode, IContext, ITypeContext
{
	int	UNKNOWN				= -1;
	int	NULL				= 0;
	int	ANY					= 1;
	int	DYNAMIC				= 2;
	int	PRIMITIVE			= 3;
	int	ARRAY				= 4;
	int	MULTI_ARRAY			= 5;
	
	int	CLASS				= 8;
	int	NAMED				= 9;
	int	INTERNAL			= 10;
	
	int	TUPLE				= 16;
	int	LAMBDA				= 17;
	int	LAMBDA_TEMP			= 18;
	
	int	GENERIC				= 32;
	int	TYPE_VAR_TYPE		= 33;
	int	INTERNAL_TYPE_VAR	= 34;
	int	WILDCARD_TYPE		= 35;
	
	public int typeTag();
	
	public default boolean isPrimitive()
	{
		return false;
	}
	
	public default boolean isGenericType()
	{
		return false;
	}
	
	public default IType getReferenceType()
	{
		return this;
	}
	
	public default IMethod getBoxMethod()
	{
		return null;
	}
	
	public default IMethod getUnboxMethod()
	{
		return null;
	}
	
	public Name getName();
	
	// Container Class
	
	public IClass getTheClass();
	
	// Arrays
	
	public default boolean isArrayType()
	{
		return false;
	}
	
	public default int getArrayDimensions()
	{
		return 0;
	}
	
	public default IType getElementType()
	{
		return this;
	}
	
	public default IClass getArrayClass()
	{
		return Types.getObjectArray();
	}
	
	// Super Type
	
	public default IType getSuperType()
	{
		IClass iclass = this.getTheClass();
		if (iclass != null)
		{
			return iclass.getSuperType();
		}
		return Types.OBJECT;
	}
	
	/**
	 * Returns true if {@code type} is a subtype of this type
	 * 
	 * @param type
	 * @return
	 */
	public default boolean isSuperTypeOf(IType type)
	{
		IClass thisClass = this.getTheClass();
		if (thisClass == Types.OBJECT_CLASS)
		{
			return true;
		}
		if (type.isArrayType())
		{
			return false;
		}
		
		IClass thatClass = type.getTheClass();
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
	public default boolean hasTypeVariables()
	{
		return false;
	}
	
	/**
	 * Returns a copy of this type with all type variables replaced.
	 * 
	 * @param typeVariables
	 *            the type variables
	 * @return
	 */
	public default IType getConcreteType(ITypeContext context)
	{
		return this;
	}
	
	/**
	 * Returns the type argument in this generic type for the given type
	 * variable.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * GenericType gt = type[List[String]]
	 * ITypeVariable tv = type[List].getTypeVariable("E")
	 * gt.resolveType(tv) // => String
	 * </pre>
	 */
	@Override
	public default IType resolveType(ITypeVariable typeVar)
	{
		return null;
	}
	
	/**
	 * Returns the type argument in this generic type for the given type
	 * variable, where this is the template containing the type variable types
	 * and {@code concrete} contains the concrete types.
	 * <p>
	 * Example:<br>
	 * 
	 * <pre>
	 * GenericType gt1 = type[List].genericType // => List[E]
	 * GenericType gt2 = type[List[String]]
	 * ITypeVariable tv = type[List].getTypeVariable("E")
	 * gt1.resolveType(tv, gt2) // => String
	 * </pre>
	 */
	public default IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		return null;
	}
	
	public default void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}
	
	// IContext
	
	@Override
	public default boolean isStatic()
	{
		return true;
	}
	
	@Override
	public default IDyvilHeader getHeader()
	{
		return this.getTheClass().getHeader();
	}
	
	@Override
	public default IClass getThisClass()
	{
		return this.getTheClass();
	}
	
	@Override
	public default Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public default IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public default ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name);
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	@Override
	public byte getVisibility(IClassMember member);
	
	@Override
	public default boolean handleException(IType type)
	{
		return false;
	}
	
	public IMethod getFunctionalMethod();
	
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
		return Opcodes.AASTORE;
	}
	
	public default int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	public default Object getFrameType()
	{
		return this.getInternalName();
	}
	
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException;
	
	public default void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	public default IConstantValue getDefaultValue()
	{
		return NullValue.getNull();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer);
	
	// Misc
	
	public IType clone();
}
