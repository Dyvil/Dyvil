package dyvilx.tools.compiler.ast.reference;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

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
	public IValue convertFrom(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IValue typedValue = value.withType(this.type, typeContext, markers, context);
		if (typedValue == null || !this.isConvertibleFrom(typedValue.getType()))
		{
			return null;
		}

		final IValue reference = typedValue.toReferenceValue(markers, context);
		if (reference != null)
		{
			return reference;
		}

		markers.add(Markers.semanticError(this.getPosition(), "reference.expression.invalid"));
		return typedValue;
	}

	@Override
	public IValue convertTo(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isConvertibleTo(type))
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
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
