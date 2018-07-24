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
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.method.AbstractMethod;
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

public final class ExternalMethod extends AbstractMethod implements IExternalCallableMember
{
	private static final int RETURN_TYPE = 1 << 1;
	private static final int PARAMETERS  = 1 << 2;
	private static final int EXCEPTIONS  = 1 << 3;
	private static final int THIS_TYPE   = 1 << 4;

	private byte resolved;

	public ExternalMethod(IClass enclosingClass, String name, String desc, String signature)
	{
		super(enclosingClass, Name.fromQualified(name));
		this.internalName = name;
		this.signature = signature;
		this.descriptor = desc;
	}

	@Override
	public IContext getExternalContext()
	{
		return new CombiningContext(this, new CombiningContext(this.enclosingClass, Package.rootPackage));
	}

	private void resolveReturnType()
	{
		if ((this.resolved & RETURN_TYPE) != 0)
		{
			return;
		}

		this.resolved |= RETURN_TYPE;
		this.type = this.type.resolveType(null, this.getExternalContext());
	}

	private void resolveParameters()
	{
		if ((this.resolved & PARAMETERS) != 0)
		{
			return;
		}

		this.resolved |= PARAMETERS;

		if (this.typeParameters != null)
		{
			for (int i = this.typeParameters.size() - 1; i >= 0; i--)
			{
				final ITypeParameter typeParameter = this.typeParameters.get(i);
				if (typeParameter.getReifiedKind() != null)
				{
					typeParameter.setReifyParameter(this.parameters.removeLast());
				}
			}
		}
	}

	private void resolveExceptions()
	{
		if (this.exceptions == null || (this.resolved & EXCEPTIONS) != 0)
		{
			return;
		}

		this.resolved |= EXCEPTIONS;

		if (this.exceptions != null)
		{
			final IContext context = this.getExternalContext();
			this.exceptions.resolveTypes(null, context);
		}
	}

	@Override
	public IType getType()
	{
		this.resolveReturnType();
		return this.type;
	}

	@Override
	public void setIntrinsicData(IntrinsicData intrinsicData)
	{
		this.intrinsicData = intrinsicData;
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
	public ParameterList getParameters()
	{
		this.resolveParameters();
		return this.parameters;
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
	public IType getThisType()
	{
		if ((this.resolved & THIS_TYPE) == 0 && this.thisType != null)
		{
			this.resolved |= THIS_TYPE;
			final IType type = this.thisType.resolveType(null, Package.rootPackage);
			this.setThisType(type);
			return type;
		}
		return super.getThisType();
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return Package.rootPackage.resolveClass(name);
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
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		if (typePath == null)
		{
			typePath = TypePath.EMPTY;
		}

		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.METHOD_RETURN:
			this.type = IType.withAnnotation(this.type, annotation, typePath);
			break;
		case TypeReference.METHOD_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			if (typeVar.skipAnnotation(annotation.getTypeDescriptor(), annotation))
			{
				return null;
			}

			typeVar.getAttributes().add(annotation);
			break;
		}
		case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
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
