package dyvilx.tools.compiler.ast.annotation;

import dyvil.reflect.Modifiers;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.MethodVisitor;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.member.INamed;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.parsing.marker.MarkerList;

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

		final IValue value = retention.getArguments().get(0, Names.value);
		if (!(value instanceof EnumValue))
		{
			return;
		}

		try
		{
			final String name = ((EnumValue) value).getName().qualified;
			this.retention = RetentionPolicy.valueOf(name);
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

		final IValue argument = target.getArguments().get(0, Names.value);
		if (!(argument instanceof ArrayExpr))
		{
			return;
		}

		final ArgumentList values = ((ArrayExpr) argument).getValues();
		final int size = values.size();

		for (int i = 0; i < size; i++)
		{
			final INamed value = (INamed) values.get(i);
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
