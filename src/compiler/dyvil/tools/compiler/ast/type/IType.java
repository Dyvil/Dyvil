package dyvil.tools.compiler.ast.type;

import java.io.*;

import dyvil.collection.List;
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
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.generic.type.WildcardType;
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
	public static enum TypePosition
	{
		/**
		 * Only allows Class Types.
		 */
		CLASS,
		/**
		 * Allows Class Types and Parameterized Types, but the latter cannot
		 * involve any Wildcard Types.
		 */
		SUPER_TYPE,
		/**
		 * Allows Class Types as well as Parameterized Types.
		 */
		TYPE,
		/**
		 * The type arguments of Parameterized Types used as SUPER_TYPE. Can be
		 * Class Types, Parameterized Types and Type Variable Types.
		 */
		SUPER_TYPE_ARGUMENT,
		/**
		 * Allows Class Types, Parameterized Types and Type Variable Types, but
		 * the latter cannot be contravariant.
		 */
		RETURN_TYPE,
		/**
		 * Allows Class Types, Parameterized Types and Type Variable Types, but
		 * the latter cannot be covariant.
		 */
		PARAMETER_TYPE,
		/**
		 * Allows all Types.
		 */
		GENERIC_ARGUMENT;
	}
	
	int	UNKNOWN				= 0;
	int	NULL				= 1;
	int	ANY					= 2;
	int	DYNAMIC				= 3;
	int	PRIMITIVE			= 4;
	
	int	CLASS				= 8;
	int	NAMED				= 9;
	int	INTERNAL			= 10;
	int	GENERIC				= 11;
	int	GENERIC_NAMED		= 12;
	int	GENERIC_INTERNAL	= 13;
	
	int	TUPLE				= 16;
	int	LAMBDA				= 17;
	int	ARRAY				= 18;
	
	int	TYPE_VAR_TYPE		= 32;
	int	INTERNAL_TYPE_VAR	= 33;
	int	WILDCARD_TYPE		= 34;
	
	public int typeTag();
	
	public default boolean isPrimitive()
	{
		return false;
	}
	
	public default boolean isGenericType()
	{
		return false;
	}
	
	public Name getName();
	
	// Container Class
	
	public IClass getTheClass();
	
	public default IType getReferenceType()
	{
		return this;
	}
	
	public default IType getReturnType()
	{
		return this;
	}
	
	public default IType getParameterType()
	{
		return this;
	}
	
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
	
	public default float getSubTypeDistance(IType subtype)
	{
		return subtype.getTheClass().getSuperTypeDistance(this);
	}
	
	public default int getSubClassDistance(IType subtype)
	{
		return subtype.getTheClass().getSuperTypeDistance(this);
	}
	
	public default IType getSuperType()
	{
		IClass iclass = this.getTheClass();
		if (iclass != null)
		{
			return iclass.getSuperType();
		}
		return Types.OBJECT;
	}
	
	public default IType combine(IType type)
	{
		return this;
	}
	
	/**
	 * Returns true if {@code type} is a subtype of this type
	 * 
	 * @param type
	 * @return
	 */
	public default boolean isSuperTypeOf(IType type)
	{
		if (this == type)
		{
			return true;
		}
		
		IClass thisClass = this.getTheClass();
		if (thisClass == Types.OBJECT_CLASS)
		{
			return true;
		}
		if (type.typeTag() == WILDCARD_TYPE)
		{
			return type.equals(this);
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
	
	public default boolean isSuperClassOf(IType type)
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
	
	public default IMethod getBoxMethod()
	{
		return null;
	}
	
	public default IMethod getUnboxMethod()
	{
		return null;
	}
	
	public default IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return value.withType(this.getConcreteType(typeContext), typeContext, markers, context);
	}
	
	// Generics
	
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
		return Types.UNKNOWN;
	}
	
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
	
	public default void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}
	
	// Resolve Types
	
	public boolean isResolved();
	
	public IType resolve(MarkerList markers, IContext context, TypePosition position);
	
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
	public default IType resolveType(Name name)
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
	public default byte getVisibility(IClassMember member)
	{
		return 0;
	}
	
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
	
	public static void writeType(IType type, DataOutput dos) throws IOException
	{
		dos.writeByte(type.typeTag());
		type.write(dos);
	}
	
	public static IType readType(DataInput dis) throws IOException
	{
		byte tag = dis.readByte();
		IType type;
		switch (tag)
		{
		case UNKNOWN:
			return Types.UNKNOWN;
		case NULL:
			return Types.NULL;
		case ANY:
			return Types.ANY;
		case DYNAMIC:
			return Types.DYNAMIC;
		case PRIMITIVE:
			return PrimitiveType.fromTypecode(dis.readByte());
		case CLASS:
			type = new ClassType();
			break;
		case GENERIC:
			type = new ClassGenericType();
			break;
		case TUPLE:
			type = new TupleType();
			break;
		case LAMBDA:
			type = new LambdaType();
			break;
		case ARRAY:
			type = new ArrayType();
			break;
		case TYPE_VAR_TYPE:
			type = new TypeVarType();
			break;
		case WILDCARD_TYPE:
			type = new WildcardType();
			break;
		default:
			return null;
		}
		
		type.read(dis);
		return type;
	}
	
	public void write(DataOutput out) throws IOException;
	
	public void read(DataInput in) throws IOException;
	
	@Override
	public void toString(String prefix, StringBuilder buffer);
	
	// Misc
	
	public IType clone();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
	public static boolean equals(IType type, Object obj)
	{
		return type.classEquals((IType) obj);
	}
}
