package dyvil.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static dyvil.reflect.Opcodes.*;

public final class PrimitiveType implements IType
{
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

	private static final long PROMOTION_BITS;

	static
	{
		// Code to generate the value of PROMOTION_BITS. Uncomment as needed.
		// @formatter:off
		/* toggle
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
		/*/
		PROMOTION_BITS = 0x7E1E1E0E06020000L;
		//*/
		// @formatter:on
	}

	protected final Name   name;
	protected       IClass theClass;

	private final int  typecode;
	private final char typeChar;

	private final int    opcodeOffset1;
	private final int    opcodeOffset2;
	private final Object frameType;

	protected IMethod boxMethod;
	protected IMethod unboxMethod;

	private IClass arrayClass;
	private IClass refClass;
	private IType  simpleRefType;

	public PrimitiveType(Name name, int typecode, char typeChar, int loadOpcode, int aloadOpcode, Object frameType)
	{
		this.name = name;
		this.typecode = typecode;
		this.typeChar = typeChar;
		this.opcodeOffset1 = loadOpcode - Opcodes.ILOAD;
		this.opcodeOffset2 = aloadOpcode - Opcodes.IALOAD;
		this.frameType = frameType;
	}

	public static IType getPrimitiveType(String internalClassName)
	{
		switch (internalClassName)
		{
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

	@Override
	public ITypeParameter getTypeVariable()
	{
		return null;
	}

	@Override
	public final IType getObjectType()
	{
		return new ClassType(this.theClass);
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
			return this.theClass.getName().qualified;
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
		return this.boxMethod;
	}

	@Override
	public IMethod getUnboxMethod()
	{
		return this.unboxMethod;
	}

	@Override
	public boolean isArrayType()
	{
		return false;
	}

	@Override
	public int getArrayDimensions()
	{
		return 0;
	}

	@Override
	public IType getElementType()
	{
		return null;
	}

	@Override
	public boolean isExtension()
	{
		return false;
	}

	@Override
	public void setExtension(boolean extension)
	{
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
		return this.theClass;
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return this.theClass == subType.getTheClass() || subType.isPrimitive() && isPromotable(subType.getTypecode(),
		                                                                                       this.typecode);
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.theClass == type.getTheClass();
	}

	private static long bitMask(int from, int to)
	{
		return 1L << ((from - 1) | ((to - 1) << 3));
	}

	private static boolean isPromotable(int from, int to)
	{
		return to != 0 && (PROMOTION_BITS & bitMask(from, to)) != 0L;
	}

	@Override
	public boolean isSameClass(IType type)
	{
		return type == this;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.theClass.resolveType(typeParameter, this);
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
	public void checkType(MarkerList markers, IContext context, TypePosition position)
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, receiver, name, arguments);
			if (!list.isEmpty())
			{
				return;
			}
		}
		Types.PRIMITIVES_CLASS.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		Types.PRIMITIVES_CLASS.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
		return this.theClass.getInternalName();
	}

	@Override
	public void appendExtendedName(StringBuilder buf)
	{
		buf.append(this.typeChar);
	}

	@Override
	public String getSignature()
	{
		return null;
	}

	@Override
	public void appendSignature(StringBuilder buf, boolean genericArg)
	{
		if (!genericArg)
		{
			buf.append(this.typeChar);
			return;
		}
		buf.append('L').append(this.theClass.getInternalName()).append(';');
	}

	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ILOAD + this.opcodeOffset1;
	}

	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.IALOAD + this.opcodeOffset2;
	}

	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ISTORE + this.opcodeOffset1;
	}

	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.IASTORE + this.opcodeOffset2;
	}

	@Override
	public int getReturnOpcode()
	{
		return Opcodes.IRETURN + this.opcodeOffset1;
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
		writer.visitLdcInsn(this.typecode);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/PrimitiveType", "apply",
		                       "(I)Ldyvilx/lang/model/type/PrimitiveType;", false);
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
		IType primitiveTarget = target;
		if (!target.isPrimitive())
		{
			// Try to extract a primitive type
			primitiveTarget = getPrimitiveType(target);

			// Target is not a primitive type
			if (primitiveTarget == null)
			{
				this.boxMethod.writeInvoke(writer, null, EmptyArguments.INSTANCE, ITypeContext.DEFAULT, lineNumber);
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
			               .writeInvoke(writer, null, EmptyArguments.INSTANCE, ITypeContext.DEFAULT, lineNumber);
		}
	}

	@Override
	public void writeClassExpression(MethodWriter writer) throws BytecodeException
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
			return BooleanValue.TRUE;
		case BYTE_CODE:
		case SHORT_CODE:
		case CHAR_CODE:
		case INT_CODE:
			return IntValue.getNull();
		case LONG_CODE:
			return LongValue.getNull();
		case FLOAT_CODE:
			return FloatValue.getNull();
		case DOUBLE_CODE:
			return DoubleValue.getNull();
		}
		return null;
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
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
			visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.PRIMITIVE, true);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.typecode);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
	}

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

	@Override
	public PrimitiveType clone()
	{
		return this; // no clones
	}

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
