package dyvilx.tools.compiler.ast.type;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IMemberContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.IConstantValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.reference.ReferenceType;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.ResolvedTypeDelegate;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.*;
import dyvilx.tools.compiler.ast.type.generic.ClassGenericType;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.ast.type.raw.NamedType;
import dyvilx.tools.compiler.ast.type.raw.PackageType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IType extends ASTNode, IMemberContext, ITypeContext
{
	class TypePosition
	{
		public static final int CLASS_FLAG            = 0b00000001;
		public static final int GENERIC_FLAG          = 0b00000010;
		public static final int TYPE_VAR_FLAG         = 0b00000100;
		public static final int NO_CONTRAVARIANT_FLAG = 0b00001000;
		public static final int NO_COVARIANT_FLAG     = 0b00010000;
		public static final int WILDCARD_FLAG         = 0b00100000;
		public static final int SUPERTYPE_FLAG        = 0b01000000;
		public static final int REIFY_FLAG            = 0b10000000;

		/**
		 * The parameter type of a {@code class<T>} expression. Allows class types as well as reified type variable
		 * references.
		 */
		public static final int CLASS               = CLASS_FLAG;
		/**
		 * The parameter type of a {@code type<T>} expression. Allows class types and parametric types, as well as
		 * type-reified type variable references.
		 */
		public static final int TYPE                = CLASS_FLAG | GENERIC_FLAG;
		/**
		 * A super type or interface of a {@code class}. Allows class types and parametric types, but the latter must
		 * not have any wildcard types.
		 */
		public static final int SUPER_TYPE          = CLASS_FLAG | GENERIC_FLAG | SUPERTYPE_FLAG;
		/**
		 * The type arguments of parametric super types. Allows class types, parametric types and type variable
		 * references.
		 */
		public static final int SUPER_TYPE_ARGUMENT = CLASS_FLAG | GENERIC_FLAG | TYPE_VAR_FLAG;
		/**
		 * The return type of a method, field or property. Allows class types, parametric types and type variable
		 * references, but the latter must not be contravariant.
		 */
		public static final int RETURN_TYPE         = CLASS_FLAG | GENERIC_FLAG | TYPE_VAR_FLAG | NO_CONTRAVARIANT_FLAG;
		/**
		 * The parameter type of a method. Allows class types, parametric types and type variable references, but the
		 * latter must not be covariant.
		 */
		public static final int PARAMETER_TYPE      = CLASS_FLAG | GENERIC_FLAG | TYPE_VAR_FLAG | NO_COVARIANT_FLAG;
		/**
		 * The argument type of a parametric type. Allows class types, parametric types, type variable references and
		 * wildcard types.
		 */
		public static final int GENERIC_ARGUMENT    = CLASS_FLAG | GENERIC_FLAG | TYPE_VAR_FLAG | WILDCARD_FLAG;

		/**
		 * Allows all Types, including
		 */

		public static int genericArgument(int position)
		{
			return copyReify(position, (position & SUPERTYPE_FLAG) != 0 ? SUPER_TYPE_ARGUMENT : GENERIC_ARGUMENT);
		}

		public static int copyReify(int from, int to)
		{
			return to | from & REIFY_FLAG;
		}
	}

	// Basic Types
	int UNKNOWN   = 0;
	int NULL      = 1;
	int ANY       = 2;
	int PRIMITIVE = 4;
	int NONE      = 5;

	// Class Types
	int CLASS    = 16;
	int NAMED    = 17; // no deserialization
	int INTERNAL = 18; // no deserialization
	int PACKAGE  = 19;

	// Generic Types
	int GENERIC          = 24;
	int GENERIC_NAMED    = 25; // no deserialization
	int GENERIC_INTERNAL = 26; // no deserialization
	int INFIX_CHAIN      = 27; // no deserialization

	// Compound Types
	int TUPLE  = 32;
	int LAMBDA = 33;

	int ARRAY = 34;
	int MAP   = 37;

	int OPTIONAL          = 48;
	int IMPLICIT_OPTIONAL = 49;
	int REFERENCE         = 50;

	int UNION        = 51; // no deserialization
	int INTERSECTION = 52; // no deserialization

	// Type Variable Types
	int TYPE_VAR          = 64;
	int INTERNAL_TYPE_VAR = 65; // no deserialization

	int WILDCARD_TYPE = 80;

	// Other Types
	int ANNOTATED = 192;

	int MISSING_TAG = 255;

	@Override
	default SourcePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(SourcePosition position)
	{
	}

	default IType atPosition(SourcePosition position)
	{
		return new ResolvedTypeDelegate(position, this);
	}

	int typeTag();

	boolean isPrimitive();

	int getTypecode();

	boolean isGenericType();

	default boolean needsSignature()
	{
		return this.isGenericType() || this.hasTypeVariables();
	}

	Name getName();

	// Container Class

	IClass getTheClass();

	// Type Conversions

	IType getObjectType();

	default IType asParameterType()
	{
		return this.getConcreteType(ITypeContext.COVARIANT);
	}

	String getTypePrefix();

	IClass getRefClass();

	IType getSimpleRefType();

	default boolean hasTag(int tag)
	{
		return this.typeTag() == tag;
	}

	default boolean canExtract(Class<? extends IType> type)
	{
		return type.isInstance(this);
	}

	default <T extends IType> T extract(Class<T> type)
	{
		return this.canExtract(type) ? (T) this : null;
	}

	IClass getArrayClass();

	default Mutability getMutability()
	{
		if (this.getAnnotation(Types.IMMUTABLE_CLASS) != null)
		{
			return Mutability.IMMUTABLE;
		}
		return Mutability.UNDEFINED;
	}

	IMethod getBoxMethod();

	IMethod getUnboxMethod();

	// Nullability

	default boolean useNonNullAnnotation()
	{
		return true;
	}

	// Subtyping

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
		final IClass thisClass = this.getTheClass();
		if (thisClass == null)
		{
			return false;
		}
		if (thisClass == Types.OBJECT_CLASS)
		{
			return !subType.isPrimitive();
		}

		final IClass subClass = subType.getTheClass();
		return subClass != null && (subClass == thisClass || subClass.isSubClassOf(this));
	}

	boolean isSameType(IType type);

	boolean isSameClass(IType type);

	int SUBTYPE_BASE               = 0;
	int SUBTYPE_NULLABLE           = 1;
	int SUBTYPE_NULL               = 2;
	int SUBTYPE_TYPEVAR            = 3;
	int SUBTYPE_COVARIANT_TYPEVAR  = 4;
	int SUBTYPE_UNION_INTERSECTION = 5;
	int SUBTYPE_NONE               = 6;

	default int subTypeCheckLevel()
	{
		return SUBTYPE_BASE;
	}

	default boolean isSubClassOf(IType superType)
	{
		throw new UnsupportedOperationException(this.getClass().getName() + ".isSubClassOf");
	}

	default boolean isSubTypeOf(IType superType)
	{
		throw new UnsupportedOperationException(this.getClass().getName() + ".isSubTypeOf");
	}

	// Conversion

	default boolean isConvertibleFrom(IType type)
	{
		return false;
	}

	default boolean isConvertibleTo(IType type)
	{
		return false;
	}

	/**
	 * Provides an expression that has this type and is a conversion of sorts from the given value.
	 * <p>
	 * Should return null iff !this.isConvertibleFrom(type)
	 *
	 * @param value
	 * 	the value to convert
	 * @param type
	 * 	the type of the value
	 * @param typeContext
	 * 	the type conversion context
	 * @param markers
	 * 	the list of markers for diagnostics
	 * @param context
	 * 	the resolution context
	 *
	 * @return a conversion expression
	 */
	default IValue convertFrom(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return null;
	}

	/**
	 * Provides an expression that has the given type and is a conversion of sorts from the given value. Here it can be
	 * assumed that value.getType() == this
	 * <p>
	 * Should return iff !this.isConvertibleTo(type)
	 *
	 * @param value
	 * 	the value to convert
	 * @param type
	 * 	the type to convert to
	 * @param typeContext
	 * 	the type conversion context
	 * @param markers
	 * 	the list of markers for diagnostics
	 * @param context
	 * 	the resolution context
	 *
	 * @return a conversion expression
	 */
	default IValue convertTo(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
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

	void checkType(MarkerList markers, IContext context, int position);

	void check(MarkerList markers, IContext context);

	void foldConstants();

	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	// IContext

	default Annotation getAnnotation(IClass type)
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
	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	@Override
	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	@Override
	void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments);

	IMethod getFunctionalMethod();

	// Compilation

	int NAME_DESCRIPTOR            = 1;
	int NAME_SIGNATURE             = 2;
	int NAME_SIGNATURE_GENERIC_ARG = 3;
	int NAME_FULL                  = 4;

	default int getDescriptorKind()
	{
		return this.needsSignature() ? NAME_SIGNATURE : NAME_DESCRIPTOR;
	}

	String getInternalName();

	default String getExtendedName()
	{
		StringBuilder buffer = new StringBuilder();
		this.appendExtendedName(buffer);
		return buffer.toString();
	}

	default void appendExtendedName(StringBuilder buffer)
	{
		this.appendDescriptor(buffer, NAME_DESCRIPTOR);
	}

	default String getSignature()
	{
		final StringBuilder builder = new StringBuilder();
		this.appendSignature(builder, false);
		return builder.toString();
	}

	default void appendSignature(StringBuilder buffer, boolean genericArg)
	{
		this.appendDescriptor(buffer, genericArg ? NAME_SIGNATURE_GENERIC_ARG : NAME_SIGNATURE);
	}

	default String getDescriptor(int type)
	{
		final StringBuilder builder = new StringBuilder();
		this.appendDescriptor(builder, type);
		return builder.toString();
	}

	void appendDescriptor(StringBuilder buffer, int type);

	int getLoadOpcode();

	int getArrayLoadOpcode();

	int getStoreOpcode();

	int getArrayStoreOpcode();

	int getReturnOpcode();

	Object getFrameType();

	int getLocalSlots();

	void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException;

	void writeClassExpression(MethodWriter writer, boolean wrapPrimitives) throws BytecodeException;

	void writeTypeExpression(MethodWriter writer) throws BytecodeException;

	default void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
	}

	default boolean hasDefaultValue()
	{
		return this.getDefaultValue() != null;
	}

	default IConstantValue getDefaultValue()
	{
		return null;
	}

	// Annotations

	static IType withAnnotation(IType type, Annotation annotation, TypePath typePath)
	{
		return typePath == null ?
			       withAnnotation(type, annotation) :
			       withAnnotation(type, annotation, typePath, 0, typePath.getLength());
	}

	static IType withAnnotation(IType type, Annotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath != null && step < steps)
		{
			type.addAnnotation(annotation, typePath, step, steps);
			return type;
		}

		return withAnnotation(type, annotation);
	}

	static IType withAnnotation(IType type, Annotation annotation)
	{
		switch (annotation.getTypeDescriptor())
		{
		case AnnotationUtil.NULLABLE_INTERNAL:
			if (type.useNonNullAnnotation())
			{
				return NullableType.apply(type);
			}
			break;
		}

		final IType customType = type.withAnnotation(annotation);
		return customType != null ? customType : new AnnotatedType(type, annotation);
	}

	default IType withAnnotation(Annotation annotation)
	{
		return null;
	}

	void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps);

	static void writeAnnotations(IType type, TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		if (type.useNonNullAnnotation())
		{
			visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.NOTNULL, false)
			       .visitEnd();
		}

		type.writeAnnotations(visitor, typeRef, typePath);
	}

	void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath);

	// General Compilation

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
		case NONE:
			return Types.NONE;
		case ANY:
			return Types.ANY;
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
		case MAP:
			type = new MapType();
			break;
		case OPTIONAL:
			type = new NullableType();
			break;
		case IMPLICIT_OPTIONAL:
			type = new ImplicitNullableType();
			break;
		case REFERENCE:
			type = new ReferenceType();
			break;
		case ANNOTATED:
			type = new AnnotatedType();
			break;
		case TYPE_VAR:
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

	// Misc

	@Override
	void toString(@NonNull String prefix, @NonNull StringBuilder buffer);

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	static boolean equals(IType type, Object obj)
	{
		return type.isSameClass((IType) obj);
	}
}
