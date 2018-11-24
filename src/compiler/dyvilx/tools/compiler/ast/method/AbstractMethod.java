package dyvilx.tools.compiler.ast.method;

import dyvil.annotation.Mutating;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.IAccessible;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.VariableThis;
import dyvilx.tools.compiler.ast.generic.GenericData;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.member.AbstractMember;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.SemanticError;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;

public abstract class AbstractMethod extends AbstractMember implements IMethod, ILabelContext, IDefaultContext
{
	// =============== Constants ===============

	protected static final Handle EXTENSION_BSM = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/DynamicLinker",
	                                                         "linkExtension",
	                                                         ClassFormat.BSM_HEAD + "Ljava/lang/invoke/MethodHandle;"
	                                                         + ClassFormat.BSM_TAIL);

	protected static final Handle STATICVIRTUAL_BSM = new Handle(ClassFormat.H_INVOKESTATIC,
	                                                             "dyvil/runtime/DynamicLinker", "linkClassMethod",
	                                                             ClassFormat.BSM_HEAD + ClassFormat.BSM_TAIL);

	// =============== Fields ===============

	protected @Nullable TypeParameterList typeParameters;

	protected IType receiverType;
	protected IType thisType;

	protected @NonNull ParameterList parameters = new ParameterList();

	protected @Nullable TypeList exceptions;

	// --------------- Metadata ---------------

	protected IClass enclosingClass;

	protected String internalName;
	protected String descriptor;
	protected String signature;

	protected @Nullable IntrinsicData intrinsicData;

	protected @Nullable Set<IMethod> overrideMethods;

	// =============== Constructors ===============

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

	public AbstractMethod(IClass enclosingClass, Name name, IType type, AttributeList attributes)
	{
		super(name, type, attributes);
		this.enclosingClass = enclosingClass;
	}

	public AbstractMethod(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

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

	// - - - - - - - - Type Parameters - - - - - - - -

	@Override
	public boolean isTypeParametric()
	{
		return this.typeParameters != null && this.typeParameters.size() > 0;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.isTypeParametric() || this.enclosingClass.isTypeParametric();
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		if (this.typeParameters != null)
		{
			return this.typeParameters;
		}
		return this.typeParameters = new TypeParameterList();
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
		case ModifierUtil.NATIVE_INTERNAL:
			this.attributes.addFlag(Modifiers.NATIVE);
			return true;
		case ModifierUtil.STRICTFP_INTERNAL:
			this.attributes.addFlag(Modifiers.STRICT);
			return true;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.attributes.addFlag(Modifiers.DEPRECATED);
			return false;
		case AnnotationUtil.INRINSIC_INTERNAL:
			this.attributes.addFlag(Modifiers.INTRINSIC);
			return false;
		case AnnotationUtil.JAVA_NAME_INTERNAL:
			if (annotation == null)
			{
				return false;
			}

			final IValue firstValue = annotation.getArguments().getFirst();
			if (firstValue != null)
			{
				// In Dyvil source code, the @DyvilName is called @BytecodeName,
				// and it sets the name to be used in the bytecode
				this.internalName = firstValue.stringValue();
			}
			return true;
		case AnnotationUtil.AUTOMANGLED_INTERNAL:
			this.internalName = this.createMangledName();
			return true;
		}
		return false;
	}

	private String createMangledName()
	{
		// append the qualified name
		final StringBuilder builder = new StringBuilder(this.name.qualified);

		for (IParameter param : this.getParameters())
		{
			// append all external parameter labels preceded by an underscore
			builder.append('_').append(param.getQualifiedLabel());
		}

		return builder.toString();
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
	public boolean isImplicitConversion()
	{
		return this.hasModifier(Modifiers.IMPLICIT | Modifiers.STATIC);
	}

	@Override
	public boolean isFunctional()
	{
		return this.isAbstract() && !this.isObjectMethod();
	}

	// - - - - - - - - This Type - - - - - - - -

	@Override
	public IType getThisType()
	{
		if (this.thisType != null)
		{
			return this.thisType;
		}
		return this.thisType = this.enclosingClass.getThisType();
	}

	@Override
	public boolean setThisType(IType type)
	{
		this.receiverType = null;
		this.thisType = type;
		return true;
	}

	@Override
	public IType getReceiverType()
	{
		if (this.receiverType != null)
		{
			return this.receiverType;
		}

		return this.receiverType = this.getThisType().asParameterType();
	}

	// --------------- Context ---------------

	// - - - - - - - - Header - - - - - - - -

	@Override
	public IHeaderUnit getHeader()
	{
		return this.enclosingClass.getHeader();
	}

	// - - - - - - - - Return Type - - - - - - - -

	@Override
	public IType getReturnType()
	{
		return this.type;
	}

	// - - - - - - - - This - - - - - - - -

	@Override
	public boolean isThisAvailable()
	{
		return !this.isStatic() || this.hasModifier(Modifiers.EXTENSION);
	}

	@Override
	public IClass getThisClass()
	{
		return this.enclosingClass;
	}

	@Override
	public IAccessible getAccessibleThis(IType type)
	{
		return Types.isSuperType(type, this.getThisType()) ? VariableThis.DEFAULT : null;
	}

	// - - - - - - - - Type Parameters - - - - - - - -

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		return this.typeParameters == null ? null : this.typeParameters.get(name);
	}

	@Override
	public IContext getTypeParameterContext()
	{
		return this.enclosingClass == null || this.isStatic() ?
			       this :
			       new CombiningContext(this, this.enclosingClass.getTypeParameterContext());
	}

	// - - - - - - - - Fields - - - - - - - -

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.get(name);
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		if (type == null)
		{
			return null;
		}

		for (IParameter param : this.parameters)
		{
			if (param.isImplicit() && Types.isSuperType(type, param.getType()))
			{
				return new FieldAccess(param);
			}
		}
		return null;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.parameters.isParameter(variable) || this.typeParameters != null && this.typeParameters
			                                                                               .isMember(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		return variable;
	}

	// - - - - - - - - Labels - - - - - - - -

	@Override
	public dyvilx.tools.compiler.ast.statement.control.Label resolveLabel(Name name)
	{
		return null;
	}

	@Override
	public dyvilx.tools.compiler.ast.statement.control.Label getBreakLabel()
	{
		return null;
	}

	@Override
	public dyvilx.tools.compiler.ast.statement.control.Label getContinueLabel()
	{
		return null;
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

	// --------------- Method Matching ---------------

	@Override
	public void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (name != this.name && name != null)
		{
			return;
		}

		final ParameterList parameters = this.getParameters();

		final int parameterStartIndex;
		final int matchStartIndex;
		final int argumentCount;

		final int[] matchValues;
		final IType[] matchTypes;
		boolean invalid = false;

		final long mod = this.attributes.flags() & Modifiers.INFIX;
		if (receiver == null)
		{
			if (mod == Modifiers.INFIX)
			{
				// disallow non-qualified access to infix methods
				invalid = true;
			}

			// No receiver

			if (arguments == null)
			{
				list.add(new Candidate<>(this));
				return;
			}

			argumentCount = arguments.size();
			matchValues = new int[argumentCount];
			matchTypes = new IType[argumentCount];
			matchStartIndex = 0;
			parameterStartIndex = 0;
		}
		else if (mod != 0 && receiver.isClassAccess())
		{
			// Static access to static method

			final IType receiverType = this.getReceiverType();
			if (!Types.isSuperType(receiverType, receiver.getType()))
			{
				// Disallow access from the wrong type
				return;
			}
			if (arguments == null)
			{
				list.add(new Candidate<>(this, IValue.EXACT_MATCH, receiverType, false));
				return;
			}

			parameterStartIndex = 0;
			argumentCount = arguments.size();
			matchStartIndex = 1;
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
			else if (mod == 0 && receiver.isClassAccess())
			{
				final IClass receiverClass = receiver.getType().getTheClass();
				if (receiverClass == null || !receiverClass.isObject())
				{
					// Disallow static access to instance method (unless it's an object class)
					invalid = true;
				}
			}

			final IType receiverType;
			if (mod == Modifiers.INFIX && !parameters.isEmpty())
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
				list.add(new Candidate<>(this, receiverMatch, receiverType, invalid));
				return;
			}

			argumentCount = arguments.size();
			matchStartIndex = 1;
			matchValues = new int[1 + argumentCount];
			matchTypes = new IType[1 + argumentCount];
			matchValues[0] = receiverMatch;
			matchTypes[0] = receiverType;
		}

		checkMatch(this, list, parameters, parameterStartIndex, arguments, matchValues, matchTypes, matchStartIndex,
		           invalid);
	}

	public static <T extends IOverloadable> void checkMatch( //
		T member, MatchList<T> list, // member info
		ParameterList parameters, int parameterStartIndex, // parameters
		ArgumentList arguments, // arguments
		int[] matchValues, IType[] matchTypes, int matchStartIndex, // match values
		boolean invalid)
	{
		final int parametersLeft = parameters.size() - parameterStartIndex;
		if (arguments.size() > parametersLeft && !member.isVariadic())
		{
			return;
		}

		int defaults = 0;
		int varargs = 0;
		for (int parameterIndex = 0, argumentIndex = 0; parameterIndex < parametersLeft; parameterIndex++)
		{
			final IParameter parameter = parameters.get(parameterStartIndex + parameterIndex);

			final int result = arguments.checkMatch(matchValues, matchTypes, matchStartIndex, argumentIndex, parameter,
			                                        list);
			switch (result)
			{
			case ArgumentList.MISMATCH:
				return;
			case ArgumentList.DEFAULT_MATCH:
				defaults++;
				continue;
			case ArgumentList.REGULAR_MATCH:
				argumentIndex++;
				continue;
			default:
				varargs += result;
				argumentIndex += result;
			}
		}

		for (int matchValue : matchValues)
		{
			if (matchValue == IValue.MISMATCH)
			{
				return; // Mismatch
			}
		}
		list.add(new Candidate<>(member, matchValues, matchTypes, defaults, varargs, invalid));
	}

	@Override
	public void checkImplicitMatch(MatchList<IMethod> list, IValue value, IType type)
	{
		if (!this.isImplicitConversion())
		{
			// The method has to be 'implicit static'
			return;
		}
		final ParameterList parameterList = this.getParameters();
		if (parameterList.size() != 1)
		{
			// and only take exactly one parameter
			return;
		}
		if (type != null && !Types.isSuperType(type, this.getType().asParameterType()))
		{
			// The method's return type has to be a sub-type of the target type
			return;
		}

		final IType parType = parameterList.get(0).getCovariantType();

		// Note: this explicitly uses null as the context parameter to avoid nested implicit conversions
		final int match = TypeChecker.getTypeMatch(value, parType, null);

		// >= to allow the "light" conversions that are built in based on type, e.g. primitive widening or (un)boxing
		if (match >= IValue.CONVERSION_MATCH)
		{
			list.add(new Candidate<>(this, match, parType, false));
		}
	}

	// --------------- Call Checking ---------------

	@Override
	public GenericData getGenericData(GenericData data, IValue instance, ArgumentList arguments)
	{
		if (!this.hasTypeVariables())
		{
			return data;
		}

		if (data == null)
		{
			return new GenericData(this, this.typeArity());
		}

		data.setMember(this);

		return data;
	}

	@Override
	public IValue checkArguments(MarkerList markers, SourcePosition position, IContext context, IValue receiver,
		ArgumentList arguments, GenericData genericData)
	{
		final ParameterList parameters = this.getParameters();

		if (receiver != null)
		{
			final long mod = this.attributes.flags() & Modifiers.INFIX;
			if (mod == Modifiers.INFIX && !receiver.isClassAccess() && !parameters.isEmpty())
			{
				// infix or extension method, declaring class implicit

				final IParameter parameter = parameters.get(0);
				final IType paramType = parameter.getCovariantType();

				updateReceiverType(receiver, genericData);
				receiver = TypeChecker.convertValue(receiver, paramType, genericData, markers, context,
				                                    TypeChecker.markerSupplier("method.access.infix_type", this.name));

				updateReceiverType(receiver, genericData);

				for (int i = 1, count = parameters.size(); i < count; i++)
				{
					arguments.checkValue(i - 1, parameters.get(i), genericData, position, markers, context);
				}

				if (genericData != null)
				{
					this.checkTypeVarsInferred(markers, position, genericData);
				}
				return receiver;
			}

			updateReceiverType(receiver, genericData);

			if ((mod & Modifiers.STATIC) != 0)
			{
				// static method or infix, extension method with explicit declaring class

				if (!receiver.isClassAccess())
				{
					// static method called like instance method -> warning

					markers.add(Markers.semanticError(position, "method.access.static", this.name));
				}
				else if (this.getReceiverType().getTheClass() == this.enclosingClass
				         && receiver.getType().getTheClass() != this.enclosingClass)
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

				final IClass receiverClass = receiver.getType().getTheClass();
				if (receiverClass == null || !receiverClass.isObject())
				{
					// declaring class is not an object class -> error

					markers.add(Markers.semanticError(position, "method.access.instance", this.name));
				}
			}
			else
			{
				// normal instance method access

				receiver = TypeChecker.convertValue(receiver, this.getReceiverType(), genericData, markers, context,
				                                    TypeChecker
					                                    .markerSupplier("method.access.receiver_type", this.name));
			}

			if (receiver != null)
			{
				updateReceiverType(receiver, genericData);
			}
		}
		else if (!this.isStatic())
		{
			// no receiver, non-static method

			if (!context.isThisAvailable())
			{
				// called from static context -> error

				markers.add(Markers.semantic(position, "method.access.instance", this.name));
			}
			else
			{
				// unqualified call
				final IType receiverType = this.enclosingClass.getThisType();
				receiver = new ThisExpr(position, receiverType, markers, context);
				if (genericData != null)
				{
					genericData.setFallbackTypeContext(receiverType);
				}

				if (!this.isNested() && !this.enclosingClass.isAnonymous())
				{
					markers.add(Markers.semantic(position, "method.access.unqualified", this.name.unqualified));
				}
			}
		}

		for (int i = 0, count = parameters.size(); i < count; i++)
		{
			arguments.checkValue(i, parameters.get(i), genericData, position, markers, context);
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

	private void checkTypeVarsInferred(MarkerList markers, SourcePosition position, GenericData genericData)
	{
		if (this.typeParameters == null)
		{
			return;
		}

		final int count = this.typeParameters.size();
		genericData.lock(count);

		for (int i = 0; i < count; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters.get(i);
			final IType typeArgument = genericData.resolveType(typeParameter);

			if (typeArgument == null || typeArgument instanceof CovariantTypeVarType)
			{
				final IType inferredType = typeParameter.getUpperBound();
				markers.add(Markers.semantic(position, "method.typevar.infer", this.name, typeParameter.getName(),
				                             inferredType));
				genericData.addMapping(typeParameter, inferredType);
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
	public void checkCall(MarkerList markers, SourcePosition position, IContext context, IValue instance,
		ArgumentList arguments, ITypeContext typeContext)
	{
		ModifierChecks.checkVisibility(this, position, markers, context);

		if (instance != null)
		{
			this.checkMutating(markers, instance);
		}

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

	private void checkMutating(MarkerList markers, IValue receiver)
	{
		final IType receiverType = receiver.getType();
		if (receiverType.getMutability() != Mutability.IMMUTABLE)
		{
			return;
		}

		final Annotation mutatingAnnotation = this.getAnnotation(Types.MUTATING_CLASS);
		if (mutatingAnnotation == null)
		{
			return;
		}

		final IValue value = mutatingAnnotation.getArguments().get(0, Names.value);
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

	// --------------- Override Checking ---------------

	@Override
	public boolean overrides(IMethod candidate, ITypeContext typeContext)
	{
		if (candidate.hasModifier(Modifiers.STATIC_FINAL) // don't check static final
		    || this.typeArity() != candidate.typeArity() // different number of type params
		    || this.name != candidate.getName() // different name
		       && !candidate.getInternalName().equals(this.getInternalName()) // and different internal name
			// this means either name or internal name or both must match to consider an override
			// if only one matches then there is special code in CodeMethod.filterOverride that produces diagnostics
		)
		{
			return false;
		}

		final ParameterList thisParameters = this.getParameters();
		final ParameterList candidateParameters = candidate.getParameters();

		if (candidateParameters.size() != thisParameters.size()) // different number of parameters
		{
			return false;
		}

		// Check the cache
		if (this.overrideMethods != null && this.overrideMethods.contains(candidate))
		{
			return true;
		}

		// Check Parameter Types
		for (int i = 0, count = thisParameters.size(); i < count; i++)
		{
			final IType parType = thisParameters.get(i).getCovariantType().getConcreteType(typeContext);
			final IType candidateParType = candidateParameters.get(i).getCovariantType().getConcreteType(typeContext);
			if (!Types.isSameType(parType, candidateParType))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void addOverride(IMethod method)
	{
		if (!this.enclosingClass.isSubClassOf(method.getEnclosingClass().getClassType()))
		{
			return;
		}

		if (this.overrideMethods == null)
		{
			this.overrideMethods = Collections.newSetFromMap(new IdentityHashMap<>());
		}
		this.overrideMethods.add(method);
	}

	// --------------- Compilation ---------------

	// - - - - - - - - Intrinsics - - - - - - - -

	@Override
	public boolean isIntrinsic()
	{
		return this.hasModifier(Modifiers.INTRINSIC);
	}

	private boolean useIntrinsicBytecode()
	{
		return this.intrinsicData != null && this.intrinsicData.getCompilerCode() == 0;
	}

	@Override
	public IntrinsicData getIntrinsicData()
	{
		if (!this.isIntrinsic())
		{
			return null;
		}
		if (this.intrinsicData != null)
		{
			return this.intrinsicData;
		}
		final Annotation annotation = this.getAnnotation(Types.INTRINSIC_CLASS);
		if (annotation == null)
		{
			return null;
		}
		return this.intrinsicData = Intrinsics.readAnnotation(this, annotation);
	}

	// - - - - - - - - Invoke Opcode and Handle - - - - - - - -

	@Override
	public int getInvokeOpcode()
	{
		return this.getInvokeOpcode(this.getEnclosingClass());
	}

	private int getInvokeOpcode(IClass owner)
	{
		if (this.isStatic() || this.hasModifier(Modifiers.EXTENSION))
		{
			return Opcodes.INVOKESTATIC;
		}
		if (this.hasModifier(Modifiers.PRIVATE))
		{
			return Opcodes.INVOKESPECIAL;
		}
		if (owner.isInterface())
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

	// - - - - - - - - Internal Name - - - - - - - -

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}

		return this.internalName = this.name.qualified;
	}

	public void setInternalName(String internalName)
	{
		this.internalName = internalName;
	}

	// - - - - - - - - Descriptor and Signature - - - - - - - -

	@Override
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');

		if (!this.isStatic() && this.hasModifier(Modifiers.EXTENSION))
		{
			this.getThisType().appendExtendedName(buffer);
		}

		this.parameters.appendDescriptor(buffer);
		if (this.typeParameters != null)
		{
			this.typeParameters.appendParameterDescriptors(buffer);
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
		if (this.typeParameters != null)
		{
			this.typeParameters.appendSignature(buffer);
		}

		buffer.append('(');

		if (!this.isStatic() && this.hasModifier(Modifiers.EXTENSION))
		{
			this.getThisType().appendSignature(buffer, false);
		}

		this.parameters.appendSignature(buffer);
		if (this.typeParameters != null)
		{
			this.typeParameters.appendParameterSignatures(buffer);
		}

		buffer.append(')');
		this.type.appendSignature(buffer, false);
		return this.signature = buffer.toString();
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
	public void writeCall(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext,
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
	public void writeJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments,
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
	public void writeInvJump(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments,
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

	private void writeArgumentsAndInvoke(MethodWriter writer, IValue receiver, ArgumentList arguments,
		ITypeContext typeContext, int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, receiver);
		this.writeArguments(writer, receiver, arguments);
		this.writeInvoke(writer, receiver, arguments, typeContext, lineNumber);
	}

	protected void writeReceiver(MethodWriter writer, IValue receiver) throws BytecodeException
	{
		if (receiver == null)
		{
			return;
		}

		if (!this.isStatic())
		{
			receiver.writeNullCheckedExpression(writer, this.getReceiverType());
			return;
		}

		if (receiver.isIgnoredClassAccess())
		{
			final IType type = receiver.getType();
			if (type.hasTag(IType.TYPE_VAR))
			{
				// Static virtual call
				type.writeClassExpression(writer, true);
			}
			return;
		}
		if (this.hasModifier(Modifiers.INFIX) && !this.parameters.isEmpty())
		{
			receiver.writeExpression(writer, this.parameters.get(0).getCovariantType());
			return;
		}

		// static
		receiver.writeExpression(writer, this.getReceiverType());
		writer.visitInsn(Opcodes.AUTO_POP);
	}

	protected void writeArguments(MethodWriter writer, IValue receiver, ArgumentList arguments) throws BytecodeException
	{
		if (receiver != null && !receiver.isIgnoredClassAccess() && this.hasModifier(Modifiers.INFIX)
		    && !this.parameters.isEmpty())
		{
			arguments.writeValues(writer, this.parameters, 1);
			return;
		}

		arguments.writeValues(writer, this.parameters, 0);
	}

	@Override
	public void writeInvoke(MethodWriter writer, IValue receiver, ArgumentList arguments, ITypeContext typeContext,
		int lineNumber) throws BytecodeException
	{
		if (this.typeParameters != null)
		{
			this.typeParameters.writeArguments(writer, typeContext);
		}

		writer.visitLineNumber(lineNumber);

		final int opcode;
		final String mangledName = this.getInternalName();
		final String descriptor = this.getDescriptor();

		if (this.hasModifier(Modifiers.EXTENSION) // extension
		    && !this.isStatic() // non-static
		    && !this.hasModifier(Modifiers.FINAL) // non-final
		)
		{
			// dynamic invocation of non-static non-final extension method
			final Handle handle = new Handle(ClassFormat.H_INVOKESTATIC, this.enclosingClass.getInternalName(),
			                                 mangledName, descriptor);
			writer.visitInvokeDynamicInsn(mangledName, descriptor, EXTENSION_BSM, handle);
			return;
		}

		final String owner;
		final boolean isInterface;

		if (receiver != null)
		{
			final IType receiverType = receiver.getType();
			if (receiver.isIgnoredClassAccess() && receiverType.hasTag(IType.TYPE_VAR))
			{
				// Dynamic invocation of a static method based on a type variable
				writer.visitInvokeDynamicInsn(mangledName, descriptor.replace("(", "(Ljava/lang/Class;"),
				                              STATICVIRTUAL_BSM);
				return;
			}

			if (receiver.valueTag() == IValue.SUPER)
			{
				// Super-method invocation
				opcode = Opcodes.INVOKESPECIAL;
				owner = this.enclosingClass.getInternalName();
				isInterface = this.enclosingClass.isInterface();
			}
			else if (this.isStatic() || this.hasModifier(Modifiers.EXTENSION))
			{
				opcode = Opcodes.INVOKESTATIC;
				owner = this.enclosingClass.getInternalName();
				isInterface = this.enclosingClass.isInterface();
			}
			else
			{
				// bugfix: for non-static calls, we use the type of the receiver to avoid
				// IllegalAccessExceptions on the owner class.
				// Example: Calling length() on a StringBuilder object resolves to AbstractStringBuilder.length(),
				// but AbstractStringBuilder is not accessible
				final IClass receiverClass = receiverType.getTheClass();
				opcode = this.getInvokeOpcode(receiverClass);
				owner = receiverType.getInternalName();
				isInterface = receiverClass.isInterface();
			}
		}
		else
		{
			opcode = this.getInvokeOpcode();
			owner = this.enclosingClass.getInternalName();
			isInterface = this.enclosingClass.isInterface();
		}

		writer.visitMethodInsn(opcode, owner, mangledName, descriptor, isInterface);
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		// Annotations and Modifiers
		super.toString(indent, buffer);

		// Name
		buffer.append("func ").append(this.name);

		// Type Parameters
		if (this.typeParameters != null)
		{
			this.typeParameters.toString(indent, buffer);
		}

		// Parameters
		final IType thisType = this.getThisType();
		this.parameters.toString(thisType == this.enclosingClass.getThisType() ? null : thisType, indent, buffer);

		// Exceptions
		if (this.exceptions != null && this.exceptions.size() > 0)
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

			Util.astToString(throwsIndent, this.exceptions.getTypes(), this.exceptions.size(),
			                 Formatting.getSeparator("method.throws", ','), buffer);
		}

		// Type Ascription
		if (this.type != null && this.type != Types.UNKNOWN)
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
