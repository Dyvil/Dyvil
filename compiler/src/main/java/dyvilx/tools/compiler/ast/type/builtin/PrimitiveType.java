package dyvilx.tools.compiler.ast.type.builtin;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Type;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.DummyValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.*;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static dyvil.reflect.Opcodes.*;

public final class PrimitiveType implements IType
{
	// =============== Constants ===============

	// Duplicate of this mapping is present in dyvil.reflect.types.PrimitiveType
	public static final int VOID_CODE    = 0;
	public static final int BOOLEAN_CODE = 1;
	public static final int BYTE_CODE    = 2;
	public static final int SHORT_CODE   = 3;
	public static final int CHAR_CODE    = 4;
	public static final int INT_CODE     = 5;
	public static final int LONG_CODE    = 6;
	public static final int FLOAT_CODE   = 7;
	public static final int DOUBLE_CODE  = 8;

	// =============== Static Final Fields ===============

	// Code to generate the value of PROMOTION_BITS. Uncomment as needed.
	// @formatter:off
	/* insert / before /* to toggle
	private static final long PROMOTION_BITS;

	static
	{
		long promoBits = 0L;
		promoBits |= bitMask(BYTE_CODE, SHORT_CODE) | bitMask(BYTE_CODE, CHAR_CODE) | bitMask(BYTE_CODE, INT_CODE);
		promoBits |= bitMask(SHORT_CODE, CHAR_CODE) | bitMask(SHORT_CODE, INT_CODE);
		promoBits |= bitMask(CHAR_CODE, INT_CODE);
		// Integer types can be promoted to long, float and double
		for (int i = BYTE_CODE; i <= INT_CODE; i++)
		{
			promoBits |= bitMask(i, LONG_CODE);
			promoBits |= bitMask(i, FLOAT_CODE);
			promoBits |= bitMask(i, DOUBLE_CODE);
		}
		promoBits |= bitMask(LONG_CODE, DOUBLE_CODE);
		promoBits |= bitMask(FLOAT_CODE, DOUBLE_CODE);
		PROMOTION_BITS = promoBits;
	}
	/*/
	private static final long PROMOTION_BITS = 0x7E1E1E0E06020000L;
	//*/
	// @formatter:on

	// =============== Fields ===============

	// --------------- Constructor Fields ---------------

	private final Name   name;
	private final String wrapperClassDescriptor;

	private final int  typecode;
	private final char typeChar;

	private final int    opcodeOffset;
	private final int    arrayOpcodeOffset;
	private final Object frameType;

	// --------------- Cache ---------------

	private IClass wrapperClass;

	private IMethod boxMethod;
	private IMethod unboxMethod;

	private IClass arrayClass;
	private IClass refClass;
	private IType  simpleRefType;

	// =============== Constructors ===============

	public PrimitiveType(Name name, String wrapperClassDescriptor, int typecode, char typeChar, int loadOpcode,
		int aloadOpcode, Object frameType)
	{
		this.name = name;
		this.wrapperClassDescriptor = wrapperClassDescriptor;
		this.typecode = typecode;
		this.typeChar = typeChar;
		this.opcodeOffset = loadOpcode - Opcodes.ILOAD;
		this.arrayOpcodeOffset = aloadOpcode - Opcodes.IALOAD;
		this.frameType = frameType;
	}

	// =============== Static Methods ===============

	public static IType getPrimitiveType(String internalClassName)
	{
		switch (internalClassName)
		{
		case AnyType.OBJECT_INTERNAL:
			return Types.ANY;
		case NullType.NULL_INTERNAL:
			return Types.NULL;
		case NoneType.NONE_INTERNAL:
			return Types.NONE;
		case "java/lang/Void":
			return Types.VOID;
		case "java/lang/Boolean":
			return Types.BOOLEAN;
		case "java/lang/Byte":
			return Types.BYTE;
		case "java/lang/Short":
			return Types.SHORT;
		case "java/lang/Character":
			return Types.CHAR;
		case "java/lang/Integer":
			return Types.INT;
		case "java/lang/Long":
			return Types.LONG;
		case "java/lang/Float":
			return Types.FLOAT;
		case "java/lang/Double":
			return Types.DOUBLE;
		}
		return null;
	}

	public static IType getPrimitiveType(IType type)
	{
		return getPrimitiveType(type.getInternalName());
	}

	public static PrimitiveType fromTypecode(int typecode)
	{
		switch (typecode)
		{
		case BOOLEAN_CODE:
			return Types.BOOLEAN;
		case BYTE_CODE:
			return Types.BYTE;
		case SHORT_CODE:
			return Types.SHORT;
		case CHAR_CODE:
			return Types.CHAR;
		case INT_CODE:
			return Types.INT;
		case LONG_CODE:
			return Types.LONG;
		case FLOAT_CODE:
			return Types.FLOAT;
		case DOUBLE_CODE:
			return Types.DOUBLE;
		default:
			return Types.VOID;
		}
	}

	public static PrimitiveType fromFrameType(Object frameType)
	{
		if (frameType.getClass() != Integer.class)
		{
			return null;
		}

		switch ((int) frameType)
		{
		case 1: // ASMConstants.INTEGER
			if (frameType == ClassFormat.BOOLEAN)
			{
				return Types.BOOLEAN;
			}
			if (frameType == ClassFormat.BYTE)
			{
				return Types.BYTE;
			}
			if (frameType == ClassFormat.SHORT)
			{
				return Types.SHORT;
			}
			if (frameType == ClassFormat.CHAR)
			{
				return Types.CHAR;
			}
			if (frameType == ClassFormat.INT)
			{
				return Types.INT;
			}
			return null;
		case 2: // ASMConstants.FLOAT
			return Types.FLOAT;
		case 3: // ASMConstants.DOUBLE
			return Types.DOUBLE;
		case 4: // ASMConstants.LONG
			return Types.LONG;
		}
		return null;
	}

	private static long bitMask(int from, int to)
	{
		return 1L << ((from - 1) | ((to - 1) << 3));
	}

	private static boolean isPromotable(int from, int to)
	{
		return to != 0 && (PROMOTION_BITS & bitMask(from, to)) != 0L;
	}

	// =============== Methods ===============

	@Override
	public int typeTag()
	{
		return PRIMITIVE;
	}

	@Override
	public boolean isPrimitive()
	{
		return true;
	}

	@Override
	public int getTypecode()
	{
		return this.typecode;
	}

	@Override
	public boolean isGenericType()
	{
		return false;
	}

	public IClass getWrapperClass()
	{
		if (this.wrapperClass != null)
		{
			return this.wrapperClass;
		}

		return this.wrapperClass = Package.rootPackage.resolveGlobalClass(this.wrapperClassDescriptor);
	}

	@Override
	public final IType getObjectType()
	{
		return new ClassType(this.getWrapperClass());
	}

	@Override
	public IType asParameterType()
	{
		return this;
	}

	@Override
	public String getTypePrefix()
	{
		switch (this.typecode)
		{
		case INT_CODE:
			return "Int";
		case CHAR_CODE:
			return "Char";
		default:
			return this.getWrapperClass().getName().qualified;
		}
	}

	@Override
	public IClass getRefClass()
	{
		if (this.refClass != null)
		{
			return this.refClass;
		}

		final String className = this.getTypePrefix() + "Ref";
		return this.refClass = Package.dyvilRef.resolveClass(className);
	}

	@Override
	public IType getSimpleRefType()
	{
		if (this.simpleRefType != null)
		{
			return this.simpleRefType;
		}

		final String className = "Simple" + this.getTypePrefix() + "Ref";
		return this.simpleRefType = new ClassType(Package.dyvilRefSimple.resolveClass(className));
	}

	@Override
	public IMethod getBoxMethod()
	{
		if (this.boxMethod != null)
		{
			return this.boxMethod;
		}
		if (this == Types.VOID)
		{
			return this.boxMethod = Package.dyvilLang.resolveClass("Primitives$VoidWrapper").getBody()
			                                         .getMethod(Names.apply);
		}
		return this.boxMethod = IContext.resolveMethod(this.getWrapperClass(), null, Names.valueOf,
		                                               new ArgumentList(new DummyValue(this)));
	}

	@Override
	public IMethod getUnboxMethod()
	{
		if (this.unboxMethod != null)
		{
			return this.unboxMethod;
		}
		if (this == Types.VOID)
		{
			return this.unboxMethod = Package.dyvilLang.resolveClass("Primitives$VoidWrapper").getBody()
			                                           .getMethod(Names.value);
		}
		return this.unboxMethod = this.getWrapperClass().getBody().getMethod(Name.fromRaw(this.name + "Value"));
	}

	@Override
	public IClass getArrayClass()
	{
		IClass iclass = this.arrayClass;
		if (iclass == null)
		{
			final String className = this.getTypePrefix() + "Array";
			return this.arrayClass = Package.dyvilArray.resolveClass(className);
		}
		return iclass;
	}

	@Override
	public Mutability getMutability()
	{
		return Mutability.IMMUTABLE;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public IClass getTheClass()
	{
		return this.getWrapperClass();
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return this.getWrapperClass() == subType.getTheClass() || subType.isPrimitive() && isPromotable(
			subType.getTypecode(), this.typecode);
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.getWrapperClass() == type.getTheClass();
	}

	@Override
	public boolean isSameClass(IType type)
	{
		return type == this;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.getWrapperClass().resolveType(typeParameter, this);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}

	@Override
	public boolean isUninferred()
	{
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		Types.PRIMITIVES_HEADER.getMethodMatches(list, receiver, name, arguments);
		if (list.hasCandidate())
		{
			return;
		}

		if (this.getWrapperClass() != null)
		{
			this.getWrapperClass().getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		Types.PRIMITIVES_HEADER.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.getWrapperClass().getInternalName();
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_SIGNATURE_GENERIC_ARG)
		{
			buffer.append('L').append(this.getInternalName()).append(';');
			return;
		}

		buffer.append(this.typeChar);
	}

	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ILOAD + this.opcodeOffset;
	}

	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.IALOAD + this.arrayOpcodeOffset;
	}

	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ISTORE + this.opcodeOffset;
	}

	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.IASTORE + this.arrayOpcodeOffset;
	}

	@Override
	public int getReturnOpcode()
	{
		return Opcodes.IRETURN + this.opcodeOffset;
	}

	@Override
	public Object getFrameType()
	{
		return this.frameType;
	}

	@Override
	public int getLocalSlots()
	{
		switch (this.typecode)
		{
		case LONG_CODE:
		case DOUBLE_CODE:
			return 2;
		}
		return 1;
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/types/PrimitiveType", this.name.qualified.toUpperCase(),
		                      "Ldyvil/reflect/types/PrimitiveType;");
	}

	@Override
	public void writeDefaultValue(MethodWriter writer)
	{
		switch (this.typecode)
		{
		case BOOLEAN_CODE:
		case BYTE_CODE:
		case SHORT_CODE:
		case CHAR_CODE:
		case INT_CODE:
			writer.visitLdcInsn(0);
			break;
		case LONG_CODE:
			writer.visitLdcInsn(0L);
			break;
		case FLOAT_CODE:
			writer.visitLdcInsn(0F);
			break;
		case DOUBLE_CODE:
			writer.visitLdcInsn(0D);
			break;
		}
	}

	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		if (Types.isVoid(target) && this.typecode != VOID_CODE)
		{
			writer.visitInsn(this.getLocalSlots() == 2 ? Opcodes.POP2 : Opcodes.POP);
			return;
		}

		IType primitiveTarget = target;
		if (!target.isPrimitive())
		{
			// Try to extract a primitive type
			primitiveTarget = getPrimitiveType(target);

			// Target is not a primitive type
			if (primitiveTarget == null || primitiveTarget.getTypecode() < 0)
			{
				this.getBoxMethod().writeInvoke(writer, null, ArgumentList.EMPTY, ITypeContext.DEFAULT, lineNumber);
				return;
			}
		}

		switch (this.typecode)
		{
		case BOOLEAN_CODE:
		case BYTE_CODE:
		case SHORT_CODE:
		case CHAR_CODE:
		case INT_CODE:
			writeIntCast(primitiveTarget, writer);
			break;
		case LONG_CODE:
			writeLongCast(primitiveTarget, writer);
			break;
		case FLOAT_CODE:
			writeFloatCast(primitiveTarget, writer);
			break;
		case DOUBLE_CODE:
			writeDoubleCast(primitiveTarget, writer);
			break;
		}

		// If the target is not primitive
		if (primitiveTarget != target)
		{
			primitiveTarget.getBoxMethod()
			               .writeInvoke(writer, null, ArgumentList.EMPTY, ITypeContext.DEFAULT, lineNumber);
		}
	}

	@Override
	public void writeClassExpression(MethodWriter writer, boolean wrapPrimitives) throws BytecodeException
	{
		String owner;

		// Cannot use PrimitiveType.getInternalName as it returns the Dyvil
		// class instead of the Java one.
		switch (this.typecode)
		{
		case PrimitiveType.BOOLEAN_CODE:
			owner = "java/lang/Boolean";
			break;
		case PrimitiveType.BYTE_CODE:
			owner = "java/lang/Byte";
			break;
		case PrimitiveType.SHORT_CODE:
			owner = "java/lang/Short";
			break;
		case PrimitiveType.CHAR_CODE:
			owner = "java/lang/Character";
			break;
		case PrimitiveType.INT_CODE:
			owner = "java/lang/Integer";
			break;
		case PrimitiveType.LONG_CODE:
			owner = "java/lang/Long";
			break;
		case PrimitiveType.FLOAT_CODE:
			owner = "java/lang/Float";
			break;
		case PrimitiveType.DOUBLE_CODE:
			owner = "java/lang/Double";
			break;
		default:
			owner = "java/lang/Void";
			break;
		}

		if (wrapPrimitives)
		{
			writer.visitLdcInsn(Type.getObjectType(owner));
			return;
		}

		writer.visitFieldInsn(Opcodes.GETSTATIC, owner, "TYPE", "Ljava/lang/Class;");
	}

	private static void writeIntCast(IType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.getTypecode())
		{
		case BOOLEAN_CODE:
		case BYTE_CODE:
		case SHORT_CODE:
		case CHAR_CODE:
		case INT_CODE:
			break;
		case LONG_CODE:
			writer.visitInsn(I2L);
			break;
		case FLOAT_CODE:
			writer.visitInsn(I2F);
			break;
		case DOUBLE_CODE:
			writer.visitInsn(I2D);
			break;
		}
	}

	private static void writeLongCast(IType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.getTypecode())
		{
		case BOOLEAN_CODE:
			writer.visitInsn(L2I);
			break;
		case BYTE_CODE:
			writer.visitInsn(L2B);
			break;
		case SHORT_CODE:
			writer.visitInsn(L2S);
			break;
		case CHAR_CODE:
			writer.visitInsn(L2C);
			break;
		case INT_CODE:
			writer.visitInsn(L2I);
			break;
		case LONG_CODE:
			break;
		case FLOAT_CODE:
			writer.visitInsn(L2F);
			break;
		case DOUBLE_CODE:
			writer.visitInsn(L2D);
			break;
		}
	}

	private static void writeFloatCast(IType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.getTypecode())
		{
		case BOOLEAN_CODE:
			writer.visitInsn(F2I);
			break;
		case BYTE_CODE:
			writer.visitInsn(F2B);
			break;
		case SHORT_CODE:
			writer.visitInsn(F2S);
			break;
		case CHAR_CODE:
			writer.visitInsn(F2C);
			break;
		case INT_CODE:
			writer.visitInsn(F2I);
			break;
		case LONG_CODE:
			writer.visitInsn(F2L);
			break;
		case FLOAT_CODE:
			break;
		case DOUBLE_CODE:
			writer.visitInsn(F2D);
			break;
		}
	}

	private static void writeDoubleCast(IType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.getTypecode())
		{
		case BOOLEAN_CODE:
			writer.visitInsn(D2I);
			break;
		case BYTE_CODE:
			writer.visitInsn(D2B);
			break;
		case SHORT_CODE:
			writer.visitInsn(D2S);
			break;
		case CHAR_CODE:
			writer.visitInsn(D2C);
			break;
		case INT_CODE:
			writer.visitInsn(D2I);
			break;
		case LONG_CODE:
			writer.visitInsn(D2L);
			break;
		case FLOAT_CODE:
			writer.visitInsn(D2F);
			break;
		case DOUBLE_CODE:
			break;
		}
	}

	@Override
	public IConstantValue getDefaultValue()
	{
		switch (this.typecode)
		{
		case BOOLEAN_CODE:
			return BooleanValue.FALSE;
		case BYTE_CODE:
		case SHORT_CODE:
		case CHAR_CODE:
		case INT_CODE:
			return IntValue.ZERO;
		case LONG_CODE:
			return LongValue.ZERO;
		case FLOAT_CODE:
			return FloatValue.ZERO;
		case DOUBLE_CODE:
			return DoubleValue.ZERO;
		}
		return null;
	}

	@Override
	public void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		final int length = typePath.length();
		if (length <= 0)
		{
			return;
		}

		final char lastChar = typePath.charAt(length - 1);
		if (lastChar == ';' || lastChar >= '0' && lastChar <= '9')
		{
			// Argument of a parametric type
			visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.PRIMITIVE,
			                            AnnotationUtil.PRIMITIVE_VISIBLE);
		}
	}

	// --------------- Serialization ---------------

	@Override
	public void read(DataInput in) throws IOException
	{
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.typecode);
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return this.name.qualified;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
	}

	// --------------- Equals and Hash Code ---------------

	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}

	@Override
	public int hashCode()
	{
		return this.typecode;
	}
}
