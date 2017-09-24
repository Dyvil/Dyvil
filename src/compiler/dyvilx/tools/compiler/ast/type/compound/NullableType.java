package dyvilx.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.constant.IConstantValue;
import dyvilx.tools.compiler.ast.expression.constant.NullValue;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.IObjectType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NullableType implements IObjectType
{
	protected IType type;

	public NullableType()
	{
	}

	@Deprecated
	public NullableType(IType type)
	{
		this.type = type.getObjectType();
	}

	public static IType apply(IType type)
	{
		if (isNullable(type))
		{
			return type;
		}
		return new NullableType(type);
	}

	public static IType unapply(IType type)
	{
		final NullableType nullable = type.extract(NullableType.class);
		return nullable == null ? type : nullable.getElementType();
	}

	public static boolean isNullable(IType type)
	{
		return type.canExtract(NullableType.class);
	}

	public IType getElementType()
	{
		return this.type;
	}

	protected NullableType wrap(IType type)
	{
		return new NullableType(type);
	}

	public void setElementType(IType type)
	{
		this.type = type;
	}

	@Override
	public int typeTag()
	{
		return OPTIONAL;
	}

	@Override
	public boolean isGenericType()
	{
		return this.type.isGenericType();
	}

	@Override
	public Name getName()
	{
		return this.type.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return this.type.getTheClass();
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	@Override
	public int subTypeCheckLevel()
	{
		return SUBTYPE_NULLABLE;
	}

	@Override
	public boolean isSameType(IType type)
	{
		final NullableType nullable = type.extract(NullableType.class);
		return nullable != null && Types.isSameType(this.type, nullable.getElementType());
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		final NullableType nullable = superType.extract(NullableType.class);
		return nullable != null && Types.isSuperType(nullable.getElementType(), this.type);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return Types.isSuperClass(superType, this.type);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return Types.isSuperClass(this.type, subType);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		if (subType.hasTag(IType.NULL))
		{
			return true;
		}

		final NullableType nullable = subType.extract(NullableType.class);
		if (nullable != null)
		{
			return Types.isSuperType(this, nullable.getElementType());
		}
		return Types.isSuperType(this.type, subType);
	}

	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!Types.isSuperType(this, value.getType()))
		{
			return this.type.convertValue(value, typeContext, markers, context);
		}

		return IObjectType.super.convertValue(value, typeContext, markers, context);
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.type.resolveType(typeParameter);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public IType asParameterType()
	{
		final IType type = this.type.asParameterType();
		return type == this.type ? this : this.wrap(type);
	}

	@Override
	public boolean hasTag(int tag)
	{
		return IObjectType.super.hasTag(tag) || this.type.hasTag(tag);
	}

	@Override
	public boolean canExtract(Class<? extends IType> type)
	{
		return IObjectType.super.canExtract(type) || this.type.canExtract(type);
	}

	@Override
	public <T extends IType> T extract(Class<T> type)
	{
		if (IObjectType.super.canExtract(type))
		{
			return (T) this;
		}
		return this.type.extract(type);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType type = this.type.getConcreteType(context);
		return type == this.type ? this : this.wrap(type);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		final NullableType nullable = concrete.extract(NullableType.class);
		if (nullable != null)
		{
			concrete = nullable.getElementType();
		}
		this.type.inferTypes(concrete, typeContext);
	}

	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		this.type.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
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
		return this.type.getInternalName();
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append('?');
			this.type.appendDescriptor(buffer, NAME_FULL);
			return;
		}

		this.type.appendDescriptor(buffer, this.type.isPrimitive() ? NAME_SIGNATURE_GENERIC_ARG : type);
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/OptionType", "apply",
		                       "(Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/OptionType;", false);
	}

	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.visitInsn(Opcodes.ACONST_NULL);
	}

	@Override
	public IConstantValue getDefaultValue()
	{
		return NullValue.NULL;
	}

	@Override
	public IType withAnnotation(IAnnotation annotation)
	{
		switch (annotation.getType().getInternalName())
		{
		case AnnotationUtil.NOTNULL_INTERNAL:
			return this.type;
		case AnnotationUtil.NULLABLE_INTERNAL:
			return this;
		}

		final IType withAnnotation = this.type.withAnnotation(annotation);
		if (withAnnotation == null)
		{
			return null;
		}

		this.type = withAnnotation;
		return this;
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		this.type = IType.withAnnotation(this.type, annotation, typePath, step, steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.NULLABLE, false).visitEnd();
		this.type.writeAnnotations(visitor, typeRef, typePath);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return this.type.toString() + '?';
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('?');
	}
}
