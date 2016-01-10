package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.AbstractMethod;
import dyvil.tools.compiler.ast.method.IExternalMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
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

public final class ExternalMethod extends AbstractMethod implements IExternalMethod
{
	private boolean annotationsResolved;
	private boolean returnTypeResolved;
	private boolean genericsResolved;
	private boolean parametersResolved;
	private boolean exceptionsResolved;
	
	public ExternalMethod(IClass iclass, Name name, String desc, ModifierSet modifiers)
	{
		super(iclass, name, null, modifiers);
		this.name = name;
		this.descriptor = desc;
	}
	
	public void setVarargsParameter()
	{
		this.parameters[this.parameterCount - 1].setVarargs(true);
	}
	
	@Override
	public IParameter getParameter_(int index)
	{
		return this.parameters[index];
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, Package.rootPackage, this);
		}
	}
	
	private void resolveReturnType()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		this.returnTypeResolved = true;
		this.type = this.type.resolveType(null, this);
	}
	
	private void resolveGenerics()
	{
		this.genericsResolved = true;
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveParameters()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		this.parametersResolved = true;

		if (this.receiverType == null)
		{
			this.receiverType = this.theClass.getType();
		}

		int parametersToRemove = 0;
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			if (this.typeParameters[i].getReifiedKind() != ITypeParameter.ReifiedKind.NOT_REIFIED)
			{
				parametersToRemove++;
			}
		}

		this.parameterCount -= parametersToRemove;
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, this);
		}
	}
	
	private void resolveExceptions()
	{
		this.exceptionsResolved = true;
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(null, this);
		}
	}
	
	@Override
	public IType getType()
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		return this.type;
	}
	
	@Override
	public boolean isIntrinsic()
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return this.intrinsicData != null;
	}
	
	@Override
	protected boolean isObjectMethod()
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.isObjectMethod();
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return this.parameters[index];
	}
	
	@Override
	public float getSignatureMatch(Name name, IValue receiver, IArguments arguments)
	{
		if (name != this.name)
		{
			return 0;
		}

		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getSignatureMatch(name, receiver, arguments);
	}

	@Override
	public boolean checkOverride(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext)
	{
		if (this.name != candidate.getName())
		{
			return false;
		}

		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.checkOverride(markers, iclass, candidate, typeContext);
	}

	@Override
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.checkArguments(markers, position, context, instance, arguments, typeContext);
	}
	
	@Override
	public GenericData getGenericData(GenericData genericData, IValue instance, IArguments arguments)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getGenericData(genericData, instance, arguments);
	}
	
	@Override
	public IType getException(int index)
	{
		if (!this.exceptionsResolved)
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
		
		if (!this.annotationsResolved)
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
	protected void addOverride(IMethod override)
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
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeCall(writer, instance, arguments, typeContext, targetType, lineNumber);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException
	{
		if (!this.annotationsResolved)
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
			this.exceptions[index] = IType
					.withAnnotation(this.exceptions[index], annotation, typePath, 0, typePath.getLength());
			break;
		}
		case TypeReference.METHOD_FORMAL_PARAMETER:
		{
			int index = TypeReference.getFormalParameterIndex(typeRef);
			IParameter param = this.parameters[index];
			param.setType(IType.withAnnotation(param.getType(), annotation, typePath, 0, typePath.getLength()));
			break;
		}
		}
		return new AnnotationReader(null, annotation);
	}
}
