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
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
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
	protected ParameterList parameters = new ParameterList(3);

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
	public IParameterList getParameterList()
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
		return this.parameters.resolveParameter(name);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
		return this.parameters.isParameter(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		return variable;
	}

	@Override
	public void checkMatch(MatchList<IConstructor> list, IArguments arguments)
	{
		final int parameterCount = this.parameters.size();
		final int argumentCount = arguments.size();

		if (argumentCount > parameterCount && !this.isVariadic())
		{
			return;
		}

		final double[] totalMatch = new double[argumentCount];
		int defaults = 0;
		int varargs = 0;
		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			final int partialVarargs = arguments.checkMatch(totalMatch, 0, i, parameter, list);

			if (partialVarargs >= 0)
			{
				varargs += partialVarargs;
				continue;
			}
			if (parameter.getValue() != null)
			{
				defaults++;
				continue;
			}

			return; // Mismatch
		}

		list.add(new MatchList.Candidate<>(this, defaults, varargs, totalMatch));
	}

	@Override
	public IType checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
		final IClass theClass = this.enclosingClass;

		if (!theClass.isTypeParametric())
		{
			for (int i = 0, count = this.parameters.size(); i < count; i++)
			{
				arguments.checkValue(i, this.parameters.get(i), null, markers, context);
			}
			return type;
		}

		final IType theClassType = theClass.getType();
		final GenericData genericData = new GenericData(theClass);

		theClassType.inferTypes(type, genericData);
		genericData.lock(genericData.typeCount());

		// Check Values and infer Types
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.checkValue(i, this.parameters.get(i), genericData, markers, context);
		}

		genericData.lock(genericData.typeCount());

		// Check Type Var Inference and Compatibility
		for (int i = 0, count = theClass.typeParameterCount(); i < count; i++)
		{
			final ITypeParameter typeParameter = theClass.getTypeParameter(i);
			final IType typeArgument = genericData.resolveType(typeParameter);

			if (typeArgument == null || typeArgument.getTypeVariable() == typeParameter)
			{
				final IType inferredType = typeParameter.getDefaultType();
				markers.add(Markers.semantic(position, "constructor.typevar.infer", theClass.getName(),
				                             typeParameter.getName(), inferredType));
				genericData.addMapping(typeParameter, inferredType);
			}
			else if (!typeParameter.isAssignableFrom(typeArgument, genericData))
			{
				final Marker marker = Markers.semanticError(position, "constructor.typevar.incompatible",
				                                            theClass.getName(), typeParameter.getName());
				marker.addInfo(Markers.getSemantic("generic.type", typeArgument));
				marker.addInfo(Markers.getSemantic("typeparameter.declaration", typeParameter));
				markers.add(marker);
			}
		}

		return theClassType.getConcreteType(genericData);
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
		this.parameters.appendDescriptor(buffer);
		buffer.append(")V");
		return buffer.toString();
	}

	@Override
	public String getSignature()
	{
		if (!this.enclosingClass.isTypeParametric() && !this.parameters.needsSignature())
		{
			return null;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		this.parameters.appendSignature(buffer);
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
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.writeValue(i, this.parameters.get(i), writer);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		buffer.append("init");

		this.parameters.toString(prefix, buffer);

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
