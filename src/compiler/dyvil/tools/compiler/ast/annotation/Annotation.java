package dyvil.tools.compiler.ast.annotation;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

public final class Annotation implements IAnnotation
{
	public static final class LazyFields
	{
		public static final IClass RETENTION_CLASS = Package.javaLangAnnotation.resolveClass("Retention");
		public static final IClass TARGET_CLASS    = Package.javaLangAnnotation.resolveClass("Target");

		public static final IClass    ANNOTATION_CLASS = Package.javaLangAnnotation.resolveClass("Annotation");
		public static final ClassType ANNOTATION       = new ClassType(ANNOTATION_CLASS);

		private LazyFields()
		{
			// no instances
		}
	}

	public static final CodeParameter VALUE = new CodeParameter(Name.fromRaw("value"));

	protected ICodePosition position;
	protected IArguments arguments = ArgumentList.EMPTY;

	// Metadata
	protected IType type;

	public Annotation()
	{
	}

	public Annotation(IType type)
	{
		this.type = type;
	}

	public Annotation(ICodePosition position)
	{
		this.position = position;
	}

	public Annotation(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}

	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "annotation.type.invalid"));
			this.type = Types.UNKNOWN;
		}

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

		final IParameterList parameterList = theClass.getParameterList();
		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			final IParameter parameter = parameterList.get(i);
			final IType parameterType = parameter.getType();

			final IValue value = this.arguments.getValue(i, parameter);
			if (value == null)
			{
				if (parameter.getValue() == null)
				{
					markers.add(Markers.semanticError(this.position, "annotation.parameter.missing", this.type,
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
				this.arguments.setValue(i, parameter, typedValue);
			}
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.checkType(markers, context, TypePosition.CLASS);
		}

		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		if (this.type == null || !this.type.isResolved())
		{
			return;
		}

		final IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return;
		}

		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(Markers.semanticError(this.position, "annotation.type", this.type.getName()));
			return;
		}

		if (target == null)
		{
			return;
		}

		final IClassMetadata metadata = theClass.getMetadata();
		if (!metadata.isTarget(target))
		{
			final Marker error = Markers.semanticError(this.position, "annotation.target", this.type.getName());
			error.addInfo(Markers.getSemantic("annotation.target.element", target));
			error.addInfo(Markers.getSemantic("annotation.target.allowed", metadata.getTargets()));
			markers.add(error);
		}
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

	@Override
	public void write(AnnotationVisitor writer)
	{
		final IClass iclass = this.type.getTheClass();
		final IParameterList parameterList = iclass.getParameterList();

		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			final IParameter parameter = parameterList.get(i);
			final IValue argument = this.arguments.getValue(i, parameter);
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
		return IASTNode.toString(this);
	}

	@Override
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		buffer.append('@');
		this.type.toString(prefix, buffer);
		this.arguments.toString(prefix, buffer);
	}
}
