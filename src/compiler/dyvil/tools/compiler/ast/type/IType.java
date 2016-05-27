package dyvil.tools.compiler.ast.type;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IMemberContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.ResolvedTypeDelegate;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.*;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.ast.type.raw.PackageType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IType extends IASTNode, IMemberContext, ITypeContext
{
	enum TypePosition
	{
		/**
		 * Only allows Class Types.
		 */
		CLASS,
		/**
		 * Allows Class Types and Parameterized Types, but the latter cannot involve any Wildcard Types.
		 */
		SUPER_TYPE,
		/**
		 * Allows Class Types as well as Parameterized Types.
		 */
		TYPE,
		/**
		 * The type arguments of Parameterized Types used as SUPER_TYPE. Can be Class Types, Parameterized Types and
		 * Type Variable Types.
		 */
		SUPER_TYPE_ARGUMENT,
		/**
		 * Allows Class Types, Parameterized Types and Type Variable Types, but the latter cannot be contravariant.
		 */
		RETURN_TYPE,
		/**
		 * Allows Class Types, Parameterized Types and Type Variable Types, but the latter cannot be covariant.
		 */
		PARAMETER_TYPE,
		/**
		 * Allows all Types.
		 */
		GENERIC_ARGUMENT
	}

	// Basic Types
	int UNKNOWN   = 0;
	int NULL      = 1;
	int ANY       = 2;
	int DYNAMIC   = 3;
	int PRIMITIVE = 4;

	// Class Types
	int CLASS    = 16;
	int NAMED    = 17;
	int INTERNAL = 18;
	int PACKAGE  = 19;

	// Generic Types
	int GENERIC          = 24;
	int GENERIC_NAMED    = 25;
	int GENERIC_INTERNAL = 26;

	// Compound Types
	int TUPLE  = 32;
	int LAMBDA = 33;

	int ARRAY = 34;
	int LIST  = 35;
	int MAP   = 37;

	int OPTIONAL  = 48;
	int REFERENCE = 50;

	int UNION = 51;
	// int INTERSECTION = 52;

	// Type Variable Types
	int TYPE_VAR_TYPE     = 64;
	int INTERNAL_TYPE_VAR = 65;

	int WILDCARD_TYPE = 80;

	// Other Types
	int ANNOTATED = 192;

	int MISSING_TAG = 255;

	@Override
	default ICodePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(ICodePosition position)
	{
	}

	default IType atPosition(ICodePosition position)
	{
		return new ResolvedTypeDelegate(position, this);
	}

	int typeTag();

	boolean isPrimitive();

	int getTypecode();

	boolean isGenericType();

	ITypeParameter getTypeVariable();

	Name getName();

	// Container Class

	IClass getTheClass();

	// Type Conversions

	IType getObjectType();

	default IType asReturnType()
	{
		return this;
	}

	default IType asParameterType()
	{
		return this.getConcreteType(ITypeContext.COVARIANT);
	}

	String getTypePrefix();

	IClass getRefClass();

	default IType getRefType()
	{
		return new ReferenceType(this.getRefClass(), this);
	}

	IType getSimpleRefType();

	// Arrays

	boolean isArrayType();

	int getArrayDimensions();

	IType getElementType();

	IClass getArrayClass();

	default Mutability getMutability()
	{
		if (this.getAnnotation(Types.IMMUTABLE_CLASS) != null)
		{
			return Mutability.IMMUTABLE;
		}
		return Mutability.UNDEFINED;
	}

	// Lambda Types

	boolean isExtension();

	void setExtension(boolean extension);

	// Super Type

	default int getSuperTypeDistance(IType superType)
	{
		if (this == superType)
		{
			return 1;
		}

		final IClass thisClass = this.getTheClass();
		return thisClass == null ? 0 : thisClass.getSuperTypeDistance(superType);
	}

	/**
	 * Returns {@code true} iff this type is a super type of the given {@code type}, {@code false otherwise}.
	 *
	 * @param subType
	 * 	the potential sub-type of this type
	 *
	 * @return {@code true} iff this type is a super type of the given type, {@code false} otherwise
	 */
	default boolean isSuperTypeOf(IType subType)
	{
		return this == subType || Types.isSuperClass(this, subType);
	}

	default boolean isSuperClassOf(IType subType)
	{
		final IClass superClass = this.getTheClass();
		if (superClass == null)
		{
			return false;
		}
		if (superClass == Types.OBJECT_CLASS)
		{
			return true;
		}

		final IClass subClass = subType.getTheClass();
		return subClass != null && (subClass == superClass || subClass.isSubClassOf(this));
	}

	boolean isSameType(IType type);

	boolean isSameClass(IType type);

	default boolean isConvertibleFrom(IType type)
	{
		return false;
	}

	default boolean isConvertibleTo(IType type)
	{
		return false;
	}

	/**
	 * Returns true if this type has a special role within the type system and needs special subtyping checks.
	 */
	default int subTypeCheckLevel()
	{
		return 0;
	}

	default boolean isSubClassOf(IType superType)
	{
		throw new Error();
	}

	default boolean isSubTypeOf(IType superType)
	{
		throw new Error();
	}

	// Resolve

	IMethod getBoxMethod();

	IMethod getUnboxMethod();

	default IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isResolved())
		{
			return value;
		}
		return value.withType(this, typeContext, markers, context);
	}

	default IValue convertValueTo(IValue value, IType targetType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return null;
	}

	// Generics

	/**
	 * Returns the type argument in this generic type for the given type variable.
	 * <p/>
	 * Example:<br>
	 * <p/>
	 * <pre>
	 * GenericType gt = type[List[String]]
	 * ITypeParameter tv = type[List].getTypeVariable("E")
	 * gt.resolveType(tv) // => String
	 * </pre>
	 */
	@Override
	IType resolveType(ITypeParameter typeParameter);

	/**
	 * Returns true if this is or contains any type variables.
	 */
	boolean hasTypeVariables();

	boolean isUninferred();

	IType getConcreteType(ITypeContext context);

	void inferTypes(IType concrete, ITypeContext typeContext);

	// Phases

	boolean isResolved();

	IType resolveType(MarkerList markers, IContext context);

	default void resolve(MarkerList markers, IContext context)
	{
	}

	void checkType(MarkerList markers, IContext context, TypePosition position);

	void check(MarkerList markers, IContext context);

	void foldConstants();

	void cleanup(IContext context, IClassCompilableList compilableList);

	// IContext

	default IAnnotation getAnnotation(IClass type)
	{
		final IClass theClass = this.getTheClass();
		return theClass == null ? null : theClass.getAnnotation(type);
	}

	@Override
	default Package resolvePackage(Name name)
	{
		return null;
	}

	@Override
	default IClass resolveClass(Name name)
	{
		final IClass theClass = this.getTheClass();
		return theClass == null ? null : theClass.resolveClass(name);
	}

	@Override
	default ITypeParameter resolveTypeParameter(Name name)
	{
		final IClass theClass = this.getTheClass();
		return theClass == null ? null : theClass.resolveTypeParameter(name);
	}

	@Override
	IDataMember resolveField(Name name);

	@Override
	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);

	@Override
	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);

	IMethod getFunctionalMethod();

	// Compilation

	String getInternalName();

	default String getExtendedName()
	{
		StringBuilder buffer = new StringBuilder();
		this.appendExtendedName(buffer);
		return buffer.toString();
	}

	void appendExtendedName(StringBuilder buffer);

	String getSignature();

	static String getSignature(IType type)
	{
		final StringBuilder builder = new StringBuilder();
		type.appendSignature(builder);
		return builder.toString();
	}

	void appendSignature(StringBuilder buffer);

	int getLoadOpcode();

	int getArrayLoadOpcode();

	int getStoreOpcode();

	int getArrayStoreOpcode();

	int getReturnOpcode();

	Object getFrameType();

	int getLocalSlots();

	void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException;

	void writeClassExpression(MethodWriter writer) throws BytecodeException;

	void writeTypeExpression(MethodWriter writer) throws BytecodeException;

	void writeDefaultValue(MethodWriter writer) throws BytecodeException;

	static IType withAnnotation(IType type, IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath == null || step >= steps)
		{
			final IType customType = type.withAnnotation(annotation);
			if (customType != null)
			{
				return customType;
			}

			return new AnnotatedType(type, annotation);
		}

		type.addAnnotation(annotation, typePath, step, steps);
		return type;
	}

	default IType withAnnotation(IAnnotation annotation)
	{
		return null;
	}

	void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps);

	void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath);

	IConstantValue getDefaultValue();

	static void writeType(IType type, DataOutput dos) throws IOException
	{
		if (type == null)
		{
			dos.writeByte(MISSING_TAG);
			return;
		}

		dos.writeByte(type.typeTag());
		type.write(dos);
	}

	static IType readType(DataInput dis) throws IOException
	{
		int tag = dis.readUnsignedByte();
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
		case PACKAGE:
			type = new PackageType();
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
		case LIST:
			type = new ListType();
			break;
		case MAP:
			type = new MapType();
			break;
		case OPTIONAL:
			type = new OptionType();
			break;
		case REFERENCE:
			type = new ReferenceType();
			break;
		case ANNOTATED:
			type = new AnnotatedType();
			break;
		case TYPE_VAR_TYPE:
			type = new NamedType();
			break;
		case WILDCARD_TYPE:
			type = new WildcardType();
			break;
		case MISSING_TAG:
			return null;
		default:
			throw new Error("Cannot decode TypeTag " + tag);
		}

		type.read(dis);
		return type;
	}

	void write(DataOutput out) throws IOException;

	void read(DataInput in) throws IOException;

	@Override
	void toString(String prefix, StringBuilder buffer);

	// Misc

	IType clone();

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	static boolean equals(IType type, Object obj)
	{
		return type.isSameClass((IType) obj);
	}
}
