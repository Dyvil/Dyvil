package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IStaticContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.generic.type.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IType extends IASTNode, IStaticContext, ITypeContext
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
		RETURN_TYPE, /**
						 * Allows Class Types, Parameterized Types and Type
						 * Variable Types, but the latter cannot be covariant.
						 */
		PARAMETER_TYPE, /**
						 * Allows all Types.
						 */
		GENERIC_ARGUMENT;
	}
	
	int	UNKNOWN		= 0;
	int	NULL		= 1;
	int	ANY			= 2;
	int	DYNAMIC		= 3;
	int	PRIMITIVE	= 4;
	
	int	CLASS				= 8;
	int	NAMED				= 9;
	int	INTERNAL			= 10;
	int	GENERIC				= 11;
	int	GENERIC_NAMED		= 12;
	int	GENERIC_INTERNAL	= 13;
	
	int	TUPLE	= 16;
	int	LAMBDA	= 17;
	int	ARRAY	= 18;
	int	MAP		= 19;
	
	int	TYPE_VAR_TYPE		= 32;
	int	INTERNAL_TYPE_VAR	= 33;
	int	WILDCARD_TYPE		= 34;
	
	int ANNOTATED = 64;
	
	@Override
	public default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public default void setPosition(ICodePosition position)
	{
	}
	
	public int typeTag();
	
	public boolean isPrimitive();
	
	public int getTypecode();
	
	public boolean isGenericType();
	
	public Name getName();
	
	// Container Class
	
	public IClass getTheClass();
	
	// Type Conversions
	
	public IType getObjectType();
	
	public default IType getReturnType()
	{
		return this;
	}
	
	public default IType getParameterType()
	{
		return this;
	}
	
	public ReferenceType getRefType();
	
	public IType getSimpleRefType();
	
	// Arrays
	
	public boolean isArrayType();
	
	public int getArrayDimensions();
	
	public IType getElementType();
	
	public IClass getArrayClass();
	
	// Super Type
	
	public default int getSuperTypeDistance(IType superType)
	{
		return this.getTheClass().getSuperTypeDistance(superType);
	}
	
	public default float getSubTypeDistance(IType subtype)
	{
		return subtype.getSuperTypeDistance(this);
	}
	
	public default int getSubClassDistance(IType subtype)
	{
		return subtype.getSuperTypeDistance(this);
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
	
	public boolean classEquals(IType type);
	
	// Resolve
	
	public IMethod getBoxMethod();
	
	public IMethod getUnboxMethod();
	
	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.hasTypeVariables())
		{
			type = type.getConcreteType(typeContext);
		}
		return type.convertValue(value, typeContext, markers, context);
	}
	
	@Deprecated
	public default IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return value.withType(this, typeContext, markers, context);
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
	public IType resolveType(ITypeVariable typeVar);
	
	public default IType resolveTypeSafely(ITypeVariable typeVar)
	{
		IType t = this.resolveType(typeVar);
		return t == null ? Types.ANY : t;
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
	public IType getConcreteType(ITypeContext context);
	
	public void inferTypes(IType concrete, ITypeContext typeContext);
	
	// Phases
	
	public boolean isResolved();
	
	public IType resolveType(MarkerList markers, IContext context);
	
	public default void resolve(MarkerList markers, IContext context)
	{
	}
	
	public void checkType(MarkerList markers, IContext context, TypePosition position);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	// IContext
	
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
	public IDataMember resolveField(Name name);
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
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
	
	public String getSignature();
	
	public void appendSignature(StringBuilder buffer);
	
	// Compilation
	
	public int getLoadOpcode();
	
	public int getArrayLoadOpcode();
	
	public int getStoreOpcode();
	
	public int getArrayStoreOpcode();
	
	public int getReturnOpcode();
	
	public Object getFrameType();
	
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException;
	
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException;
	
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException;
	
	public static IType withAnnotation(IType type, IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath == null || step > steps)
		{
			return new AnnotatedType(type, annotation);
		}
		
		type.addAnnotation(annotation, typePath, step, steps);
		return type;
	}
	
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps);
	
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath);
	
	public IConstantValue getDefaultValue();
	
	public static void writeType(IType type, DataOutput dos) throws IOException
	{
		if (type == null)
		{
			dos.writeByte(-1);
			return;
		}
		
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
