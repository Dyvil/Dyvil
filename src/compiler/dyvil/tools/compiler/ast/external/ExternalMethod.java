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
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.AbstractMethod;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ExternalMethod extends AbstractMethod implements IExternalCallableMember
{
	private static final int ANNOTATIONS = 1;
	private static final int RETURN_TYPE = 1 << 1;
	private static final int GENERICS    = 1 << 2;
	private static final int PARAMETERS  = 1 << 3;
	private static final int EXCEPTIONS  = 1 << 4;

	private int resolved;

	public ExternalMethod(IClass enclosingClass, String name, String desc, String signature, ModifierSet modifiers)
	{
		super(enclosingClass, null, null, modifiers);

		final int index = name.indexOf(NAME_SEPARATOR);
		if (index >= 0)
		{
			this.name = Name.fromQualified(name.substring(0, index));
		}
		else
		{
			this.name = Name.fromQualified(name);
		}

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
		this.resolved |= ANNOTATIONS;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, this.getExternalContext(), this);
		}
	}

	private void resolveReturnType()
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}

		this.resolved |= RETURN_TYPE;
		this.type = this.type.resolveType(null, this.getExternalContext());
	}

	private void resolveGenerics()
	{
		this.resolved |= GENERICS;

		final IContext context = this.getExternalContext();
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(null, context);
		}
	}

	private void resolveParameters()
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}

		final IContext context = this.getExternalContext();

		this.resolved |= PARAMETERS;

		if (this.receiverType == null)
		{
			this.receiverType = this.enclosingClass.getType();
		}

		int parametersToRemove = 0;
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			if (this.typeParameters[i].getReifiedKind() != null)
			{
				parametersToRemove++;
			}
		}

		this.parameters.remove(parametersToRemove);

		if (this.receiverType != null)
		{
			this.receiverType = this.receiverType.resolveType(null, context);
		}
		else if (!this.isStatic())
		{
			this.receiverType = this.enclosingClass.getType();
		}
	}

	private void resolveExceptions()
	{
		this.resolved |= EXCEPTIONS;

		final IContext context = this.getExternalContext();
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(null, context);
		}
	}

	@Override
	public IType getType()
	{
		if ((this.resolved & RETURN_TYPE) == 0)
		{
			this.resolveReturnType();
		}
		return this.type;
	}

	@Override
	public boolean isIntrinsic()
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		return this.intrinsicData != null;
	}

	@Override
	public void setIntrinsicData(IntrinsicData intrinsicData)
	{
		this.intrinsicData = intrinsicData;
	}

	@Override
	public IParameter createParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new ExternalParameter(name, type, modifiers, annotations);
	}

	@Override
	public IParameterList getExternalParameterList()
	{
		return this.parameters;
	}

	@Override
	public IParameterList getParameterList()
	{
		if ((this.resolved & PARAMETERS) == 0)
		{
			this.resolveParameters();
		}
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
	protected boolean checkOverride0(IMethod candidate)
	{
		if ((this.resolved & PARAMETERS) == 0)
		{
			this.resolveParameters();
		}
		return false;
	}

	@Override
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue receiver, IArguments arguments, GenericData genericData)
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		if ((this.resolved & PARAMETERS) == 0)
		{
			this.resolveParameters();
		}
		return super.checkArguments(markers, position, context, receiver, arguments, genericData);
	}

	@Override
	public IType getException(int index)
	{
		if ((this.resolved & EXCEPTIONS) == 0)
		{
			this.resolveExceptions();
		}
		return this.exceptions[index];
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}

		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		return this.annotations.getAnnotation(type);
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
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, ITypeContext typeContext, IType targetType, int lineNumber)
		throws BytecodeException
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		super.writeCall(writer, instance, arguments, typeContext, targetType, lineNumber);
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
		throws BytecodeException
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		super.writeJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
		throws BytecodeException
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
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
			this.type = IType.withAnnotation(this.type, annotation, typePath, 0, typePath.getLength());
			break;
		case TypeReference.METHOD_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.typeParameters[TypeReference.getTypeParameterIndex(typeRef)];
			if (!typeVar.addRawAnnotation(desc, annotation))
			{
				return null;
			}

			typeVar.addAnnotation(annotation);
			break;
		}
		case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.typeParameters[TypeReference.getTypeParameterIndex(typeRef)];
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		case TypeReference.EXCEPTION_PARAMETER:
		{
			int index = TypeReference.getExceptionIndex(typeRef);
			this.exceptions[index] = IType.withAnnotation(this.exceptions[index], annotation, typePath, 0,
			                                              typePath.getLength());
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
