package dyvil.tools.compiler.ast.method;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.member.Member;
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
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class Constructor extends Member implements IConstructor
{
	protected IClass theClass;
	
	protected IParameter[]	parameters	= new MethodParameter[3];
	protected int			parameterCount;
	protected IType[]		exceptions;
	protected int			exceptionCount;
	
	protected IValue value;
	
	public Constructor(IClass iclass)
	{
		this.theClass = iclass;
		this.type = iclass.getType();
	}
	
	public Constructor(IClass iclass, int modifiers)
	{
		this.theClass = iclass;
		this.type = iclass.getType();
		this.modifiers = modifiers;
	}
	
	public Constructor(ICodePosition position, IClass iclass, int modifiers)
	{
		this.position = position;
		this.theClass = iclass;
		this.type = iclass.getType();
		this.modifiers = modifiers;
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
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case "dyvil/annotation/inline":
			this.modifiers |= Modifiers.INLINE;
			return false;
		case "dyvil/annotation/internal":
			this.modifiers |= Modifiers.INTERNAL;
			return false;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers |= Modifiers.DEPRECATED;
			return true;
		}
		return true;
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.METHOD;
	}
	
	@Override
	public int exceptionCount()
	{
		return this.exceptionCount;
	}
	
	@Override
	public void setException(int index, IType exception)
	{
		this.exceptions[index] = exception;
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
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(markers, this);
			param.setIndex(i);
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
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, this);
		}
		
		this.resolveSuperConstructors(markers, context);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
			
			IValue value1 = this.value.withType(Types.VOID, null, markers, context);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.position, "constructor.return");
				marker.addInfo("Expression Type: " + this.value.getType());
				markers.add(marker);
			}
			else
			{
				this.value = value1;
			}
		}
	}
	
	private void resolveSuperConstructors(MarkerList markers, IContext context)
	{
		if (this.value.valueTag() == IValue.INITIALIZER_CALL)
		{
			return;
		}
		if (this.value.valueTag() == IValue.STATEMENT_LIST)
		{
			StatementList sl = (StatementList) this.value;
			int count = sl.valueCount();
			for (int i = 0; i < count; i++)
			{
				if (sl.getValue(i).valueTag() == IValue.INITIALIZER_CALL)
				{
					return;
				}
			}
		}
		
		// Implicit Super Constructor
		IConstructor match = IContext.resolveConstructor(this.theClass.getSuperType(), EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(I18n.createMarker(this.position, "constructor.super"));
			return;
		}
		
		this.value = Util.prependValue(new InitializerCall(this.position, match, EmptyArguments.INSTANCE, true), this.value);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, this, TypePosition.RETURN_TYPE);
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
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, ElementType.CONSTRUCTOR);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].check(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType t = this.exceptions[i];
			if (!Types.THROWABLE.isSuperTypeOf(t))
			{
				Marker m = I18n.createMarker(t.getPosition(), "method.exception.type");
				m.addInfo("Exception Type: " + t);
				markers.add(m);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0 && !this.theClass.isAbstract())
		{
			markers.add(I18n.createMarker(this.position, "constructor.unimplemented", this.name));
		}
		
		if (this.isStatic())
		{
			markers.add(I18n.createMarker(this.position, "constructor.static", this.name));
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].foldConstants();
		}
		
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(this, compilableList);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(this, compilableList);
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
	public IAccessible getAccessibleThis(IClass type)
	{
		return this.theClass.getAccessibleThis(type);
	}
	
	@Override
	public IAccessible getAccessibleImplicit()
	{
		return null;
	}
	
	@Override
	public boolean isMember(IVariable variable)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameters[i] == variable)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}
		return this.theClass.capture(variable);
	}
	
	@Override
	public float getSignatureMatch(IArguments arguments)
	{
		int match = 1;
		int argumentCount = arguments.size();
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int parCount = this.parameterCount - 1;
			if (argumentCount <= parCount)
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
			for (int i = parCount; i < argumentCount; i++)
			{
				m = arguments.getVarargsTypeMatch(i, varParam);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			return match + AbstractMethod.VARARGS_MATCH;
		}
		else if (argumentCount > this.parameterCount)
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
	public IType checkGenericType(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
		int typeVarCount = this.theClass.genericCount();
		ClassGenericType gt = new ClassGenericType(this.theClass, new IType[typeVarCount], typeVarCount)
		{
			@Override
			public void addMapping(ITypeVariable typeVar, IType type)
			{
				int index = typeVar.getIndex();
				this.typeArguments[index] = type;
			}
		};
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int index = this.parameterCount - 1;
			for (int i = 0; i < index; i++)
			{
				arguments.inferType(i, this.parameters[i], gt);
			}
			arguments.inferVarargsType(index, this.parameters[index], gt);
		}
		else
		{
			for (int i = 0; i < this.parameterCount; i++)
			{
				arguments.inferType(i, this.parameters[i], gt);
			}
		}
		
		for (int i = 0; i < typeVarCount; i++)
		{
			if (gt.getType(i) == null)
			{
				gt.setType(i, Types.ANY);
				
				markers.add(I18n.createMarker(position, "constructor.typevar.infer", this.theClass.getTypeVariable(i).getName(), this.theClass.getName()));
			}
		}
		
		return gt;
	}
	
	@Override
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
		int len = arguments.size();
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			len = this.parameterCount - 1;
			arguments.checkVarargsValue(len, this.parameters[len], type, markers, context);
			
			for (int i = 0; i < len; i++)
			{
				arguments.checkValue(i, this.parameters[i], type, markers, context);
			}
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.checkValue(i, this.parameters[i], type, markers, context);
		}
	}
	
	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IArguments arguments)
	{
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			Deprecation.checkDeprecation(markers, position, this, "constructor");
		}
		
		switch (IContext.getVisibility(context, this))
		{
		case IContext.INTERNAL:
			markers.add(I18n.createMarker(position, "constructor.access.internal", this.theClass.getName()));
			break;
		case IContext.INVISIBLE:
			markers.add(I18n.createMarker(position, "constructor.access.invisible", this.theClass.getName()));
			break;
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType type = this.exceptions[i];
			if (!Types.RUNTIME_EXCEPTION.isSuperTypeOf(type) && !context.handleException(type))
			{
				markers.add(I18n.createMarker(position, "method.access.exception", type.toString()));
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
		MethodWriter mw = new MethodWriterImpl(writer,
				writer.visitMethod(modifiers, "<init>", this.getDescriptor(), this.getSignature(), this.getExceptions()));
				
		mw.setThisType(this.theClass.getInternalName());
		
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.visitAnnotation("Ldyvil/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
		if ((this.modifiers & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			mw.visitAnnotation("Ldyvil/annotation/internal;", false);
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
			mw.writeLocal(param.getLocalIndex(), param.getName().qualified, param.getDescription(), param.getSignature(), start, end);
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
				arguments.writeValue(i, param, writer);
			}
			param = this.parameters[len];
			arguments.writeVarargsValue(len, param, writer);
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			arguments.writeValue(i, param, writer);
		}
	}
	
	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType.writeType(this.parameters[i].getType(), out);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		this.writeAnnotations(out);
		
		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(out);
		}
	}
	
	@Override
	public void readSignature(DataInput in) throws IOException
	{
		int parameterCount = in.readByte();
		if (this.parameterCount != 0)
		{
			for (int i = 0; i < parameterCount; i++)
			{
				this.parameters[i].setType(IType.readType(in));
			}
			this.parameterCount = parameterCount;
			return;
		}
		
		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			this.parameters[i] = new MethodParameter(Name.getQualified("par" + i), IType.readType(in));
		}
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);
		
		int parameterCount = in.readByte();
		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			MethodParameter param = new MethodParameter();
			param.read(in);
			this.parameters[i] = param;
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
