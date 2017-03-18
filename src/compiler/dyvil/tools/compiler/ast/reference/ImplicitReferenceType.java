package dyvil.tools.compiler.ast.reference;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.MethodCall;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ImplicitReferenceType extends ReferenceType
{
	// Constructors

	public ImplicitReferenceType(@NonNull IType type)
	{
		super(type);
	}

	public ImplicitReferenceType(@Nullable IClass refClass, @Nullable IType type)
	{
		super(refClass, type);
	}

	@Override
	protected ReferenceType wrap(@NonNull IType type)
	{
		return new ImplicitReferenceType(type.getRefClass(), type);
	}

	// Conversion Methods

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return Types.isSuperType(this, new ReferenceType(type));
	}

	@Override
	public boolean isConvertibleTo(IType type)
	{
		return Types.isSuperType(type, this.type);
	}

	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType valueType = value.getType();
		if (this.isSuperTypeOf(valueType))
		{
			return value;
		}

		IValue typedValue = value.withType(this.type, typeContext, markers, context);
		if (typedValue == null)
		{
			return null;
		}

		if (!this.isConvertibleFrom(typedValue.getType()))
		{
			return null;
		}

		final IValue referenceValue = value.toReferenceValue(markers, context);
		if (referenceValue != null)
		{
			return referenceValue;
		}

		final IReference ref = value.toReference();
		if (ref != null)
		{
			return new ReferenceOperator(value, ref);
		}

		markers.add(Markers.semanticError(value.getPosition(), "reference.expression.invalid"));
		return typedValue;
	}

	@Override
	public IValue convertValueTo(IValue value, IType targetType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isConvertibleTo(targetType))
		{
			return null;
		}

		final IMethod method = IContext.resolveMethod(this.theClass, value, Names.get, ArgumentList.EMPTY);
		return new MethodCall(value.getPosition(), value, method, ArgumentList.EMPTY);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType type = this.type.getConcreteType(context);
		if (type != this.type)
		{
			return new ImplicitReferenceType(type);
		}
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		this.type.getMethodMatches(list, receiver, name, arguments);
		super.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.IMPLICITLY_UNWRAPPED, false)
		       .visitEnd();
	}

	@Override
	public String toString()
	{
		return this.type.toString() + '^';
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('^');
	}
}
