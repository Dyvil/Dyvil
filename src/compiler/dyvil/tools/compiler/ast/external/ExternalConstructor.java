package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.AbstractConstructor;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.structure.RootPackage;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class ExternalConstructor extends AbstractConstructor implements IExternalCallableMember
{
	private static final int EXCEPTIONS  = 1 << 2;

	private byte resolved;

	public ExternalConstructor(IClass enclosingClass)
	{
		super(enclosingClass);
		this.type = enclosingClass.getThisType();
	}

	@Override
	public void setIntrinsicData(IntrinsicData intrinsicData)
	{
	}

	@Override
	public InitializerCall getInitializer()
	{
		return null;
	}

	@Override
	public void setInitializer(InitializerCall initializer)
	{
	}

	@Override
	public IValue getValue()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{
	}

	@Override
	public IContext getExternalContext()
	{
		return new CombiningContext(this, new CombiningContext(this.enclosingClass, Package.rootPackage));
	}

	private void resolveAnnotations()
	{
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, RootPackage.rootPackage, this);
		}
	}

	private void resolveExceptions()
	{
		this.resolved |= EXCEPTIONS;

		final IContext context = this.getExternalContext();
		this.exceptions.resolveTypes(null, context);
	}

	@Override
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
	}

	@Override
	public IParameter createParameter(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new ExternalParameter(this, name, type, modifiers, annotations);
	}

	@Override
	public ParameterList getExternalParameterList()
	{
		return this.parameters;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.EXCEPTION_PARAMETER:
		{
			int index = TypeReference.getExceptionIndex(typeRef);
			this.exceptions.set(index, IType.withAnnotation(this.exceptions.get(index), annotation, typePath));
			break;
		}
		case TypeReference.METHOD_FORMAL_PARAMETER:
		{
			final int index = TypeReference.getFormalParameterIndex(typeRef);
			final ExternalParameter parameter = (ExternalParameter) this.parameters.get(index);
			parameter.addTypeAnnotation(annotation, typePath);
			break;
		}
		}
		return new AnnotationReader(null, annotation);
	}
}
