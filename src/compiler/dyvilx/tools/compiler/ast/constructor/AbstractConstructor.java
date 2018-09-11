package dyvilx.tools.compiler.ast.constructor;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.InitializerCall;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.member.AbstractMember;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public abstract class AbstractConstructor extends AbstractMember implements IConstructor, IDefaultContext
{
	// =============== Fields ===============

	protected @NonNull ParameterList parameters = new ParameterList(3);

	protected @Nullable TypeList exceptions;

	// --------------- Metadata ---------------

	protected IClass enclosingClass;

	// =============== Constructors ===============

	public AbstractConstructor(IClass enclosingClass)
	{
		super(Names.init, Types.VOID);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(IClass enclosingClass, AttributeList attributes)
	{
		super(Names.init, Types.VOID, attributes);
		this.enclosingClass = enclosingClass;
	}

	public AbstractConstructor(SourcePosition position, AttributeList attributes)
	{
		super(position, Names.init, Types.VOID, attributes);
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

	// - - - - - - - - Enclosing Class - - - - - - - -

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	// - - - - - - - - Parameters - - - - - - - -

	@Override
	public ParameterList getParameters()
	{
		return this.parameters;
	}

	// - - - - - - - - Exceptions - - - - - - - -

	@Override
	public TypeList getExceptions()
	{
		if (this.exceptions != null)
		{
			return this.exceptions;
		}
		return this.exceptions = new TypeList();
	}

	// - - - - - - - - Attributes - - - - - - - -

	@Override
	public ElementType getElementType()
	{
		return ElementType.METHOD;
	}

	@Override
	public boolean skipAnnotation(String type, Annotation annotation)
	{
		switch (type)
		{
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.attributes.addFlag(Modifiers.DEPRECATED);
			return false;
		}
		return false;
	}

	// --------------- Context ---------------

	// - - - - - - - - Header - - - - - - - -

	@Override
	public IHeaderUnit getHeader()
	{
		return this.enclosingClass.getHeader();
	}

	@Override
	public boolean isConstructor()
	{
		return true;
	}

	// - - - - - - - - Return Type - - - - - - - -

	@Override
	public IType getReturnType()
	{
		return Types.VOID;
	}

	// - - - - - - - - This - - - - - - - -

	@Override
	public boolean isThisAvailable()
	{
		return true;
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

	// - - - - - - - - Fields - - - - - - - -

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.get(name);
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

	// - - - - - - - - Exceptions - - - - - - - -

	@Override
	public byte checkException(IType type)
	{
		if (this.exceptions == null)
		{
			return FALSE;
		}

		for (int i = 0; i < this.exceptions.size(); i++)
		{
			if (Types.isSuperType(this.exceptions.get(i), type))
			{
				return TRUE;
			}
		}
		return FALSE;
	}

	// --------------- Constructor Matching ---------------

	@Override
	public void checkMatch(MatchList<IConstructor> list, ArgumentList arguments)
	{
		final int parameterCount = this.parameters.size();
		if (arguments == null)
		{
			list.add(new Candidate<>(this));
			return;
		}

		final int argumentCount = arguments.size();

		if (argumentCount > parameterCount && !this.isVariadic())
		{
			return;
		}

		final int[] matchValues = new int[argumentCount];
		final IType[] matchTypes = new IType[argumentCount];

		int defaults = 0;
		int varargs = 0;
		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			final int partialVarargs = arguments.checkMatch(matchValues, matchTypes, 0, i, parameter, list);

			switch (partialVarargs)
			{
			case ArgumentList.MISMATCH:
				return;
			case ArgumentList.DEFAULT:
				defaults++;
				continue;
			default:
				varargs += partialVarargs;
			}
		}

		for (int matchValue : matchValues)
		{
			if (matchValue == IValue.MISMATCH)
			{
				return;
			}
		}
		list.add(new Candidate<>(this, matchValues, matchTypes, defaults, varargs));
	}

	// --------------- Argument Checking ---------------

	@Override
	public IType checkArguments(MarkerList markers, SourcePosition position, IContext context, IType type,
		ArgumentList arguments)
	{
		final IClass theClass = this.enclosingClass;

		if (!theClass.isTypeParametric())
		{
			for (int i = 0, count = this.parameters.size(); i < count; i++)
			{
				arguments.checkValue(i, this.parameters.get(i), null, position, markers, context);
			}
			return type;
		}

		final IType classType = theClass.getThisType();
		final GenericData genericData = new GenericData(theClass);

		classType.inferTypes(type, genericData);
		genericData.lockAvailable();

		// Check Values and infer Types
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.checkValue(i, this.parameters.get(i), genericData, position, markers, context);
		}

		genericData.lockAvailable();

		// Check Type Var Inference and Compatibility
		final TypeParameterList typeParams = theClass.getTypeParameters();
		for (int i = 0, count = typeParams.size(); i < count; i++)
		{
			final ITypeParameter typeParameter = typeParams.get(i);
			final IType typeArgument = genericData.resolveType(typeParameter);

			if (typeArgument == null)
			{
				final IType inferredType = typeParameter.getUpperBound();
				markers.add(Markers.semantic(position, "constructor.typevar.infer", theClass.getName(),
				                             typeParameter.getName(), inferredType));
				genericData.addMapping(typeParameter, inferredType);
			}
			else if (!typeParameter.isAssignableFrom(typeArgument, genericData))
			{
				final Marker marker = Markers.semanticError(position, "constructor.typevar.incompatible",
				                                            theClass.getName(), typeParameter.getName());
				marker.addInfo(Markers.getSemantic("type.generic.argument", typeArgument));
				marker.addInfo(Markers.getSemantic("type_parameter.declaration", typeParameter));
				markers.add(marker);
			}
		}

		return classType.getConcreteType(genericData);
	}

	@Override
	public void checkCall(MarkerList markers, SourcePosition position, IContext context, ArgumentList arguments)
	{
		ModifierChecks.checkVisibility(this, position, markers, context);

		if (this.exceptions == null)
		{
			return;
		}

		for (int i = 0; i < this.exceptions.size(); i++)
		{
			IType exceptionType = this.exceptions.get(i);
			if (IContext.isUnhandled(context, exceptionType))
			{
				markers.add(Markers.semanticError(position, "exception.unhandled", exceptionType.toString()));
			}
		}
	}

	// --------------- Compilation ---------------

	// - - - - - - - - Descriptor and Signature - - - - - - - -

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

	// - - - - - - - - Exceptions - - - - - - - -

	@Override
	public String[] getInternalExceptions()
	{
		if (this.exceptions == null || this.exceptions.size() == 0)
		{
			return null;
		}

		return this.getExceptions().getInternalTypeNames();
	}

	// - - - - - - - - Call Compilation - - - - - - - -

	@Override
	public void writeCall(MethodWriter writer, ArgumentList arguments, IType type, int lineNumber)
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
	public void writeArguments(MethodWriter writer, ArgumentList arguments) throws BytecodeException
	{
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.writeValue(i, this.parameters.get(i), writer);
		}
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);
		buffer.append("init");

		this.parameters.toString(indent, buffer);

		final InitializerCall init = this.getInitializer();
		if (init != null)
		{
			Formatting.appendSeparator(buffer, "initializer.call.colon", ':');
			init.toString(indent, buffer);
		}

		if (this.exceptions != null && this.exceptions.size() > 0)
		{
			String throwsPrefix = indent;
			if (Formatting.getBoolean("constructor.throws.newline"))
			{
				throwsPrefix = Formatting.getIndent("constructor.throws.indent", indent);
				buffer.append('\n').append(throwsPrefix).append("throws ");
			}
			else
			{
				buffer.append(" throws ");
			}

			Util.astToString(throwsPrefix, this.exceptions.getTypes(), this.exceptions.size(),
			                 Formatting.getSeparator("constructor.throws", ','), buffer);
		}

		final IValue value = this.getValue();
		if (value != null && !Util.formatStatementList(indent, buffer, value))
		{
			buffer.append(" = ");
			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("constructor.semicolon"))
		{
			buffer.append(';');
		}
	}
}
