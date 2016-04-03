package dyvil.tools.compiler.ast.constructor;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.MapTypeContext;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
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

import java.lang.annotation.ElementType;

public abstract class AbstractConstructor extends Member implements IConstructor, IDefaultContext
{
	protected IParameter[] parameters = new IParameter[3];
	protected int parameterCount;

	protected IType[] exceptions;
	protected int     exceptionCount;

	// Metadata
	protected IClass enclosingClass;

	public AbstractConstructor(IClass enclosingClass)
	{
		super(Names.init, Types.VOID);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(IClass enclosingClass, ModifierSet modifiers)
	{
		super(Names.init, Types.VOID, modifiers);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
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
		return variable;
	}

	@Override
	public float getSignatureMatch(IArguments arguments)
	{
		int match = 1;
		int argumentCount = arguments.size();

		if (argumentCount > this.parameterCount && !this.isVariadic())
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
		final ITypeContext typeContext = new MapTypeContext();
		final IClass theClass = this.enclosingClass;

		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.inferType(i, this.parameters[i], typeContext);
		}

		for (int i = 0, count = theClass.typeParameterCount(); i < count; i++)
		{
			final ITypeParameter typeParameter = theClass.getTypeParameter(i);
			final IType typeArgument = typeContext.resolveType(typeParameter);

			if (typeArgument == null || typeArgument.getTypeVariable() == typeParameter)
			{
				final IType inferredType = typeParameter.getDefaultType();
				markers.add(Markers.semantic(position, "constructor.typevar.infer", theClass.getName(),
				                             typeParameter.getName(), inferredType));
				typeContext.addMapping(typeParameter, inferredType);
			}
			else if (!typeParameter.isAssignableFrom(typeArgument, typeContext))
			{
				final Marker marker = Markers.semanticError(position, "constructor.typevar.incompatible",
				                                            theClass.getName(), typeParameter.getName());
				marker.addInfo(Markers.getSemantic("generic.type", typeArgument));
				marker.addInfo(Markers.getSemantic("typeparameter.declaration", typeParameter));
				markers.add(marker);
			}
		}

		return theClass.getType().getConcreteType(typeContext);
	}

	@Override
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
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
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.writeValue(i, this.parameters[i], writer);
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

		final IValue value = this.getValue();
		if (value != null)
		{
			if (!Util.formatStatementList(prefix, buffer, value))
			{
				buffer.append(" = ");
				value.toString(prefix, buffer);
			}
		}

		if (Formatting.getBoolean("constructor.semicolon"))
		{
			buffer.append(';');
		}
	}
}
