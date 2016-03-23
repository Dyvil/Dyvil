package dyvil.tools.compiler.ast.constructor;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
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

public class Constructor extends Member implements IConstructor, IDefaultContext
{
	protected IParameter[] parameters = new IParameter[3];
	protected int parameterCount;

	protected IType[] exceptions;
	protected int     exceptionCount;

	protected IValue          value;
	protected InitializerCall initializerCall;

	// Metadata
	protected IClass enclosingClass;

	public Constructor(IClass enclosingClass)
	{
		super(Names.init, Types.VOID);
		this.enclosingClass = enclosingClass;
	}

	public Constructor(IClass enclosingClass, ModifierSet modifiers)
	{
		super(Names.init, Types.VOID, modifiers);
		this.enclosingClass = enclosingClass;
	}

	public Constructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, Names.init, Types.VOID, modifiers, annotations);
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
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
	public void addParameterType(IType type)
	{
		final int parameterIndex = this.parameterCount;
		this.addParameter(new MethodParameter(Name.getQualified("par" + parameterIndex), type));
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

		if (parameter.isVarargs())
		{
			this.setVariadic();
		}

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
	public InitializerCall getInitializer()
	{
		return this.initializerCall;
	}

	@Override
	public void setInitializer(InitializerCall initializer)
	{
		this.initializerCall = initializer;
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue statement)
	{
		this.value = statement;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolveTypes(markers, context);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		context = context.push(this);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, context);
		}

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			final IValue typedValue = this.value.withType(Types.VOID, Types.VOID, markers, context);
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

		this.resolveSuperConstructors(markers);

		context.pop();
	}

	private void resolveSuperConstructors(MarkerList markers)
	{
		if (this.value.valueTag() == IValue.INITIALIZER_CALL)
		{
			this.initializerCall = (InitializerCall) this.value;
			this.value = null;
			return;
		}
		if (this.value.valueTag() == IValue.STATEMENT_LIST)
		{
			final StatementList statementList = (StatementList) this.value;
			if (statementList.valueCount() > 0)
			{
				final IValue firstValue = statementList.getValue(0);
				if (firstValue.valueTag() == IValue.INITIALIZER_CALL)
				{
					// We can't simply remove the value from the Statement List, so we replace it with a void statement
					statementList.setValue(0, new VoidValue(firstValue.getPosition()));

					this.initializerCall = (InitializerCall) firstValue;
					return;
				}
			}
		}

		// No Super Type -> don't try to resolve a Super Constructor
		final IType superType = this.enclosingClass.getSuperType();
		if (superType == null)
		{
			return;
		}

		// Implicit Super Constructor
		final IConstructor match = IContext.resolveConstructor(superType, EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(Markers.semantic(this.position, "constructor.super"));
			return;
		}

		this.initializerCall = new InitializerCall(this.position, match, EmptyArguments.INSTANCE, true);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, context, TypePosition.RETURN_TYPE);
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.checkTypes(markers, context);
		}

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.check(markers, context);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			final IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, context);

			if (!Types.isSuperType(Types.THROWABLE, exceptionType))
			{
				final Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
			}
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.checkNoError(markers, context);
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		else if (this.initializerCall != null)
		{
			markers.add(Markers.semanticError(this.position, "constructor.abstract"));
		}

		if (this.isStatic())
		{
			markers.add(Markers.semantic(this.position, "constructor.static", this.name));
		}

		context.pop();
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

		if (this.initializerCall != null)
		{
			this.initializerCall.foldConstants();
		}
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		context = context.push(this);

		super.cleanup(context, compilableList);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(context, compilableList);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(context, compilableList);
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.cleanup(context, compilableList);
		}
		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}

		context.pop();
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public IDyvilHeader getHeader()
	{
		return this.enclosingClass.getHeader();
	}

	@Override
	public IClass getThisClass()
	{
		return this.enclosingClass.getThisClass();
	}

	@Override
	public IType getThisType()
	{
		return this.enclosingClass.getThisType();
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
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}

	@Override
	public byte checkException(IType type)
	{
		for (int i = 0; i < this.exceptionCount; i++)
		{
			if (Types.isSuperType(this.exceptions[i], type))
			{
				return TRUE;
			}
		}
		return FALSE;
	}

	@Override
	public IType getReturnType()
	{
		return Types.VOID;
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
		return null;
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
		int typeVarCount = this.enclosingClass.typeParameterCount();
		ClassGenericType gt = new ClassGenericType(this.enclosingClass, new IType[typeVarCount], typeVarCount)
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
				                             this.enclosingClass.getTypeParameter(i).getName(),
				                             this.enclosingClass.getName()));
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
		ModifierUtil.checkVisibility(this, position, markers, context);

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
		if (!this.enclosingClass.isTypeParametric())
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
		final int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;
		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(modifiers, "<init>",
		                                                                                  this.getDescriptor(),
		                                                                                  this.getSignature(),
		                                                                                  this.getExceptions()));

		// Write Modifiers and Annotations
		ModifierUtil.writeModifiers(methodWriter, this.modifiers);

		if (this.annotations != null)
		{
			this.annotations.write(methodWriter);
		}

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			methodWriter.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}

		// Write Parameters
		methodWriter.setThisType(this.enclosingClass.getInternalName());
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeInit(methodWriter);
		}

		// Write Code
		final Label start = new Label();
		final Label end = new Label();

		methodWriter.visitCode();
		methodWriter.visitLabel(start);

		if (this.initializerCall != null)
		{
			this.initializerCall.writeExpression(methodWriter, Types.VOID);
		}

		if (this.initializerCall == null || this.initializerCall.isSuper())
		{
			this.enclosingClass.writeInit(methodWriter);
		}

		if (this.value != null)
		{
			this.value.writeExpression(methodWriter, Types.VOID);
		}

		methodWriter.visitLabel(end);
		methodWriter.visitEnd(Types.VOID);

		// Write Local Variable Data
		methodWriter.visitLocalVariable("this", 'L' + this.enclosingClass.getInternalName() + ';', null, start, end, 0);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeLocal(methodWriter, start, end);
		}
	}

	@Override
	public void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber)
		throws BytecodeException
	{
		writer.visitTypeInsn(Opcodes.NEW, this.enclosingClass.getInternalName());
		if (type != Types.VOID)
		{
			writer.visitInsn(Opcodes.DUP);
		}

		this.writeArguments(writer, arguments);
		this.writeInvoke(writer, lineNumber);
	}

	@Override
	public void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitLineNumber(lineNumber);

		String owner = this.enclosingClass.getInternalName();
		String name = "<init>";
		String desc = this.getDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false);
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
		buffer.append("init");

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
