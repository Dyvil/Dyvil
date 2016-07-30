package dyvil.tools.compiler.ast.generic;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ClassOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.TypeOperator;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.IntersectionType;
import dyvil.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import static dyvil.tools.compiler.ast.type.builtin.Types.isSuperClass;
import static dyvil.tools.compiler.ast.type.builtin.Types.isSuperType;

public final class TypeParameter implements ITypeParameter
{
	protected ICodePosition position;

	private AnnotationList annotations;

	protected Variance variance = Variance.INVARIANT;

	protected Name name;

	protected IType[] upperBounds = new IType[1];
	protected int   upperBoundCount;
	protected IType lowerBound;

	// Metadata
	private int index;
	private int parameterIndex;

	private ITypeParametric generic;
	private ReifiedKind reifiedKind = ReifiedKind.NOT_REIFIED;

	private IType defaultType   = Types.OBJECT;
	private IType covariantType = new CovariantTypeVarType(this);

	public TypeParameter(ITypeParametric generic)
	{
		this.generic = generic;
	}

	public TypeParameter(ITypeParametric generic, Name name)
	{
		this.name = name;
		this.generic = generic;
	}

	public TypeParameter(ICodePosition position, ITypeParametric generic)
	{
		this.position = position;
		this.generic = generic;
	}

	public TypeParameter(ICodePosition position, ITypeParametric generic, Name name, Variance variance)
	{
		this.position = position;
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
	public ReifiedKind getReifiedKind()
	{
		return this.reifiedKind;
	}

	@Override
	public int getParameterIndex()
	{
		return this.parameterIndex;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public AnnotationList getAnnotations()
	{
		return this.annotations;
	}

	@Override
	public void setAnnotations(AnnotationList annotations)
	{
		this.annotations = annotations;
	}

	@Override
	public void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
		}

		this.annotations.addAnnotation(annotation);
	}

	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case "Ldyvil/annotation/_internal/Covariant;":
			this.variance = Variance.COVARIANT;
			return false;
		case "Ldyvil/annotation/_internal/Contravariant;":
			this.variance = Variance.CONTRAVARIANT;
			return false;
		}
		return true;
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.annotations.getAnnotation(type);
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.TYPE_PARAMETER;
	}

	@Override
	public void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath)
	{
		this.upperBounds[index] = IType.withAnnotation(this.upperBounds[index], annotation, typePath, 0,
		                                               typePath.getLength());
	}

	@Override
	public IType getDefaultType()
	{
		return this.defaultType;
	}

	@Override
	public IType getCovariantType()
	{
		return this.covariantType;
	}

	@Override
	public int upperBoundCount()
	{
		return this.upperBoundCount;
	}

	@Override
	public void setUpperBound(int index, IType bound)
	{
		this.upperBounds[index] = bound;
	}

	@Override
	public void addUpperBound(IType bound)
	{
		int index = this.upperBoundCount++;
		if (index >= this.upperBounds.length)
		{
			IType[] temp = new IType[this.upperBoundCount];
			System.arraycopy(this.upperBounds, 0, temp, 0, index);
			this.upperBounds = temp;
		}
		this.upperBounds[index] = bound;
	}

	@Override
	public IType getUpperBound(int index)
	{
		return this.upperBounds[index];
	}

	@Override
	public IType[] getUpperBounds()
	{
		return this.upperBounds;
	}

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
		if (this.lowerBound != null || this.upperBoundCount == 0)
		{
			return Types.OBJECT_CLASS;
		}
		return this.upperBounds[0].getTheClass();
	}

	@Override
	public boolean isAssignableFrom(IType type, ITypeContext typeContext)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (!isSuperType(this.upperBounds[i].getConcreteType(typeContext), type))
			{
				return false;
			}
		}
		if (this.lowerBound != null)
		{
			if (!isSuperType(type, this.lowerBound.getConcreteType(typeContext)))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSameType(IType superType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (Types.isSameType(superType, this.upperBounds[i]))
			{
				return true;
			}
		}
		if (this.lowerBound != null)
		{
			if (Types.isSameType(this.lowerBound, superType))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSameClass(IType superType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (Types.isSameClass(superType, this.upperBounds[i]))
			{
				return true;
			}
		}
		if (this.lowerBound != null)
		{
			if (Types.isSameClass(this.lowerBound, superType))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (!isSuperType(this.upperBounds[i], subType))
			{
				return false;
			}
		}
		if (this.lowerBound != null)
		{
			if (!isSuperType(subType, this.lowerBound))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (!isSuperClass(this.upperBounds[i], subType))
			{
				return false;
			}
		}
		if (this.lowerBound != null)
		{
			if (!isSuperClass(subType, this.lowerBound))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (isSuperType(superType, this.upperBounds[i]))
			{
				return true;
			}
		}
		if (this.lowerBound != null)
		{
			if (isSuperType(this.lowerBound, superType))
			{
				return true;
			}
		}
		return Types.isExactType(superType, Types.OBJECT);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			if (isSuperClass(superType, this.upperBounds[i]))
			{
				return true;
			}
		}
		if (this.lowerBound != null)
		{
			if (isSuperClass(this.lowerBound, superType))
			{
				return true;
			}
		}
		return Types.isSameClass(superType, Types.OBJECT);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field;

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			field = this.upperBounds[i].resolveField(name);
			if (field != null)
			{
				return field;
			}
		}

		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue instance, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].getMethodMatches(list, instance, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolveType(markers, context);
		}

		if (this.upperBoundCount > 0)
		{
			// The first upper bound is meant to be a class bound.
			IType type = this.upperBounds[0] = this.upperBounds[0].resolveType(markers, context);
			IClass iclass = type.getTheClass();
			if (iclass != null && iclass.hasModifier(Modifiers.INTERFACE_CLASS))
			{
				// shift the entire array one to the right and insert
				// Types.OBJECT at index 0
				if (++this.upperBoundCount > this.upperBounds.length)
				{
					IType[] temp = new IType[this.upperBoundCount];
					type = temp[0] = Types.OBJECT;
					System.arraycopy(this.upperBounds, 0, temp, 1, this.upperBoundCount - 1);
					this.upperBounds = temp;
				}
				else
				{
					System.arraycopy(this.upperBounds, 0, this.upperBounds, 1, this.upperBoundCount - 1);
					type = this.upperBounds[0] = Types.OBJECT;
				}
			}

			this.defaultType = type;

			// Check if the remaining upper bounds are interfaces
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				type = this.upperBounds[i] = this.upperBounds[i].resolveType(markers, context);
				this.defaultType = IntersectionType.combine(this.defaultType, type, null);

				iclass = type.getTheClass();
				if (iclass != null && !iclass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					final Marker marker = Markers.semanticError(type.getPosition(), "typeparameter.bound.class");
					marker.addInfo(Markers.getSemantic("class.declaration", Util.classSignatureToString(iclass)));
					markers.add(marker);
				}
			}
		}

		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);

			IAnnotation reifiedAnnotation = this.annotations.getAnnotation(Types.REIFIED_CLASS);
			if (reifiedAnnotation != null)
			{
				final IValue erasure = reifiedAnnotation.getArguments().getFirstValue();
				if (erasure != null && erasure.booleanValue())
				{
					this.reifiedKind = ReifiedKind.REIFIED_ERASURE;
				}
				else
				{
					this.reifiedKind = ReifiedKind.REIFIED_TYPE;
				}
			}
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}

		if (this.lowerBound != null)
		{
			this.lowerBound.resolve(markers, context);
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}

		if (this.lowerBound != null)
		{
			this.lowerBound.checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, ElementType.TYPE_PARAMETER);
		}

		if (this.lowerBound != null)
		{
			this.lowerBound.check(markers, context);
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}

		if (this.lowerBound != null)
		{
			this.lowerBound.foldConstants();
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(context, compilableList);
		}

		if (this.lowerBound != null)
		{
			this.lowerBound.cleanup(context, compilableList);
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].cleanup(context, compilableList);
		}
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name).append(':');
		if (this.upperBoundCount > 0)
		{
			if (this.upperBounds[0] != Types.OBJECT || this.upperBoundCount == 1)
			{
				this.upperBounds[0].appendSignature(buffer, false);
			}

			for (int i = 1; i < this.upperBoundCount; i++)
			{
				buffer.append(':');
				this.upperBounds[i].appendSignature(buffer, false);
			}
		}
		else
		{
			buffer.append("Ljava/lang/Object;");
		}
	}

	@Override
	public void appendParameterDescriptor(StringBuilder buffer)
	{
		if (this.reifiedKind == ReifiedKind.REIFIED_ERASURE)
		{
			buffer.append("Ljava/lang/Class;");
		}
		else if (this.reifiedKind == ReifiedKind.REIFIED_TYPE)
		{
			buffer.append("Ldyvilx/lang/model/type/Type;");
		}
	}

	@Override
	public void appendParameterSignature(StringBuilder buffer)
	{
		this.appendParameterDescriptor(buffer);
	}

	@Override
	public void writeParameter(MethodWriter writer) throws BytecodeException
	{
		final IType type;
		if (this.reifiedKind == ReifiedKind.REIFIED_ERASURE)
		{
			type = ClassOperator.LazyFields.CLASS;
		}
		else if (this.reifiedKind == ReifiedKind.REIFIED_TYPE)
		{
			type = TypeOperator.LazyFields.TYPE;
		}
		else
		{
			return;
		}

		this.parameterIndex = writer.localCount();
		writer.visitParameter(this.parameterIndex, "reify$" + this.getName().qualified, type, Modifiers.MANDATED);
	}

	@Override
	public void writeArgument(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.reifiedKind == ReifiedKind.REIFIED_ERASURE)
		{
			type.writeClassExpression(writer);
		}
		else if (this.reifiedKind == ReifiedKind.REIFIED_TYPE)
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
				              "Ldyvil/annotation/_internal/Contravariant;" :
				              "Ldyvil/annotation/_internal/Covariant;";
			visitor.visitTypeAnnotation(typeRef, null, type, true).visitEnd();
		}

		if (this.annotations != null)
		{
			for (int i = 0, count = this.annotations.annotationCount(); i < count; i++)
			{
				this.annotations.getAnnotation(i).write(visitor, typeRef, null);
			}
		}

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			final int boundTypeRef = TypeReference.newTypeParameterBoundReference(
				method ? TypeReference.METHOD_TYPE_PARAMETER_BOUND : TypeReference.CLASS_TYPE_PARAMETER_BOUND,
				this.index, i);
			this.upperBounds[i].writeAnnotations(visitor, boundTypeRef, "");
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);

		Variance.write(this.variance, out);

		IType.writeType(this.lowerBound, out);

		out.writeShort(this.upperBoundCount);

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			IType.writeType(this.upperBounds[i], out);
		}
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.fromRaw(in.readUTF());

		this.variance = Variance.read(in);

		this.lowerBound = IType.readType(in);

		this.upperBoundCount = in.readShort();

		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i] = IType.readType(in);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).toString(prefix, buffer);
				buffer.append(' ');
			}
		}

		this.variance.appendPrefix(buffer);
		buffer.append(this.name);

		if (this.lowerBound != null)
		{
			Formatting.appendSeparator(buffer, "type.bound", ">:");
			this.lowerBound.toString(prefix, buffer);
		}
		if (this.upperBoundCount > 0)
		{
			Formatting.appendSeparator(buffer, "type.bound", "<:");
			this.upperBounds[0].toString(prefix, buffer);
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				Formatting.appendSeparator(buffer, "type.bound.separator", '&');
				this.upperBounds[i].toString(prefix, buffer);
			}
		}
	}
}
