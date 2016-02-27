package dyvil.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaptureHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class NestedMethod extends CodeMethod
{
	private CaptureHelper captureHelper = new CaptureHelper(CaptureVariable.FACTORY);
	
	public NestedMethod(ICodePosition position, Name name, IType type, ModifierSet modifierSet, AnnotationList annotations)
	{
		super(position, name, type, modifierSet, annotations);
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeParameter resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			ITypeParameter var = this.typeParameters[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		return null;
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.captureHelper.capture(variable);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		final float signatureMatch = this.getSignatureMatch(name, instance, arguments);
		if (signatureMatch > 0)
		{
			list.add(this, signatureMatch);
		}
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final int modifiers;
		if (this.captureHelper.isThisCaptured())
		{
			modifiers = Modifiers.PRIVATE;
		}
		else
		{
			modifiers = Modifiers.PRIVATE | Modifiers.STATIC;
		}
		
		MethodWriter mw = new MethodWriterImpl(writer,
		                                       writer.visitMethod(modifiers, this.name.qualified, this.getDescriptor(),
		                                                          this.getSignature(), this.getInternalExceptions()));

		this.writeAnnotations(mw, modifiers);

		int index = this.captureHelper.writeCaptureParameters(mw, 0);

		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.setLocalIndex(index);
			index = mw.registerParameter(index, param.getName().qualified, param.getType(), 0);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			this.value.writeExpression(mw, this.type);
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getLocalIndex(), param.getName().qualified, param.getDescription(),
			              param.getSignature(), start, end);
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, ITypeContext typeContext, IType targetType, int lineNumber)
			throws BytecodeException
	{
		this.captureHelper.writeCaptures(writer);
		super.writeCall(writer, instance, arguments, typeContext, targetType, lineNumber);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException
	{
		this.captureHelper.writeCaptures(writer);
		super.writeJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, ITypeContext typeContext, int lineNumber)
			throws BytecodeException
	{
		this.captureHelper.writeCaptures(writer);
		super.writeInvJump(writer, dest, instance, arguments, typeContext, lineNumber);
	}
}
