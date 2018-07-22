package dyvilx.tools.compiler.ast.attribute.annotation;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.Attribute;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.ObjectCompilable;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

public abstract class Annotation implements Attribute, Typed, ObjectCompilable, IArgumentsConsumer
{
	public static final class LazyFields
	{

		public static final IClass    RETENTION_CLASS  = Package.javaLangAnnotation.resolveClass("Retention");
		public static final IClass    TARGET_CLASS     = Package.javaLangAnnotation.resolveClass("Target");
		public static final IClass    ANNOTATION_CLASS = Package.javaLangAnnotation.resolveClass("Annotation");
		public static final ClassType ANNOTATION       = new ClassType(ANNOTATION_CLASS);

		private LazyFields()
		{
			// no instances
		}
	}

	protected IType type;

	protected ArgumentList arguments = ArgumentList.EMPTY;

	public Annotation()
	{
	}

	public Annotation(IType type)
	{
		this.type = type;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	public String getTypeDescriptor()
	{
		return this.type.getInternalName();
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	public ArgumentList getArguments()
	{
		return this.arguments;
	}

	@Override
	public void setArguments(ArgumentList arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		this.arguments.resolveTypes(markers, context);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);

		final IClass theClass;
		if (this.type == null || (theClass = this.type.getTheClass()) == null)
		{
			return;
		}

		final ParameterList parameterList = theClass.getParameters();
		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			final IParameter parameter = parameterList.get(i);
			final IType parameterType = parameter.getType();

			final IValue value = this.arguments.get(parameter);
			if (value == null)
			{
				if (parameter.getValue() == null)
				{
					markers.add(Markers.semanticError(this.getPosition(), "annotation.parameter.missing", this.type,
					                                  parameter.getName()));
				}
				continue;
			}

			IValue typedValue = value.withType(parameterType, parameterType, markers, context);
			if (typedValue == null)
			{
				markers.add(TypeChecker.typeError(value, parameterType, parameterType, "annotation.parameter.type",
				                                  parameter.getName()));
				continue;
			}

			typedValue = IValue.toAnnotationConstant(typedValue, markers, context);
			if (typedValue != value)
			{
				this.arguments.set(i, parameter.getLabel(), typedValue);
			}
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		this.arguments.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.arguments.cleanup(compilableList, classCompilableList);
	}

	private RetentionPolicy getRetention()
	{
		final IClass type = this.type.getTheClass();
		return type == null ? null : type.getMetadata().getRetention();
	}

	@Override
	public void write(AnnotatableVisitor writer)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(ClassFormat.internalToExtended(this.type.getInternalName()),
			                                  retention == RetentionPolicy.RUNTIME));
		}
	}

	@Override
	public void write(TypeAnnotatableVisitor writer, int typeRef, TypePath path)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitTypeAnnotation(typeRef, path,
			                                      ClassFormat.internalToExtended(this.type.getInternalName()),
			                                      retention == RetentionPolicy.RUNTIME));
		}
	}

	public void write(AnnotationVisitor writer)
	{
		final IClass iclass = this.type.getTheClass();
		final ParameterList parameterList = iclass.getParameters();

		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			final IParameter parameter = parameterList.get(i);
			final IValue argument = this.arguments.get(parameter);
			if (argument != null)
			{
				argument.writeAnnotationValue(writer, parameter.getName().qualified);
			}
		}
		writer.visitEnd();
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
		// TODO write arguments
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
		// TODO read arguments
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		buffer.append('@');
		this.type.toString(prefix, buffer);
		this.arguments.toString(prefix, buffer);
	}
}
