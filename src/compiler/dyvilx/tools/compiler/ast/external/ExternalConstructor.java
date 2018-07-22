package dyvilx.tools.compiler.ast.external;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.AbstractConstructor;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.InitializerCall;
import dyvilx.tools.compiler.ast.method.IExternalCallableMember;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.annotation.AnnotationReader;
import dyvilx.tools.parsing.marker.MarkerList;

public final class ExternalConstructor extends AbstractConstructor implements IExternalCallableMember
{
	private static final int EXCEPTIONS = 1 << 2;

	private byte resolved;

	public ExternalConstructor(IClass enclosingClass, AttributeList attributes)
	{
		super(enclosingClass, attributes);
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

	private void resolveExceptions()
	{
		if (this.exceptions == null || (this.resolved & EXCEPTIONS) != 0)
		{
			return;
		}

		this.resolved |= EXCEPTIONS;

		final IContext context = this.getExternalContext();
		this.exceptions.resolveTypes(null, context);
	}

	@Override
	public TypeList getExceptions()
	{
		this.resolveExceptions();
		return super.getExceptions();
	}

	@Override
	public TypeList getExternalExceptions()
	{
		return super.getExceptions();
	}

	@Override
	public IParameter createParameter(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new ExternalParameter(this, name, type, attributes);
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
	public String[] getInternalExceptions()
	{
		this.resolveExceptions();
		return super.getInternalExceptions();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
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
