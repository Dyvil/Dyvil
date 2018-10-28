package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
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
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

import static dyvilx.tools.compiler.ast.type.builtin.Types.isSuperClass;
import static dyvilx.tools.compiler.ast.type.builtin.Types.isSuperType;

public abstract class TypeParameter implements ITypeParameter
{
	// =============== Fields ===============

	// --------------- Accompanying Member ---------------

	protected ITypeParametric generic;

	// --------------- Declaration ---------------

	protected @NonNull AttributeList attributes = new AttributeList();
	protected          Variance      variance   = Variance.INVARIANT;

	protected Name name;

	private @NonNull    IType upperBound = Types.NULLABLE_ANY;
	protected @Nullable IType lowerBound;

	// --------------- Metadata ---------------

	private IType   safeUpperBound;
	private IType[] upperBounds;

	protected Reified.Type reifiedKind; // defaults to null (not reified)
	protected IParameter   reifyParameter;

	private final IType covariantType = new CovariantTypeVarType(this);

	// =============== Constructors ===============

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

	// =============== Properties ===============

	// --------------- Accompanying Member ---------------

	@Override
	public ITypeParametric getGeneric()
	{
		return this.generic;
	}

	// --------------- Attributes ---------------

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

	// --------------- Reification ---------------

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

	// --------------- Variance ---------------

	@Override
	public Variance getVariance()
	{
		return this.variance;
	}

	@Override
	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}

	// --------------- Name ---------------

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

	// --------------- Upper Bound ---------------

	@Override
	public IType getUpperBound()
	{
		if (this.upperBound != null)
		{
			return this.upperBound;
		}
		final IType[] upperBounds = this.getUpperBounds();
		if (upperBounds != null)
		{
			return this.upperBound = getUpperBound(upperBounds, 0, upperBounds.length);
		}
		return null;
	}

	@Override
	public void setUpperBound(IType bound)
	{
		this.upperBound = bound;
		this.upperBounds = null;
		this.safeUpperBound = null;
	}

	public IType[] getUpperBounds()
	{
		if (this.upperBounds != null)
		{
			return this.upperBounds;
		}

		final IType upperBound = this.getUpperBound();
		if (upperBound != null)
		{
			// Flatten the tree-like upperBound structure into a list
			final List<IType> list = new ArrayList<>();
			getUpperBounds(list, upperBound);
			return this.upperBounds = list.toArray(new IType[0]);
		}
		return null;
	}

	public void setUpperBounds(IType[] upperBounds)
	{
		this.upperBounds = upperBounds;
		this.upperBound = null;
		this.safeUpperBound = null;
	}

	/**
	 * Creates a balanced tree for the slice of the given array
	 *
	 * @param upperBounds
	 * 	the upper bounds array
	 * @param start
	 * 	the start index
	 * @param count
	 * 	the number of elements
	 *
	 * @return a balanced tree of {@link IntersectionType}s
	 */
	private static IType getUpperBound(IType[] upperBounds, int start, int count)
	{
		if (count == 1)
		{
			return upperBounds[start];
		}

		final int halfCount = count / 2;
		return new IntersectionType(getUpperBound(upperBounds, start, halfCount),
		                            getUpperBound(upperBounds, start + halfCount, count - halfCount));
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

	// --------------- Safe Upper Bound ---------------

	private IType getSafeUpperBound()
	{
		if (this.safeUpperBound != null)
		{
			return this.safeUpperBound;
		}
		return (this.safeUpperBound = this.getUpperBound().getConcreteType(this::replaceBackRefs));
	}

	private @Nullable IType replaceBackRefs(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() == this.getGeneric() && typeParameter.getIndex() >= this.getIndex())
		{
			return new CovariantTypeVarType(this, true);
		}
		return null;
	}

	// --------------- Lower Bound ---------------

	@Override
	public IType getLowerBound()
	{
		return this.lowerBound;
	}

	@Override
	public void setLowerBound(IType bound)
	{
		this.lowerBound = bound;
	}

	// --------------- Other Types ---------------

	@Override
	public IType getErasure()
	{
		return this.getUpperBounds()[0];
	}

	@Override
	public IType getCovariantType()
	{
		return this.covariantType;
	}

	@Override
	public IClass getTheClass()
	{
		return this.getSafeUpperBound().getTheClass();
	}

	// =============== Methods ===============

	// --------------- Subtyping ---------------

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

	// --------------- Field and Method Resolution ---------------

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

	// --------------- Descriptor and Signature ---------------

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name).append(':');

		final IType[] upperBounds = this.getUpperBounds();
		if (upperBounds == null || upperBounds.length == 0)
		{
			buffer.append("Ljava/lang/Object;");
			return;
		}

		final IClass theClass = upperBounds[0].getTheClass();
		if (theClass != null && theClass.isInterface())
		{
			// If the first type is an interface, we append two colons
			// T::Lmy/Interface;
			buffer.append(':');
		}

		upperBounds[0].appendSignature(buffer, false);
		for (int i = 1, count = upperBounds.length; i < count; i++)
		{
			buffer.append(':');
			upperBounds[i].appendSignature(buffer, false);
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

	// --------------- Compilation ---------------

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
		final int index = this.getIndex();

		int typeRef = TypeReference.newTypeParameterReference(
			method ? TypeReference.METHOD_TYPE_PARAMETER : TypeReference.CLASS_TYPE_PARAMETER, index);

		if (this.variance != Variance.INVARIANT)
		{
			String type = this.variance == Variance.CONTRAVARIANT ?
				              "Ldyvil/annotation/internal/Contravariant;" :
				              "Ldyvil/annotation/internal/Covariant;";
			visitor.visitTypeAnnotation(typeRef, null, type, true).visitEnd();
		}

		this.attributes.write(visitor, typeRef, null);

		final IType[] upperBounds = this.getUpperBounds();
		for (int i = 0, size = upperBounds.length; i < size; i++)
		{
			final int boundTypeRef = TypeReference.newTypeParameterBoundReference(
				method ? TypeReference.METHOD_TYPE_PARAMETER_BOUND : TypeReference.CLASS_TYPE_PARAMETER_BOUND, index,
				i);
			IType.writeAnnotations(upperBounds[i], visitor, boundTypeRef, "");
		}
	}

	// --------------- Serialization ---------------

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.name.write(out);

		Variance.write(this.variance, out);

		IType.writeType(this.lowerBound, out);
		IType.writeType(this.getUpperBound(), out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.read(in);

		this.variance = Variance.read(in);

		this.lowerBound = IType.readType(in);
		this.setUpperBound(IType.readType(in));
	}

	// --------------- Formatting ---------------

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
