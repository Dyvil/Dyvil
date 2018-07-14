package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Name;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.IntersectionType;
import dyvilx.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import static dyvilx.tools.compiler.ast.type.builtin.Types.isSuperClass;
import static dyvilx.tools.compiler.ast.type.builtin.Types.isSuperType;

public abstract class TypeParameter implements ITypeParameter
{
	protected @NonNull AttributeList attributes = new AttributeList();
	protected          Variance      variance   = Variance.INVARIANT;

	protected Name name;

	protected @NonNull IType upperBound = Types.NULLABLE_ANY;
	protected @Nullable IType lowerBound;

	// Metadata
	protected int index;
	protected IType erasure = Types.OBJECT;
	private   IType   safeUpperBound;
	protected IType[] upperBounds;

	protected ITypeParametric generic;
	protected Reified.Type    reifiedKind; // defaults to null (not reified)
	protected IParameter      reifyParameter;

	private final IType covariantType = new CovariantTypeVarType(this);

	public TypeParameter(ITypeParametric generic)
	{
		this.generic = generic;
	}

	public TypeParameter(ITypeParametric generic, Name name)
	{
		this.name = name;
		this.generic = generic;
	}

	public TypeParameter(ITypeParametric generic, Name name, Variance variance)
	{
		this.name = name;
		this.generic = generic;
		this.variance = variance;
	}

	@Override
	public ITypeParametric getGeneric()
	{
		return this.generic;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}

	@Override
	public Variance getVariance()
	{
		return this.variance;
	}

	@Override
	public Reified.Type getReifiedKind()
	{
		return this.reifiedKind;
	}

	@Override
	public IParameter getReifyParameter()
	{
		return this.reifyParameter;
	}

	@Override
	public void setReifyParameter(IParameter parameter)
	{
		this.reifyParameter = parameter;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.TYPE_PARAMETER;
	}

	@Override
	public AttributeList getAttributes()
	{
		return this.attributes;
	}

	@Override
	public void setAttributes(AttributeList attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public final Annotation getAnnotation(IClass type)
	{
		return this.attributes.getAnnotation(type);
	}

	@Override
	public boolean skipAnnotation(String type, Annotation annotation)
	{
		switch (type)
		{
		case "dyvil/annotation/internal/Covariant":
			this.variance = Variance.COVARIANT;
			return true;
		case "dyvil/annotation/internal/Contravariant":
			this.variance = Variance.CONTRAVARIANT;
			return true;
		}
		return false;
	}

	protected void computeReifiedKind()
	{
		if (this.reifiedKind != null)
		{
			return;
		}

		final Annotation reifiedAnnotation = this.getAnnotation(Types.REIFIED_CLASS);
		if (reifiedAnnotation != null)
		{
			final IParameter parameter = Types.REIFIED_CLASS.getParameters().get(0);
			this.reifiedKind = EnumValue
				                   .eval(reifiedAnnotation.getArguments().getOrDefault(parameter), Reified.Type.class);
		}
	}

	@Override
	public void addBoundAnnotation(Annotation annotation, int index, TypePath typePath)
	{
		this.upperBounds[index] = IType.withAnnotation(this.upperBounds[index], annotation, typePath);
	}

	@Override
	public IType getErasure()
	{
		return this.upperBounds[0];
	}

	@Override
	public IType getCovariantType()
	{
		return this.covariantType;
	}

	// Upper Bound

	@Override
	public IType getUpperBound()
	{
		return this.upperBound;
	}

	private IType getSafeUpperBound()
	{
		if (this.safeUpperBound != null)
		{
			return this.safeUpperBound;
		}
		return (this.safeUpperBound = this.getUpperBound().getConcreteType(this::replaceBackRefs));
	}

	@Nullable
	private IType replaceBackRefs(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() == this.getGeneric() && typeParameter.getIndex() >= this.getIndex())
		{
			return new CovariantTypeVarType(this, true);
		}
		return null;
	}

	@Override
	public void setUpperBound(IType bound)
	{
		this.upperBound = bound;
	}

	// Lower Bound

	@Override
	public void setLowerBound(IType bound)
	{
		this.lowerBound = bound;
	}

	@Override
	public IType getLowerBound()
	{
		return this.lowerBound;
	}

	@Override
	public IClass getTheClass()
	{
		return this.getSafeUpperBound().getTheClass();
	}

	@Override
	public boolean isAssignableFrom(IType type, ITypeContext typeContext)
	{
		if (!Types.isSuperType(this.getSafeUpperBound().getConcreteType(typeContext), type))
		{
			return false;
		}
		final IType lowerBound = this.getLowerBound();
		return lowerBound == null || Types.isSuperType(type, lowerBound.getConcreteType(typeContext));
	}

	@Override
	public boolean isSameType(IType type)
	{
		return Types.isSameType(type, this.getSafeUpperBound());
	}

	@Override
	public boolean isSameClass(IType type)
	{
		return Types.isSameClass(type, this.getSafeUpperBound());
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return isSuperType(this.getSafeUpperBound(), subType);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return isSuperClass(this.getSafeUpperBound(), subType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return isSuperType(superType, this.getSafeUpperBound());
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return isSuperClass(superType, this.getSafeUpperBound());
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getSafeUpperBound().resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue instance, Name name, ArgumentList arguments)
	{
		this.getSafeUpperBound().getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.getSafeUpperBound().getImplicitMatches(list, value, targetType);
	}

	protected static IType[] getUpperBounds(IType upperBound)
	{
		// Flatten the tree-like upperBound structure into a list
		final List<IType> list = new ArrayList<>();
		getUpperBounds(list, upperBound);
		return list.toArray(IType.class);
	}

	private static void getUpperBounds(List<IType> list, IType upperBound)
	{
		if (upperBound.typeTag() != IType.INTERSECTION)
		{
			list.add(upperBound);
			return;
		}

		final IntersectionType intersection = (IntersectionType) upperBound;
		getUpperBounds(list, intersection.getLeft());
		getUpperBounds(list, intersection.getRight());
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name).append(':');
		if (this.upperBounds == null)
		{
			buffer.append("Ljava/lang/Object;");
			return;
		}

		final IClass theClass = this.upperBounds[0].getTheClass();
		if (theClass != null && theClass.isInterface())
		{
			// If the first type is not an interface type, we append two colons
			buffer.append(':');
		}
		this.upperBounds[0].appendSignature(buffer, false);
		for (int i = 1, count = this.upperBounds.length; i < count; i++)
		{
			buffer.append(':');
			this.upperBounds[i].appendSignature(buffer, false);
		}
	}

	@Override
	public void appendParameterDescriptor(StringBuilder buffer)
	{
		if (this.reifiedKind == Reified.Type.TYPE)
		{
			buffer.append("Ldyvil/reflect/types/Type;");
		}
		else if (this.reifiedKind != null) // OBJECT_CLASS or ANY_CLASS
		{
			buffer.append("Ljava/lang/Class;");
		}
	}

	@Override
	public void appendParameterSignature(StringBuilder buffer)
	{
		this.appendParameterDescriptor(buffer);
	}

	@Override
	public void writeArgument(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.reifiedKind == Reified.Type.ANY_CLASS)
		{
			type.writeClassExpression(writer, false);
		}
		else if (this.reifiedKind == Reified.Type.OBJECT_CLASS)
		{
			// Convert primitive types to their reference counterpart
			type.writeClassExpression(writer, true);
		}
		else if (this.reifiedKind == Reified.Type.TYPE)
		{
			type.writeTypeExpression(writer);
		}
	}

	@Override
	public void write(TypeAnnotatableVisitor visitor)
	{
		boolean method = this.generic instanceof IMethod;
		int typeRef = TypeReference.newTypeParameterReference(
			method ? TypeReference.METHOD_TYPE_PARAMETER : TypeReference.CLASS_TYPE_PARAMETER, this.index);

		if (this.variance != Variance.INVARIANT)
		{
			String type = this.variance == Variance.CONTRAVARIANT ?
				              "Ldyvil/annotation/internal/Contravariant;" :
				              "Ldyvil/annotation/internal/Covariant;";
			visitor.visitTypeAnnotation(typeRef, null, type, true).visitEnd();
		}

		this.attributes.write(visitor, typeRef, null);

		if (this.upperBounds == null)
		{
			return;
		}

		for (int i = 0, size = this.upperBounds.length; i < size; i++)
		{
			final int boundTypeRef = TypeReference.newTypeParameterBoundReference(
				method ? TypeReference.METHOD_TYPE_PARAMETER_BOUND : TypeReference.CLASS_TYPE_PARAMETER_BOUND,
				this.index, i);
			IType.writeAnnotations(this.upperBounds[i], visitor, boundTypeRef, "");
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.name.write(out);

		Variance.write(this.variance, out);

		IType.writeType(this.lowerBound, out);
		IType.writeType(this.upperBound, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.read(in);

		this.variance = Variance.read(in);

		this.lowerBound = IType.readType(in);
		this.upperBound = IType.readType(in);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributes.toInlineString(indent, buffer);

		buffer.append("type ");

		this.variance.appendPrefix(buffer);
		buffer.append(this.name);

		final IType upperBound = this.getSafeUpperBound();
		if (upperBound != null)
		{
			buffer.append(": ");
			upperBound.toString(indent, buffer);
		}

		final IType lowerBound = this.getLowerBound();
		if (lowerBound != null)
		{
			buffer.append(" super ");
			lowerBound.toString(indent, buffer);
		}
	}
}
