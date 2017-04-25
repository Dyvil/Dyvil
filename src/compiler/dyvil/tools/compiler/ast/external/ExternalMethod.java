package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.AbstractMethod;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class ExternalMethod extends AbstractMethod implements IExternalCallableMember
{
	private static final int RETURN_TYPE = 1 << 1;
	private static final int PARAMETERS  = 1 << 2;
	private static final int EXCEPTIONS  = 1 << 3;
	private static final int THIS_TYPE   = 1 << 4;

	private byte resolved;

	public ExternalMethod(IClass enclosingClass, String name, String desc, String signature, ModifierSet modifiers)
	{
		super(enclosingClass, null, null, modifiers);
		this.name = Name.fromQualified(name);
		this.internalName = name;
		this.signature = signature;
		this.descriptor = desc;
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
			this.annotations.resolveTypes(null, Package.rootPackage, this);
		}
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
			int reifiedParameters = 0;
			for (int i = 0; i < this.typeParameters.size(); i++)
			{
				if (this.typeParameters.get(i).getReifiedKind() != null)
				{
					reifiedParameters++;
				}
			}
			this.parameters.remove(reifiedParameters);
		}
	}

	private void resolveExceptions()
	{
		if ((this.resolved & EXCEPTIONS) != 0)
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
	public IntrinsicData getIntrinsicData()
	{
		this.resolveAnnotations();
		return super.getIntrinsicData();
	}

	@Override
	public void setIntrinsicData(IntrinsicData intrinsicData)
	{
		this.intrinsicData = intrinsicData;
	}

	@Override
	public IParameter createParameter(SourcePosition position, Name name, IType type, ModifierSet modifiers,
		                                 AnnotationList annotations)
	{
		return new ExternalParameter(this, name, type, modifiers, annotations);
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
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
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
	public void writeCall(MethodWriter writer, IValue instance, ArgumentList arguments, ITypeContext typeContext,
		                     IType targetType, int lineNumber) throws BytecodeException
	{
		this.resolveAnnotations();
		super.writeCall(writer, instance, arguments, typeContext, targetType, lineNumber);
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, ArgumentList arguments,
		                     ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		this.resolveAnnotations();
		super.writeJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, ArgumentList arguments,
		                        ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		this.resolveAnnotations();
		super.writeInvJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
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
			if (!typeVar.addRawAnnotation(desc, annotation))
			{
				return null;
			}

			typeVar.getAnnotations().add(annotation);
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
