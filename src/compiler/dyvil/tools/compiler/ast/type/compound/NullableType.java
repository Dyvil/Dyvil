package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NullableType implements IObjectType
{
	protected IType type;

	public NullableType()
	{
	}

	public NullableType(IType type)
	{
		this.type = type;
	}

	public static NullableType apply(IType type)
	{
		return new NullableType(type);
	}

	public IType getElementType()
	{
		return this.type;
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
		final NullableType nullable = subType.extract(NullableType.class);
		return Types.isSuperType(this.type, nullable != null ? nullable.getElementType() : subType);
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

	protected NullableType wrap(IType type)
	{
		return new NullableType(type);
	}

	@Override
	public IType asReturnType()
	{
		final IType type = this.type.asReturnType();
		return type == this.type ? this : this.wrap(type);
	}

	@Override
	public IType asParameterType()
	{
		final IType type = this.type.asParameterType();
		return type == this.type ? this : this.wrap(type);
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
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

		this.type.appendDescriptor(buffer, type);
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/OptionType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/OptionType;", false);
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

		return null;
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
