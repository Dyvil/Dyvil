package dyvil.tools.compiler.ast.external;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IntValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class ExternalMethod extends Method
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	genericsResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalMethod(IClass iclass)
	{
		super(iclass);
	}
	
	public void setVarargsParameter()
	{
		this.parameters[this.parameterCount - 1].setVarargs(true);
	}
	
	public void setParameterName(int index, Name name)
	{
		this.parameters[index].setName(name);
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation annotation = this.annotations[i];
			annotation.resolveTypes(null, Package.rootPackage);
			
			if (annotation.type.getTheClass() != Types.INTRINSIC_CLASS)
			{
				continue;
			}
			
			try
			{
				Array array = (Array) annotation.arguments.getValue(0, Annotation.VALUE);
				
				int len = array.valueCount();
				int[] opcodes = new int[len];
				for (int j = 0; j < len; j++)
				{
					IntValue v = (IntValue) array.getValue(j);
					opcodes[j] = v.value;
				}
				this.intrinsicOpcodes = opcodes;
			}
			catch (NullPointerException | ClassCastException ex)
			{
			}
		}
	}
	
	private void resolveReturnType()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		this.returnTypeResolved = true;
		this.type = this.type.resolve(null, this, TypePosition.RETURN_TYPE);
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
			this.exceptions[i] = this.exceptions[i].resolve(null, this, TypePosition.TYPE);
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
	public Annotation getAnnotation(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return this.annotations[index];
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return super.getAnnotation(type);
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
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeCall(writer, instance, arguments, type);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeJump(writer, dest, instance, arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeInvJump(writer, dest, instance, arguments);
	}
}
