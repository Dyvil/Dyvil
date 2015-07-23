package dyvil.tools.compiler.ast.method;

import java.lang.annotation.ElementType;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.ApplyMethodCall;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class Constructor extends Member implements IConstructor
{
	protected IClass		theClass;
	
	protected IParameter[]	parameters	= new MethodParameter[3];
	protected int			parameterCount;
	protected IType[]		exceptions;
	protected int			exceptionCount;
	
	public IValue			value;
	
	protected IMethod		overrideMethod;
	
	public Constructor(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public void setTheClass(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public void setVarargs()
	{
		this.modifiers |= Modifiers.VARARGS;
	}
	
	@Override
	public boolean isVarargs()
	{
		return (this.modifiers & Modifiers.VARARGS) != 0;
	}
	
	// Parameters
	
	@Override
	public void setParameters(IParameter[] parameters, int parameterCount)
	{
		this.parameters = parameters;
		this.parameterCount = parameterCount;
	}
	
	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}
	
	@Override
	public void setParameter(int index, IParameter param)
	{
		param.setMethod(this);
		this.parameters[index] = param;
	}
	
	@Override
	public void addParameter(IParameter param)
	{
		param.setMethod(this);
		
		int index = this.parameterCount++;
		if (index >= this.parameters.length)
		{
			MethodParameter[] temp = new MethodParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = param;
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		return this.parameters[index];
	}
	
	@Override
	public IParameter[] getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		switch (type)
		{
		case "dyvil/annotation/inline":
			this.modifiers |= Modifiers.INLINE;
			return false;
		case "dyvil/annotation/sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "java/lang/Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.METHOD;
	}
	
	@Override
	public void exceptionCount()
	{
	}
	
	@Override
	public void setException(int index, IType exception)
	{
	}
	
	@Override
	public void addException(IType exception)
	{
		if (this.exceptions == null)
		{
			this.exceptions = new IType[3];
			this.exceptions[0] = exception;
			this.exceptionCount = 1;
			return;
		}
		
		int index = this.exceptionCount++;
		if (this.exceptionCount > this.exceptions.length)
		{
			IType[] temp = new IType[this.exceptionCount];
			System.arraycopy(this.exceptions, 0, temp, 0, index);
			this.exceptions = temp;
		}
		this.exceptions[index] = exception;
	}
	
	@Override
	public IType getException(int index)
	{
		return this.exceptions[index];
	}
	
	@Override
	public void setValue(IValue statement)
	{
		this.value = statement;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolve(markers, this, TypePosition.TYPE);
		}
		
		int index = 1;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(markers, this);
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
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, this);
		}
		
		this.type = this.theClass.getType();
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
		
		this.resolveSuperConstructors(markers, context);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
			
			IValue value1 = this.value.withType(Types.VOID, null, markers, context);
			if (value1 == null)
			{
				Marker marker = markers.create(this.position, "constructor.return");
				marker.addInfo("Expression Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
		}
	}
	
	private void resolveSuperConstructors(MarkerList markers, IContext context)
	{
		if (this.value.valueTag() != IValue.STATEMENT_LIST)
		{
			markers.add(this.position, "constructor.expression");
		}
		
		StatementList sl = (StatementList) this.value;
		if (sl.valueCount() > 0)
		{
			IValue first = sl.getValue(0);
			if (first.valueTag() == IValue.APPLY_CALL)
			{
				ApplyMethodCall amc = (ApplyMethodCall) first;
				int valueType = amc.instance.valueTag();
				if (valueType == IValue.SUPER)
				{
					IClass iclass = this.theClass.getSuperType().getTheClass();
					sl.setValue(0, this.initializer(amc.instance.getPosition(), markers, iclass, amc.arguments, true));
					return;
				}
				else if (valueType == IValue.THIS)
				{
					sl.setValue(0, this.initializer(amc.instance.getPosition(), markers, this.theClass, amc.arguments, false));
					return;
				}
			}
		}
		
		// Implicit Super Constructor
		IConstructor match = IContext.resolveConstructor(this.theClass.getSuperType(), EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(this.position, "constructor.super");
			return;
		}
		
		sl.addValue(0, new InitializerCall(this.position, match, EmptyArguments.INSTANCE));
	}
	
	private IValue initializer(ICodePosition position, MarkerList markers, IClass iclass, IArguments arguments, boolean isSuper)
	{
		IConstructor match = IContext.resolveConstructor(iclass, arguments);
		if (match == null)
		{
			Marker marker = markers.create(this.position, "resolve.constructor", iclass.getName().qualified);
			if (!arguments.isEmpty())
			{
				StringBuilder builder = new StringBuilder("Argument Types: ");
				arguments.typesToString(builder);
				marker.addInfo(builder.toString());
			}
			return new InitializerCall(position, null, arguments, isSuper);
		}
		
		return new InitializerCall(position, match, arguments, isSuper);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}
		
		if (this.value == null)
		{
			if ((this.modifiers & Modifiers.ABSTRACT) == 0)
			{
				this.modifiers |= Modifiers.ABSTRACT;
			}
			
			return;
		}
		
		this.value.checkTypes(markers, this);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType t = this.exceptions[i];
			if (!Types.THROWABLE.isSuperTypeOf(t))
			{
				Marker m = markers.create(t.getPosition(), "method.exception.type");
				m.addInfo("Exception Type: " + t);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0 && !this.theClass.isAbstract())
		{
			markers.add(this.position, "constructor.unimplemented", this.name);
		}
		
		if (this.isStatic())
		{
			markers.add(this.position, "constructor.static", this.name);
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
		
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(this, compilableList);
		}
		
		if (this.value != null)
		{
			this.value = this.value.cleanup(this, compilableList);
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.theClass.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.theClass.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return this.theClass.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.theClass.resolveTypeVariable(name);
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
		
		return this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.theClass.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		for (int i = 0; i < this.exceptionCount; i++)
		{
			if (this.exceptions[i].isSuperTypeOf(type))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public float getSignatureMatch(IArguments arguments)
	{
		int match = 1;
		int len = arguments.size();
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int parCount = this.parameterCount - 1;
			if (len <= parCount)
			{
				return 0;
			}
			
			float m;
			IParameter varParam = this.parameters[parCount];
			for (int i = 0; i < parCount; i++)
			{
				IParameter par = this.parameters[i];
				m = arguments.getTypeMatch(i, par);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			for (int i = parCount; i < len; i++)
			{
				m = arguments.getVarargsTypeMatch(i, varParam);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			return match;
		}
		else if (len > this.parameterCount)
		{
			return 0;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter par = this.parameters[i];
			float m = arguments.getTypeMatch(i, par);
			if (m == 0)
			{
				return 0;
			}
			match += m;
		}
		
		return match;
	}
	
	@Override
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IArguments arguments)
	{
		int len = arguments.size();
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			len = this.parameterCount - 1;
			arguments.checkVarargsValue(len, this.parameters[len], this.type, markers, context);
			
			for (int i = 0; i < len; i++)
			{
				arguments.checkValue(i, this.parameters[i], this.type, markers, context);
			}
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.checkValue(i, this.parameters[i], this.type, markers, context);
		}
	}
	
	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IArguments arguments)
	{
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			markers.add(position, "constructor.access.deprecated", this.theClass.getName());
		}
		
		switch (context.getThisClass().getVisibility(this))
		{
		case IContext.SEALED:
			markers.add(position, "constructor.access.sealed", this.theClass.getName());
			break;
		case IContext.INVISIBLE:
			markers.add(position, "constructor.access.invisible", this.theClass.getName());
			break;
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType type = this.exceptions[i];
			if (!Types.RUNTIME_EXCEPTION.isSuperTypeOf(type) && !context.handleException(type))
			{
				markers.add(position, "method.access.exception", type.toString());
			}
		}
	}
	
	@Override
	public String getDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].appendDescription(buffer);
		}
		buffer.append(")V");
		return buffer.toString();
	}
	
	@Override
	public String getSignature()
	{
		if (!this.theClass.isGeneric())
		{
			return null;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].appendSignature(buffer);
		}
		buffer.append(")V");
		return buffer.toString();
	}
	
	@Override
	public String[] getExceptions()
	{
		if (this.exceptionCount == 0)
		{
			return null;
		}
		
		String[] array = new String[this.exceptionCount];
		for (int i = 0; i < this.exceptionCount; i++)
		{
			array[i] = this.exceptions[i].getInternalName();
		}
		return array;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		this.write(writer, null);
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		int modifiers = this.modifiers & 0xFFFF;
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, "<init>", this.getDescriptor(), this.getSignature(), this.getExceptions()));
		
		mw.setThisType(this.theClass.getInternalName());
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.addAnnotation("Ldyvil/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.addAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
		{
			mw.addAnnotation("Ldyvil/annotation/sealed;", false);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(mw);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			
			if (instanceFields != null)
			{
				instanceFields.writeStatement(mw);
			}
			
			this.value.writeStatement(mw);
			mw.writeLabel(end);
			mw.end(Types.VOID);
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.writeLocal(0, "this", 'L' + this.theClass.getInternalName() + ';', null, start, end);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getIndex(), param.getName().qualified, param.getDescription(), param.getSignature(), start, end);
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber) throws BytecodeException
	{
		writer.writeTypeInsn(Opcodes.NEW, this.theClass.getInternalName());
		if (type != Types.VOID)
		{
			writer.writeInsn(Opcodes.DUP);
		}
		
		this.writeArguments(writer, arguments);
		this.writeInvoke(writer, lineNumber);
	}
	
	@Override
	public void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.writeLineNumber(lineNumber);
		
		String owner = this.theClass.getInternalName();
		String name = "<init>";
		String desc = this.getDescriptor();
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false);
	}
	
	@Override
	public void writeArguments(MethodWriter writer, IArguments arguments) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int len = this.parameterCount - 1;
			IParameter param;
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i];
				arguments.writeValue(i, param.getName(), param.getValue(), writer);
			}
			param = this.parameters[len];
			arguments.writeVarargsValue(len, param.getName(), param.getType(), writer);
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			arguments.writeValue(i, param.getName(), param.getValue(), writer);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(ModifierTypes.METHOD.toString(this.modifiers));
		buffer.append("new");
		
		buffer.append(Formatting.Method.parametersStart);
		Util.astToString(prefix, this.parameters, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
		buffer.append(Formatting.Method.parametersEnd);
		
		if (this.exceptionCount > 0)
		{
			buffer.append(Formatting.Method.signatureThrowsSeperator);
			Util.astToString(prefix, this.exceptions, this.exceptionCount, Formatting.Method.throwsSeperator, buffer);
		}
		
		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
