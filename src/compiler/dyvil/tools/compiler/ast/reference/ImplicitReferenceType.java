package dyvil.tools.compiler.ast.reference;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ImplicitReferenceType extends ReferenceType
{
	public ImplicitReferenceType(IType type)
	{
		super(type);
	}

	public ImplicitReferenceType(IClass iclass, IType type)
	{
		super(iclass, type);
	}

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return Types.isSuperType(this.type, type);
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

		if (!Types.isSameType(typedValue.getType(), this.type))
		{
			return null;
		}

		final IReference ref = value.toReference();
		if (ref != null)
		{
			return new ReferenceOperator(value, ref);
		}

		markers.add(Markers.semantic(value.getPosition(), "reference.expression.invalid"));
		return typedValue;
	}

	@Override
	public IValue convertValueTo(IValue value, IType targetType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isConvertibleTo(targetType))
		{
			return null;
		}

		final IMethod method = IContext.resolveMethod(this.theClass, value, Names.get, EmptyArguments.INSTANCE);
		return new MethodCall(value.getPosition(), value, method, EmptyArguments.INSTANCE);
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.type.getMethodMatches(list, instance, name, arguments);
		super.getMethodMatches(list, instance, name, arguments);
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
