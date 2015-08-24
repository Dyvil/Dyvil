package dyvil.tools.compiler.ast.external;

import dyvil.reflect.Modifiers;
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
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.AbstractMethod;
import dyvil.tools.compiler.ast.method.IExternalMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationVisitorImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ExternalMethod extends AbstractMethod implements IExternalMethod
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	genericsResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalMethod(IClass iclass, Name name, String desc, int modifiers)
	{
		super(iclass, name);
		this.name = name;
		this.modifiers = modifiers;
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
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveParameters()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		this.parametersResolved = true;
		
		int index = (this.modifiers & Modifiers.STATIC) == 0 ? 1 : 0;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(null, this);
			param.setIndex(index);
			
			IType type = param.getType();
			if (type == Types.LONG || type == Types.DOUBLE)
			{
				index += 2;
			}
			else
			{
				index++;
			}
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
		return this.intrinsicOpcodes != null;
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
	public float getSignatureMatch(Name name, IValue instance, IArguments arguments)
	{
		if (name != this.name)
		{
			return 0;
		}
		
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getSignatureMatch(name, instance, arguments);
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
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type, int lineNumber) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeCall(writer, instance, arguments, type, lineNumber);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeJump(writer, dest, instance, arguments, lineNumber);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeInvJump(writer, dest, instance, arguments, lineNumber);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		int length = typePath.getLength();
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.METHOD_RETURN:
			this.type = IType.withAnnotation(this.type, annotation, typePath, 0, length);
		case TypeReference.METHOD_TYPE_PARAMETER:
		{
			ITypeVariable typeVar = this.generics[TypeReference.getTypeParameterIndex(typeRef)];
			if (typeVar.addRawAnnotation(desc, annotation))
			{
				return null;
			}
			
			typeVar.addAnnotation(annotation);
			break;
		}
		case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
		{
			ITypeVariable typeVar = this.generics[TypeReference.getTypeParameterIndex(typeRef)];
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		case TypeReference.EXCEPTION_PARAMETER:
		{
			int index = TypeReference.getExceptionIndex(typeRef);
			this.exceptions[index] = IType.withAnnotation(this.exceptions[index], annotation, typePath, 0, length);
			break;
		}
		case TypeReference.METHOD_FORMAL_PARAMETER:
		{
			int index = TypeReference.getFormalParameterIndex(typeRef);
			IParameter param = this.parameters[index];
			param.setType(IType.withAnnotation(param.getType(), annotation, typePath, 0, length));
		}
		}
		return new AnnotationVisitorImpl(null, annotation);
	}
}
