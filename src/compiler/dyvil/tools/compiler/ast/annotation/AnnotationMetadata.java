package dyvil.tools.compiler.ast.annotation;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.Set;

public final class AnnotationMetadata implements IClassMetadata
{
	private IClass           theClass;
	public  RetentionPolicy  retention;
	public  Set<ElementType> targets;

	public AnnotationMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}

	@Override
	public RetentionPolicy getRetention()
	{
		return this.retention;
	}

	@Override
	public Set<ElementType> getTargets()
	{
		return this.targets;
	}

	@Override
	public boolean isTarget(ElementType target)
	{
		return this.targets == null || this.targets.isEmpty() || this.targets.contains(target);
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		// Add the java.lang.Annotation interface
		if (!this.theClass.isSubClassOf(Annotation.LazyFields.ANNOTATION))
		{
			this.theClass.getInterfaces().add(Annotation.LazyFields.ANNOTATION);
		}

		if (this.retention == null)
		{
			this.readRetention();
		}
		if (this.targets == null)
		{
			this.readTargets();
		}
	}

	private void readRetention()
	{
		final IAnnotation retention = this.theClass.getAnnotation(Annotation.LazyFields.RETENTION_CLASS);
		if (retention == null)
		{
			return;
		}

		final INamed value = (INamed) retention.getArguments().getValue(0, Annotation.VALUE);
		try
		{
			this.retention = RetentionPolicy.valueOf(value.getName().qualified);
		}
		catch (IllegalArgumentException ignored)
		{
			// Problematic RetentionPolicy annotation - do not handle this
		}
	}

	private void readTargets()
	{
		final IAnnotation target = this.theClass.getAnnotation(Annotation.LazyFields.TARGET_CLASS);
		if (target == null)
		{
			return;
		}

		this.targets = EnumSet.noneOf(ElementType.class);
		IValueList values = (IValueList) target.getArguments().getValue(0, Annotation.VALUE);
		if (values == null)
		{
			return;
		}

		int count = values.valueCount();
		for (int i = 0; i < count; i++)
		{
			final INamed value = (INamed) values.getValue(i);
			try
			{
				this.targets.add(ElementType.valueOf(value.getName().qualified));
			}
			catch (IllegalArgumentException ignored)
			{
				// Problematic Target annotation - do not handle this
			}
		}
	}

	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (IParameter parameter : this.theClass.getParameters())
		{
			final IValue value = parameter.getValue();
			if (value != null)
			{
				parameter.setValue(IValue.toAnnotationConstant(value, markers, context));
			}
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		for (IParameter parameter : this.theClass.getParameters())
		{
			final StringBuilder desc = new StringBuilder("()");
			parameter.getType().appendExtendedName(desc);

			final MethodVisitor methodVisitor = writer.visitMethod(Modifiers.PUBLIC | Modifiers.ABSTRACT,
			                                                       parameter.getInternalName(), desc.toString(), null,
			                                                       null);

			final IValue argument = parameter.getValue();
			if (argument != null && argument.isAnnotationConstant())
			{
				final AnnotationVisitor av = methodVisitor.visitAnnotationDefault();
				argument.writeAnnotationValue(av, parameter.getInternalName());
				av.visitEnd();
			}

			methodVisitor.visitEnd();
		}
	}
}
