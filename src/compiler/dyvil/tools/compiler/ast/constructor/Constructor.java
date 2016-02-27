package dyvil.tools.compiler.ast.constructor;

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
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
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
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class Constructor extends Member implements IConstructor
{
	protected IClass theClass;
	
	protected IParameter[] parameters = new MethodParameter[3];
	protected int     parameterCount;
	protected IType[] exceptions;
	protected int     exceptionCount;
	
	protected IValue value;
	
	public Constructor(IClass iclass)
	{
		super(Names.init, Types.VOID);
		this.theClass = iclass;
	}
	
	public Constructor(IClass iclass, ModifierSet modifiers)
	{
		this(null, iclass, modifiers);
	}
	
	public Constructor(ICodePosition position, IClass iclass, ModifierSet modifiers)
	{
		super(Names.init, Types.VOID, modifiers);
		this.position = position;
		this.theClass = iclass;
	}
	
	@Override
	public void setEnclosingClass(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IClass getEnclosingClass()
	{
		return this.theClass;
	}
	
	@Override
	public void setVariadic()
	{
		this.modifiers.addIntModifier(Modifiers.VARARGS);
	}
	
	@Override
	public boolean isVariadic()
	{
		return this.modifiers.hasIntModifier(Modifiers.VARARGS);
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
	public void setParameter(int index, IParameter parameter)
	{
		parameter.setMethod(this);
		parameter.setIndex(index);
		this.parameters[index] = parameter;
	}
	
	@Override
	public void addParameter(IParameter parameter)
	{
		parameter.setMethod(this);
		
		final int index = this.parameterCount++;

		parameter.setIndex(index);

		if (index >= this.parameters.length)
		{
			IParameter[] temp = new IParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = parameter;
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
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
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
			this.parameters[i].resolveTypes(markers, this);
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
		
		this.resolveSuperConstructors(markers);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
			
			final IValue typedValue = this.value.withType(Types.VOID, null, markers, context);
			if (typedValue == null)
			{
				Marker marker = Markers.semantic(this.position, "constructor.return.type");
				marker.addInfo(Markers.getSemantic("return.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = typedValue;
			}
		}
	}
	
	private void resolveSuperConstructors(MarkerList markers)
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
		final IConstructor match = IContext.resolveConstructor(this.theClass.getSuperType(), EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(Markers.semantic(this.position, "constructor.super"));
			return;
		}
		
		this.value = Util
				.prependValue(new InitializerCall(this.position, match, EmptyArguments.INSTANCE, true), this.value);
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
		
		if (this.value != null)
		{
			this.value.checkTypes(markers, this);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "constructor.abstract"));
		}
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
			IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, context);

			if (!Types.THROWABLE.isSuperTypeOf(exceptionType))
			{
				Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		else if (!this.modifiers.hasIntModifier(Modifiers.ABSTRACT) && !this.theClass.isAbstract())
		{
			markers.add(Markers.semantic(this.position, "constructor.unimplemented", this.name));
		}
		
		if (this.isStatic())
		{
			markers.add(Markers.semantic(this.position, "constructor.static", this.name));
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
	public IType getThisType()
	{
		return this.theClass.getThisType();
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
	public ITypeParameter resolveTypeVariable(Name name)
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
	public boolean canReturn(IType type)
	{
		return type == Types.VOID;
	}

	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		return this.theClass.getAccessibleThis(type);
	}
	
	@Override
	public IValue getImplicit()
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
		
		if (this.modifiers.hasIntModifier(Modifiers.VARARGS))
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

			m = arguments.getVarargsTypeMatch(parCount, varParam);
			if (m == 0)
			{
				return 0;
			}
			return m + match;
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
		int typeVarCount = this.theClass.typeParameterCount();
		ClassGenericType gt = new ClassGenericType(this.theClass, new IType[typeVarCount], typeVarCount)
		{
			@Override
			public void addMapping(ITypeParameter typeVar, IType type)
			{
				int index = typeVar.getIndex();
				this.typeArguments[index] = type;
			}
		};
		
		if (this.modifiers.hasIntModifier(Modifiers.VARARGS))
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
				
				markers.add(Markers.semantic(position, "constructor.typevar.infer",
				                             this.theClass.getTypeParameter(i).getName(), this.theClass.getName()));
			}
		}
		
		return gt;
	}
	
	@Override
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
		if (this.modifiers.hasIntModifier(Modifiers.VARARGS))
		{
			int len = this.parameterCount - 1;
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
		Deprecation.checkAnnotations(markers, position, this, "constructor");

		switch (IContext.getVisibility(context, this))
		{
		case INTERNAL:
			markers.add(Markers.semantic(position, "constructor.access.internal", this.theClass.getName()));
			break;
		case INVISIBLE:
			markers.add(Markers.semantic(position, "constructor.access.invisible", this.theClass.getName()));
			break;
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			if (IContext.isUnhandled(context, exceptionType))
			{
				markers.add(Markers.semantic(position, "exception.unhandled", exceptionType.toString()));
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
		if (!this.theClass.isTypeParametric())
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
		int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, "<init>", this.getDescriptor(),
		                                                                  this.getSignature(), this.getExceptions()));

		mw.setThisType(this.theClass.getInternalName());
		
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(mw);
		}
		
		Label start = new Label();
		Label end = new Label();

		mw.begin();
		mw.writeLabel(start);

		if (this.value != null)
		{
			this.value.writeExpression(mw, Types.VOID);
		}
		this.theClass.writeInit(mw);

		mw.writeLabel(end);
		mw.end(Types.VOID);

		if ((modifiers & Modifiers.STATIC) == 0)
		{
			mw.writeLocal(0, "this", 'L' + this.theClass.getInternalName() + ';', null, start, end);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getLocalIndex(), param.getName().qualified, param.getDescription(),
			              param.getSignature(), start, end);
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber)
			throws BytecodeException
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
		if (this.modifiers.hasIntModifier(Modifiers.VARARGS))
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

		this.modifiers.toString(buffer);
		buffer.append("new");
		
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');
		Util.astToString(prefix, this.parameters, this.parameterCount,
		                 Formatting.getSeparator("parameters.separator", ','), buffer);
		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
		
		if (this.exceptionCount > 0)
		{
			String throwsPrefix = prefix;
			if (Formatting.getBoolean("constructor.throws.newline"))
			{
				throwsPrefix = Formatting.getIndent("constructor.throws.indent", prefix);
				buffer.append('\n').append(throwsPrefix).append("throws ");
			}
			else
			{
				buffer.append(" throws ");
			}

			Util.astToString(throwsPrefix, this.exceptions, this.exceptionCount,
			                 Formatting.getSeparator("constructor.throws", ','), buffer);
		}
		
		if (this.value != null)
		{
			Util.formatStatementList(prefix, buffer, this.value);
		}

		if (Formatting.getBoolean("constructor.semicolon"))
		{
			buffer.append(';');
		}
	}
}
