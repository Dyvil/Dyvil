package dyvil.tools.compiler.ast.method;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;

public class NestedMethod extends Method
{
	private IType				thisType;
	private CaptureVariable[]	capturedFields;
	private int					capturedFieldCount;
	
	public transient IContext	context;
	
	public NestedMethod(IClass iclass)
	{
		super(iclass);
	}
	
	public NestedMethod(IClass iclass, Name name)
	{
		super(iclass, name);
	}
	
	public NestedMethod(IClass iclass, Name name, IType type)
	{
		super(iclass, name, type);
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IType getThisType()
	{
		return this.thisType = this.context.getThisType();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		
		IField match = this.context.resolveField(name);
		if (match == null)
		{
			return null;
		}
		
		if (!match.isVariable())
		{
			return match;
		}
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureVariable[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureVariable(match);
		}
		
		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			if (this.capturedFields[i].variable == match)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return match;
			}
		}
		
		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureVariable[] temp = new CaptureVariable[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureVariable(match);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		int modifiers = this.modifiers & 0xFFFF;
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified, this.getDescriptor(), this.getSignature(),
				this.getExceptions()));
		
		if (this.thisType != null)
		{
			mw.setInstanceMethod();
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.addAnnotation("Ljava/lang/Deprecated;", true);
		}
		
		int index = 0;
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable capture = this.capturedFields[i];
			capture.index = index;
			index = mw.registerParameter(index, capture.variable.getName().qualified, capture.variable.getType());
		}
		
		index = 0;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			index = mw.registerParameter(index, param.getName().qualified, param.getType());
			param.setIndex(index);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			this.value.writeExpression(mw);
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		if (this.thisType != null)
		{
			mw.writeLocal(0, "this", this.theClass.getType(), start, end);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getIndex(), param.getName().qualified, param.getType(), start, end);
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].variable.writeGet(writer, null);
		}
		
		super.writeCall(writer, instance, arguments, type);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].variable.writeGet(writer, null);
		}
		
		super.writeJump(writer, dest, instance, arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].variable.writeGet(writer, null);
		}
		
		super.writeInvJump(writer, dest, instance, arguments);
	}
}
