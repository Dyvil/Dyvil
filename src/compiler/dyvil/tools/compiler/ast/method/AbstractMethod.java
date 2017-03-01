package dyvil.tools.compiler.ast.method;

import dyvil.annotation.Mutating;
import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Handle;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.external.ExternalMethod;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;

public abstract class AbstractMethod extends Member implements IMethod, ILabelContext, IDefaultContext
{
	protected static final Handle EXTENSION_BSM = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/DynamicLinker",
	                                                         "linkExtension",
	                                                         ClassFormat.BSM_HEAD + "Ljava/lang/invoke/MethodHandle;"
		                                                         + ClassFormat.BSM_TAIL);

	protected static final Handle STATICVIRTUAL_BSM = new Handle(ClassFormat.H_INVOKESTATIC,
	                                                             "dyvil/runtime/DynamicLinker", "linkClassMethod",
	                                                             ClassFormat.BSM_HEAD + ClassFormat.BSM_TAIL);

	// --------------------------------------------------

	protected ITypeParameter[] typeParameters;
	protected int              typeParameterCount;

	protected IType receiverType;

	protected ParameterList parameters = new ParameterList();

	protected IType[] exceptions;
	protected int     exceptionCount;

	// Metadata
	protected IClass        enclosingClass;
	protected String        internalName;
	protected String        descriptor;
	protected String        signature;
	protected IntrinsicData intrinsicData;

	public AbstractMethod(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	public AbstractMethod(IClass enclosingClass, Name name)
	{
		this.enclosingClass = enclosingClass;
		this.name = name;
	}

	public AbstractMethod(IClass enclosingClass, Name name, IType type)
	{
		this.enclosingClass = enclosingClass;
		this.type = type;
		this.name = name;
	}

	public AbstractMethod(IClass enclosingClass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.enclosingClass = enclosingClass;
	}

	public AbstractMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                     AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
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
	public void setTypeParametric()
	{
		this.typeParameters = new ITypeParameter[2];
	}

	@Override
	public boolean isTypeParametric()
	{
		return this.typeParameterCount > 0;
	}

	@Override
	public int typeParameterCount()
	{
		return this.typeParameterCount;
	}

	@Override
	public void setTypeParameters(ITypeParameter[] typeParameters, int count)
	{
		this.typeParameters = typeParameters;
		this.typeParameterCount = count;
	}

	@Override
	public void setTypeParameter(int index, ITypeParameter typeParameter)
	{
		this.typeParameters[index] = typeParameter;
	}

	@Override
	public void addTypeParameter(ITypeParameter typeParameter)
	{
		if (this.typeParameters == null)
		{
			this.typeParameters = new ITypeParameter[3];
			this.typeParameters[0] = typeParameter;
			this.typeParameterCount = 1;
			return;
		}

		int index = this.typeParameterCount++;
		if (this.typeParameterCount > this.typeParameters.length)
		{
			ITypeParameter[] temp = new ITypeParameter[this.typeParameterCount];
			System.arraycopy(this.typeParameters, 0, temp, 0, index);
			this.typeParameters = temp;
		}
		this.typeParameters[index] = typeParameter;

		typeParameter.setIndex(index);
	}

	@Override
	public ITypeParameter[] getTypeParameters()
	{
		return this.typeParameters;
	}

	@Override
	public ITypeParameter getTypeParameter(int index)
	{
		return this.typeParameters[index];
	}

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
		case AnnotationUtil.NATIVE:
			this.modifiers.addIntModifier(Modifiers.NATIVE);
			return false;
		case AnnotationUtil.STRICT:
			this.modifiers.addIntModifier(Modifiers.STRICT);
			return false;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		case AnnotationUtil.OVERRIDE:
			this.modifiers.addIntModifier(Modifiers.OVERRIDE);
			return false;
		case AnnotationUtil.INRINSIC:
			if (annotation == null)
			{
				return true;
			}

			this.intrinsicData = Intrinsics.readAnnotation(this, annotation);
			// retain the annotation if this method is not external
			return this.getClass() != ExternalMethod.class;
		case AnnotationUtil.DYVIL_NAME_INTERNAL:
			if (annotation == null)
			{
				return true;
			}

			final IValue firstValue = annotation.getArguments().getFirstValue();
			if (firstValue != null)
			{
				// In Dyvil source code, the @DyvilName is called @BytecodeName,
				// and it sets the name to be used in the bytecode
				this.internalName = firstValue.stringValue();
			}
			// do not retain the annotation
			return false;
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
		return this.modifiers.hasIntModifier(Modifiers.STATIC);
	}

	@Override
	public byte checkStatic()
	{
		return this.isStatic() ? TRUE : PASS;
	}

	@Override
	public boolean isAbstract()
	{
		return this.modifiers.hasIntModifier(Modifiers.ABSTRACT) && !this.isObjectMethod();
	}

	@Override
	public boolean isObjectMethod()
	{
		switch (this.parameters.size())
		{
		case 0:
			return this.name == Names.toString || this.name == Names.hashCode;
		case 1:
			if (this.name == Names.equals
				    && this.parameters.get(0).getCovariantType().getTheClass() == Types.OBJECT_CLASS)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this.enclosingClass.getHeader();
	}

	@Override
	public IClass getThisClass()
	{
		return this.enclosingClass;
	}

	@Override
	public IType getThisType()
	{
		return this.receiverType;
	}

	@Override
	public IType getReceiverType()
	{
		return this.receiverType;
	}

	@Override
	public void setReceiverType(IType type)
	{
		this.receiverType = type;
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters[i];
			if (typeParameter.getName() == name)
			{
				return typeParameter;
			}
		}

		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.resolveParameter(name);
	}

	@Override
	public dyvil.tools.compiler.ast.statement.control.Label resolveLabel(Name name)
	{
		return null;
	}

	@Override
	public dyvil.tools.compiler.ast.statement.control.Label getBreakLabel()
	{
		return null;
	}

	@Override
	public dyvil.tools.compiler.ast.statement.control.Label getContinueLabel()
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		// Handled by enclosing class
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		// Handled by enclosing class
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
		return this.type;
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
	public void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (name != this.name && name != null)
		{
			return;
		}

		final IParameterList parameters = this.getParameterList();

		final int parameterStartIndex;
		final int argumentStartIndex;
		final int argumentCount;
		final int parameterCount = parameters.size();

		final int[] matchValues;
		final IType[] matchTypes;
		boolean invalid = false;

		final int mod;
		if (receiver == null)
		{
			// No receiver

			if (arguments == null)
			{
				list.add(new Candidate<>(this));
				return;
			}

			argumentCount = arguments.size();
			matchValues = new int[argumentCount];
			matchTypes = new IType[argumentCount];
			argumentStartIndex = 0;
			parameterStartIndex = 0;
		}
		else if ((mod = this.modifiers.toFlags() & Modifiers.INFIX) != 0 && receiver.isClassAccess())
		{
			// Static access to static method

			final IType receiverType = receiver.getType();
			if (!Types.isSuperType(this.enclosingClass.getClassType(), receiverType))
			{
				// Disallow access from the wrong type
				return;
			}
			if (arguments == null)
			{
				list.add(new Candidate<>(this, IValue.EXACT_MATCH, null, false));
				return;
			}

			parameterStartIndex = 0;
			argumentCount = arguments.size();
			argumentStartIndex = 1;
			matchValues = new int[1 + argumentCount];
			matchTypes = new IType[1 + argumentCount];
			matchValues[0] = 1;
			matchTypes[0] = receiverType;
		}
		else
		{
			if (mod == Modifiers.STATIC && !receiver.isClassAccess())
			{
				// Disallow non-static access to static method
				invalid = true;
			}

			final IType receiverType;
			if (mod == Modifiers.INFIX)
			{
				// Infix access to infix method
				receiverType = parameters.get(0).getCovariantType();
				parameterStartIndex = 1;
			}
			else
			{
				// Infix access to instance method
				receiverType = this.getReceiverType();
				parameterStartIndex = 0;
			}

			final int receiverMatch = TypeChecker.getTypeMatch(receiver, receiverType, list);
			if (receiverMatch == IValue.MISMATCH)
			{
				return;
			}
			if (arguments == null)
			{
				list.add(new Candidate<>(this, receiverMatch, receiverType, false));
				return;
			}

			argumentCount = arguments.size();
			argumentStartIndex = 1;
			matchValues = new int[1 + argumentCount];
			matchTypes = new IType[1 + argumentCount];
			matchValues[0] = receiverMatch;
			matchTypes[0] = receiverType;
		}

		final int parametersLeft = parameterCount - parameterStartIndex;
		if (argumentCount > parametersLeft && !this.isVariadic())
		{
			return;
		}

		int defaults = 0;
		int varargs = 0;
		for (int argumentIndex = 0; argumentIndex < parametersLeft; argumentIndex++)
		{
			final IParameter parameter = parameters.get(parameterStartIndex + argumentIndex);
			final int partialVarargs = arguments.checkMatch(matchValues, matchTypes, argumentStartIndex, argumentIndex,
			                                                parameter, list);
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

		for (int matchValue : matchValues)
		{
			if (matchValue == IValue.MISMATCH)
			{
				return; // Mismatch
			}
		}
		list.add(new Candidate<>(this, matchValues, matchTypes, defaults, varargs, invalid));
	}

	@Override
	public void checkImplicitMatch(MatchList<IMethod> list, IValue value, IType type)
	{
		if (!this.isImplicitConversion())
		{
			// The method has to be 'implicit static'
			return;
		}
		final IParameterList parameterList = this.getParameterList();
		if (parameterList.size() != 1)
		{
			// and only take exactly one parameter
			return;
		}
		if (type != null && !Types.isSuperType(type, this.getType()
		                                                 .asParameterType())) // getType to ensure it is resolved by ExternalMethods
		{
			// The method's return type has to be a sub-type of the target type
			return;
		}

		final IType parType = parameterList.get(0).getCovariantType();

		// Note: this explicitly uses IValue.getTypeMatch to avoid nested implicit conversions
		final int match = value.getTypeMatch(parType);
		if (match > IValue.CONVERSION_MATCH)
		{
			list.add(new Candidate<>(this, match, parType, false));
		}
	}

	@Override
	public boolean isImplicitConversion()
	{
		return this.modifiers != null && this.modifiers.hasIntModifier(Modifiers.IMPLICIT | Modifiers.STATIC);
	}

	@Override
	public GenericData getGenericData(GenericData data, IValue instance, IArguments arguments)
	{
		if (!this.hasTypeVariables())
		{
			return data;
		}

		if (data == null)
		{
			return new GenericData(this, this.typeParameterCount);
		}

		data.setTypeParametric(this);
		data.setTypeCount(this.typeParameterCount);

		return data;
	}

	@Override
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue receiver,
		                            IArguments arguments, GenericData genericData)
	{
		if (receiver != null)
		{
			final int mod = this.modifiers.toFlags() & Modifiers.INFIX;
			if (mod == Modifiers.INFIX && !receiver.isClassAccess())
			{
				// infix or extension method, declaring class implicit

				final IParameter parameter = this.parameters.get(0);
				final IType paramType = parameter.getCovariantType();

				updateReceiverType(receiver, genericData);
				receiver = TypeChecker.convertValue(receiver, paramType, genericData, markers, context,
				                                    TypeChecker.markerSupplier("method.access.infix_type", this.name));

				updateReceiverType(receiver, genericData);

				for (int i = 1, count = this.parameters.size(); i < count; i++)
				{
					arguments.checkValue(i - 1, this.parameters.get(i), genericData, markers, context);
				}

				if (genericData != null)
				{
					this.checkTypeVarsInferred(markers, position, genericData);
				}
				return receiver;
			}

			if ((mod & Modifiers.STATIC) != 0)
			{
				// static method or infix, extension method with explicit declaring class

				if (!receiver.isClassAccess())
				{
					// static method called like instance method -> warning

					markers.add(Markers.semantic(position, "method.access.static", this.name));
				}
				else if (receiver.getType().getTheClass() != this.enclosingClass)
				{
					// static method called on wrong type -> warning

					markers.add(Markers.semantic(position, "method.access.static.type", this.name,
					                             this.enclosingClass.getFullName()));
				}
				receiver = receiver.asIgnoredClassAccess();
			}
			else if (receiver.isClassAccess())
			{
				// instance method, accessed via declaring class

				if (!receiver.getType().getTheClass().isObject())
				{
					// declaring class is not an object class -> error

					markers.add(Markers.semanticError(position, "method.access.instance", this.name));
				}
			}
			else
			{
				// normal instance method access

				updateReceiverType(receiver, genericData);
				receiver = TypeChecker.convertValue(receiver, this.getReceiverType(), genericData, markers, context,
				                                    TypeChecker
					                                    .markerSupplier("method.access.receiver_type", this.name));
				updateReceiverType(receiver, genericData);
			}
		}
		else if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			// no receiver, non-static method

			if (context.isStatic())
			{
				// called from static context -> error

				markers.add(Markers.semantic(position, "method.access.instance", this.name));
			}
			else
			{
				// unqualified call
				final IType receiverType = this.enclosingClass.getThisType();
				receiver = new ThisExpr(position, receiverType, context, markers);
				if (genericData != null)
				{
					genericData.setFallbackTypeContext(receiverType);
				}

				if (!this.enclosingClass.isAnonymous())
				{
					markers.add(Markers.semantic(position, "method.access.unqualified", this.name.unqualified));
				}
			}
		}

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			arguments.checkValue(i, this.parameters.get(i), genericData, markers, context);
		}

		if (genericData != null)
		{
			this.checkTypeVarsInferred(markers, position, genericData);
		}
		return receiver;
	}

	private static void updateReceiverType(IValue receiver, GenericData genericData)
	{
		if (genericData != null)
		{
			genericData.lockAvailable();
			genericData.setFallbackTypeContext(receiver.getType());
		}
	}

	private void checkTypeVarsInferred(MarkerList markers, ICodePosition position, GenericData genericData)
	{
		genericData.lock(this.typeParameterCount);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters[i];
			final IType typeArgument = genericData.getType(typeParameter.getIndex());

			if (typeArgument == null || typeArgument instanceof CovariantTypeVarType)
			{
				final IType inferredType = typeParameter.getUpperBound();
				markers.add(Markers.semantic(position, "method.typevar.infer", this.name, typeParameter.getName(),
				                             inferredType));
				genericData.setType(typeParameter.getIndex(), inferredType);
			}
			else if (!typeParameter.isAssignableFrom(typeArgument, genericData))
			{
				final Marker marker = Markers.semanticError(position, "method.typevar.incompatible", this.name,
				                                            typeParameter.getName());
				marker.addInfo(Markers.getSemantic("type.generic.argument", typeArgument));
				marker.addInfo(Markers.getSemantic("type_parameter.declaration", typeParameter));
				markers.add(marker);
			}
		}
	}

	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance,
		                     IArguments arguments, ITypeContext typeContext)
	{
		ModifierUtil.checkVisibility(this, position, markers, context);

		if (instance != null)
		{
			this.checkMutating(markers, instance);
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

	private void checkMutating(MarkerList markers, IValue receiver)
	{
		final IType receiverType = receiver.getType();
		if (receiverType.getMutability() != Mutability.IMMUTABLE)
		{
			return;
		}

		final IAnnotation mutatingAnnotation = this.getAnnotation(Types.MUTATING_CLASS);
		if (mutatingAnnotation == null)
		{
			return;
		}

		final IValue value = mutatingAnnotation.getArguments().getValue(0, Annotation.VALUE);
		final String stringValue = value != null ? value.stringValue() : Mutating.DEFAULT_MESSAGE;
		StringBuilder builder = new StringBuilder(stringValue);

		int index = builder.indexOf("{method}");
		if (index >= 0)
		{
			builder.replace(index, index + 8, this.name.unqualified);
		}

		index = builder.indexOf("{type}");
		if (index >= 0)
		{
			builder.replace(index, index + 6, receiverType.toString());
		}

		markers.add(new SemanticError(receiver.getPosition(), builder.toString()));
	}

	@Override
	public boolean overrides(IMethod candidate, ITypeContext typeContext)
	{
		// Check Name and number of type parameters
		if (candidate.getName() != this.name // different name
			    || this.typeParameterCount != candidate.typeParameterCount() // different number of type params
			    || candidate.hasModifier(Modifiers.STATIC_FINAL)) // don't check static final
		{
			return false;
		}

		final IParameterList candidateParameters = candidate.getParameterList();

		// Check Parameter Count
		if (candidateParameters.size() != this.parameters.size())
		{
			return false;
		}

		// The above checks can be made without checking the cache (CodeMethod) or resolving parameter types (ExternalMethod)
		if (this.checkOverride0(candidate))
		{
			return true;
		}

		// Check Parameter Types
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IType parType = this.parameters.get(i).getCovariantType().getConcreteType(typeContext);
			final IType candidateParType = candidateParameters.get(i).getCovariantType().getConcreteType(typeContext);
			if (!Types.isSameType(parType, candidateParType))
			{
				return false;
			}
		}

		return true;
	}

	protected boolean checkOverride0(IMethod candidate)
	{
		return false;
	}

	@Override
	public void addOverride(IMethod method)
	{
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.typeParameterCount > 0 || this.enclosingClass.isTypeParametric();
	}

	@Override
	public boolean isIntrinsic()
	{
		return this.intrinsicData != null;
	}

	@Override
	public IntrinsicData getIntrinsicData()
	{
		return this.intrinsicData;
	}

	@Override
	public int getInvokeOpcode()
	{
		int modifiers = this.modifiers.toFlags();
		if ((modifiers & Modifiers.STATIC) != 0)
		{
			return Opcodes.INVOKESTATIC;
		}
		if ((modifiers & Modifiers.PRIVATE) == Modifiers.PRIVATE)
		{
			return Opcodes.INVOKESPECIAL;
		}
		if (this.enclosingClass.isInterface())
		{
			return Opcodes.INVOKEINTERFACE;
		}
		return Opcodes.INVOKEVIRTUAL;
	}

	@Override
	public Handle toHandle()
	{
		return new Handle(ClassFormat.insnToHandle(this.getInvokeOpcode()), this.enclosingClass.getInternalName(),
		                  this.getInternalName(), this.getDescriptor());
	}

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}

		return this.internalName = this.name.qualified;
	}

	@Override
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		// Similar copy in NestedMethod.getDescriptor
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');

		this.parameters.appendDescriptor(buffer);
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].appendParameterDescriptor(buffer);
		}

		buffer.append(')');
		this.type.appendExtendedName(buffer);

		return this.descriptor = buffer.toString();
	}

	@Override
	public String getSignature()
	{
		if (this.signature != null)
		{
			return this.signature;
		}

		StringBuilder buffer = new StringBuilder();
		if (this.typeParameterCount > 0)
		{
			buffer.append('<');
			for (int i = 0; i < this.typeParameterCount; i++)
			{
				this.typeParameters[i].appendSignature(buffer);
			}
			buffer.append('>');
		}

		buffer.append('(');
		this.parameters.appendSignature(buffer);
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].appendParameterSignature(buffer);
		}
		buffer.append(')');
		this.type.appendSignature(buffer, false);
		return this.signature = buffer.toString();
	}

	@Override
	public String[] getInternalExceptions()
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

	private boolean useIntrinsicBytecode()
	{
		return this.intrinsicData != null && this.intrinsicData.getCompilerCode() == 0;
	}

	@Override
	public void writeCall(MethodWriter writer, IValue receiver, IArguments arguments, ITypeContext typeContext,
		                     IType targetType, int lineNumber) throws BytecodeException
	{
		if (this.useIntrinsicBytecode())
		{
			this.intrinsicData.writeIntrinsic(writer, receiver, arguments, lineNumber);
		}
		else
		{
			this.writeArgumentsAndInvoke(writer, receiver, arguments, typeContext, lineNumber);
		}

		if (targetType != null)
		{
			this.getType().writeCast(writer, targetType, lineNumber);
		}
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue receiver, IArguments arguments,
		                     ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		if (this.useIntrinsicBytecode())
		{
			this.intrinsicData.writeIntrinsic(writer, dest, receiver, arguments, lineNumber);
			return;
		}

		this.writeArgumentsAndInvoke(writer, receiver, arguments, typeContext, lineNumber);
		this.type.writeCast(writer, Types.BOOLEAN, 0);
		writer.visitJumpInsn(IFNE, dest);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue receiver, IArguments arguments,
		                        ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		if (this.useIntrinsicBytecode())
		{
			this.intrinsicData.writeInvIntrinsic(writer, dest, receiver, arguments, lineNumber);
			return;
		}

		this.writeArgumentsAndInvoke(writer, receiver, arguments, typeContext, lineNumber);
		this.type.writeCast(writer, Types.BOOLEAN, 0);
		writer.visitJumpInsn(IFEQ, dest);
	}

	protected void writeReceiver(MethodWriter writer, IValue receiver) throws BytecodeException
	{
		if (receiver == null)
		{
			return;
		}

		final int modifiers = this.modifiers.toFlags();
		if ((modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			receiver.writeExpression(writer, this.parameters.get(0).getCovariantType());
			return;
		}

		final IType receiverType = this.enclosingClass.getReceiverType();

		if ((modifiers & Modifiers.STATIC) == 0)
		{
			receiver.writeNullCheckedExpression(writer, receiverType);
		}
		else
		{
			receiver.writeExpression(writer, receiverType);
		}

		if (receiver.isIgnoredClassAccess())
		{
			final IType type = receiver.getType();
			if (type.hasTag(IType.TYPE_VAR))
			{
				// Static virtual call
				type.writeClassExpression(writer, true);
			}
		}
	}

	protected void writeArguments(MethodWriter writer, IValue receiver, IArguments arguments) throws BytecodeException
	{
		if (receiver != null && !receiver.isIgnoredClassAccess() && this.hasModifier(Modifiers.INFIX))
		{
			arguments.writeValues(writer, this.parameters, 1);
			return;
		}

		arguments.writeValues(writer, this.parameters, 0);
	}

	private void writeArgumentsAndInvoke(MethodWriter writer, IValue instance, IArguments arguments,
		                                    ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, instance);
		this.writeArguments(writer, instance, arguments);
		this.writeInvoke(writer, instance, arguments, typeContext, lineNumber);
	}

	@Override
	public void writeInvoke(MethodWriter writer, IValue receiver, IArguments arguments, ITypeContext typeContext,
		                       int lineNumber) throws BytecodeException
	{
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters[i];
			typeParameter.writeArgument(writer, typeContext.resolveType(typeParameter));
		}

		writer.visitLineNumber(lineNumber);

		int opcode;
		int modifiers = this.modifiers.toFlags();

		final String owner = this.enclosingClass.getInternalName();
		final String mangledName = this.getInternalName();
		final String descriptor = this.getDescriptor();

		if ((modifiers & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			writer.visitInvokeDynamicInsn(mangledName, descriptor, EXTENSION_BSM,
			                              new Handle(ClassFormat.H_INVOKESTATIC, owner, mangledName, descriptor));
			return;
		}

		if (receiver == null)
		{
			opcode = this.getInvokeOpcode();
		}
		else if (receiver.valueTag() == IValue.SUPER)
		{
			opcode = Opcodes.INVOKESPECIAL;
		}
		else if (receiver.isIgnoredClassAccess() && receiver.getType().hasTag(IType.TYPE_VAR))
		{
			writer
				.visitInvokeDynamicInsn(mangledName, descriptor.replace("(", "(Ljava/lang/Class;"), STATICVIRTUAL_BSM);
			return;
		}
		else
		{
			opcode = this.getInvokeOpcode();
		}

		writer.visitMethodInsn(opcode, owner, mangledName, descriptor, this.enclosingClass.isInterface());
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);

		// Type
		boolean typeAscription;
		boolean parameters;
		if (this.type != null && this.type != Types.UNKNOWN)
		{
			typeAscription = Formatting.typeAscription("method.type_ascription", this);

			if (!typeAscription)
			{
				this.type.toString(indent, buffer);
				parameters = true;
			}
			else
			{
				buffer.append("func");
				parameters = this.parameters.size() > 0 || Formatting.getBoolean("method.parameters.visible");
			}
		}
		else
		{
			typeAscription = false;
			buffer.append("func");
			parameters = this.parameters.size() > 0 || Formatting.getBoolean("method.parameters.visible");
		}

		// Name
		buffer.append(' ').append(this.name);

		// Type Parameters
		if (this.typeParameterCount > 0)
		{
			if (Util.endsWithSymbol(buffer))
			{
				buffer.append(' ');
			}

			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
			Util.astToString(indent, this.typeParameters, this.typeParameterCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
		}

		// Parameters
		if (parameters)
		{
			this.parameters.toString(indent, buffer);
		}

		// Exceptions
		if (this.exceptionCount > 0)
		{
			final String throwsIndent;
			if (Formatting.getBoolean("method.throws.newline"))
			{
				throwsIndent = Formatting.getIndent("method.throws.indent", indent);
				buffer.append('\n').append(throwsIndent).append("throws ");
			}
			else
			{
				throwsIndent = indent;
				buffer.append(" throws ");
			}

			Util.astToString(throwsIndent, this.exceptions, this.exceptionCount,
			                 Formatting.getSeparator("method.throws", ','), buffer);
		}

		// Type Ascription
		if (typeAscription)
		{
			Formatting.appendSeparator(buffer, "method.type_ascription", "->");
			this.type.toString(indent, buffer);
		}

		// Implementation
		final IValue value = this.getValue();
		if (value != null)
		{
			if (Util.formatStatementList(indent, buffer, value))
			{
				return;
			}

			if (Formatting.getBoolean("method.declaration.space_before"))
			{
				buffer.append(' ');
			}

			buffer.append('=');

			String valuePrefix = Formatting.getIndent("method.declaration.indent", indent);
			if (Formatting.getBoolean("method.declaration.newline_after"))
			{
				buffer.append('\n').append(valuePrefix);
			}
			else if (Formatting.getBoolean("method.declaration.space_after"))
			{
				buffer.append(' ');
			}

			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("method.semicolon"))
		{
			buffer.append(';');
		}
	}
}
