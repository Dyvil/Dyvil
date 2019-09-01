package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.Iterables;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Type;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.access.AbstractCall;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.constant.WildcardValue;
import dyvilx.tools.compiler.ast.field.IAccessible;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.VariableThis;
import dyvilx.tools.compiler.ast.field.capture.CaptureHelper;
import dyvilx.tools.compiler.ast.field.capture.CaptureVariable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.MapTypeContext;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.*;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.FunctionType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

public class LambdaExpr implements IValue, ClassCompilable, IDefaultContext, IParametric
{
	// =============== Constants ===============

	public static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/LambdaMetafactory",
	                                                  "metafactory",
	                                                  ClassFormat.BSM_HEAD + "Ljava/lang/invoke/MethodType;"
	                                                  + "Ljava/lang/invoke/MethodHandle;"
	                                                  + "Ljava/lang/invoke/MethodType;" + ClassFormat.BSM_TAIL);

	public static final TypeChecker.MarkerSupplier LAMBDA_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"lambda.value.type.incompatible", "return.type", "value.type");

	// --------------- Flags ---------------

	private static final int HANDLE_TYPE_MASK     = 0b00001111;
	private static final int VALUE_RESOLVED       = 0b00010000;
	public static final  int IMPLICIT_PARAMETERS  = 0b00100000;
	private static final int EXPLICIT_RETURN      = 0b01000000;
	private static final int LAMBDA_TYPE_INFERRED = 0b10000000;

	// =============== Fields ===============

	protected ParameterList parameters;

	protected IValue value;

	// --------------- Metadata ---------------

	protected SourcePosition position;

	/**
	 * Stores various metadata about this Lambda Expression. See Flags constants for possible values
	 */
	private int flags;

	/**
	 * The instantiated type this lambda expression represents
	 */
	protected IType type;

	/**
	 * The return type of this lambda expression (the type of the return value)
	 */
	private IType returnType;

	/**
	 * The abstract method this lambda expression implements
	 */
	protected IMethod method;

	protected CaptureHelper<CaptureVariable> captureHelper;

	/**
	 * The enclosing class internal name
	 */
	private String owner;

	/**
	 * The name of the synthetic lambda method
	 */
	private String name;

	/**
	 * The descriptor of the synthetic lambda method
	 */
	private String descriptor;

	// =============== Constructors ===============

	public LambdaExpr(SourcePosition position)
	{
		this.position = position;
		this.parameters = new ParameterList(2);
	}

	public LambdaExpr(SourcePosition position, IParameter param)
	{
		this.position = position;
		this.parameters = new ParameterList(param);
	}

	public LambdaExpr(SourcePosition position, IParameter[] params, int paramCount)
	{
		this.position = position;
		this.parameters = new ParameterList(params, paramCount);
	}

	public LambdaExpr(SourcePosition position, ParameterList parameters)
	{
		this.position = position;
		this.parameters = parameters;
	}

	// =============== Properties ===============

	@Override
	public ParameterList getParameters()
	{
		return this.parameters;
	}

	@Override
	public IType getReturnType()
	{
		return this.returnType;
	}

	public void setReturnType(IType returnType)
	{
		this.returnType = returnType;
		this.flags |= EXPLICIT_RETURN;
	}

	public IValue getValue()
	{
		return this.value;
	}

	public void setValue(IValue value)
	{
		this.value = value;
	}

	// --------------- Metadata ---------------

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}

		this.flags |= LAMBDA_TYPE_INFERRED;
		return this.type = this.makeType();
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	public IMethod getMethod()
	{
		return this.method;
	}

	public void setMethod(IMethod method)
	{
		this.method = method;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public boolean isPolyExpression()
	{
		if (this.hasImplicitReturnType())
		{
			// An implicit return type always implies a poly-expression
			return true;
		}

		for (IParameter param : this.parameters)
		{
			if (param.getType().isUninferred())
			{
				// If any parameter type is uninferred / not explicit, this is a poly-expression
				return true;
			}
		}

		// Otherwise, all types are already known, so this is not a poly-expression
		return false;
	}

	// --------------- Internal ---------------

	/**
	 * Returns the Handle Type for Direct Invocation if this is a Method Pointer, {@code 0} otherwise.
	 *
	 * @return the Handle Type for Direct Invocation
	 */
	public int getHandleType()
	{
		return this.flags & HANDLE_TYPE_MASK;
	}

	private void setHandleType(int handleType)
	{
		this.flags = this.flags & ~HANDLE_TYPE_MASK | handleType;
	}

	private boolean hasImplicitReturnType()
	{
		return this.returnType == null || (this.flags & EXPLICIT_RETURN) == 0;
	}

	public void setImplicitParameters(boolean implicitParameters)
	{
		if (implicitParameters)
		{
			this.flags |= IMPLICIT_PARAMETERS;
		}
		else
		{
			this.flags &= ~IMPLICIT_PARAMETERS;
		}
	}

	private CaptureHelper<CaptureVariable> getCaptureHelper()
	{
		if (this.captureHelper != null)
		{
			return this.captureHelper;
		}
		return this.captureHelper = new CaptureHelper<>(CaptureVariable.FACTORY);
	}

	/**
	 * @return the descriptor that contains the captured instance and captured variables (if present) as the argument
	 * types and the instantiated method type as the return type.
	 */
	private String getInvokeDescriptor()
	{
		final StringBuilder builder = new StringBuilder();

		builder.append('(');
		this.appendCaptures(builder);
		builder.append(')');
		this.type.appendExtendedName(builder);

		return builder.toString();
	}

	/**
	 * @return the specialized descriptor of the lambda callback method, including parameter types and return type, but
	 * excluding captured variables
	 */
	private String getLambdaDescriptor()
	{
		final StringBuilder builder = new StringBuilder();

		builder.append('(');
		this.parameters.appendDescriptor(builder);
		builder.append(')');
		this.returnType.appendExtendedName(builder);

		return builder.toString();
	}

	/**
	 * @return the descriptor of the (synthetic) lambda callback method, including captured variables, parameter types
	 * and the return type.
	 */
	private String getTargetDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		final StringBuilder builder = new StringBuilder();

		builder.append('(');
		this.appendCaptures(builder);
		this.parameters.appendDescriptor(builder);
		builder.append(')');
		this.returnType.appendExtendedName(builder);

		return this.descriptor = builder.toString();
	}

	private void appendCaptures(StringBuilder builder)
	{
		if (this.captureHelper != null)
		{
			this.captureHelper.appendThisCaptureType(builder);
			this.captureHelper.appendCaptureTypes(builder);
		}
	}

	// =============== Methods ===============

	// --------------- Misc. ---------------

	@Override
	public int valueTag()
	{
		return LAMBDA;
	}

	@Override
	public IParameter createParameter(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new CodeParameter(null, position, name, type, attributes);
	}

	// --------------- Typing ---------------

	private @NonNull FunctionType makeType()
	{
		final int count = this.parameters.size();
		final FunctionType functionType = new FunctionType();
		final TypeList arguments = functionType.getArguments();

		for (int i = 0; i < count; i++)
		{
			arguments.add(this.parameters.get(i).getType());
		}
		arguments.add(this.returnType != null ? this.returnType : Types.UNKNOWN);

		this.flags |= LAMBDA_TYPE_INFERRED;
		return functionType;
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.type != null && Types.isSuperType(type, this.type))
		{
			return true;
		}

		// TODO maybe generalize to super-types of the corresponding Function$OfX class?
		if (type.getTheClass() == Types.OBJECT_CLASS)
		{
			return true;
		}

		final IMethod functionalMethod = type.getFunctionalMethod();
		if (functionalMethod == null)
		{
			return false;
		}

		final ParameterList methodParameters = functionalMethod.getParameters();
		final int parameterCount = this.parameters.size();

		if (parameterCount != methodParameters.size())
		{
			return false;
		}

		for (int i = 0; i < parameterCount; i++)
		{
			final IType lambdaParameterType = this.parameters.get(i).getCovariantType();
			if (lambdaParameterType.isUninferred())
			{
				continue;
			}

			final IType methodParameterType = methodParameters.get(i).getCovariantType();
			if (!Types.isSuperType(methodParameterType, lambdaParameterType))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (type.getTheClass() == Types.OBJECT_CLASS)
		{
			return SUBTYPE_MATCH;
		}
		return this.isType(type) ? EXACT_MATCH : MISMATCH;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isType(type))
		{
			return null;
		}

		if (type.getTheClass() == Types.OBJECT_CLASS)
		{
			type = this.getType();
		}

		if (type != this.type)
		{
			// If this is not the type initially created and returned by getType(), remove the LAMBDA_TYPE_INFERRED flag
			this.flags &= ~LAMBDA_TYPE_INFERRED;
		}
		this.type = type;
		this.method = type.getFunctionalMethod();

		this.inferTypes(markers);

		final IContext combinedContext = context.push(this);

		if ((this.flags & VALUE_RESOLVED) == 0)
		{
			this.value = this.value.resolve(markers, combinedContext);
			this.flags |= VALUE_RESOLVED;
		}

		if (this.returnType.isUninferred())
		{
			this.returnType = this.value.getType();
		}

		this.value = TypeChecker.convertValue(this.value, this.returnType, this.returnType, markers, combinedContext,
		                                      LAMBDA_MARKER_SUPPLIER);

		this.inferReturnType(type, this.value.getType());

		if (this.returnType.isUninferred() && this.value.isResolved())
		{
			markers.add(Markers.semanticError(this.position, "lambda.return_type.infer"));
		}

		context.pop();

		return this;
	}

	private void inferTypes(MarkerList markers)
	{
		if (!this.method.hasTypeVariables())
		{
			for (int i = 0, count = this.parameters.size(); i < count; i++)
			{
				final IParameter parameter = this.parameters.get(i);
				if (parameter.getType().isUninferred())
				{
					final IType type = this.method.getParameters().get(i).getType().atPosition(parameter.getPosition());
					parameter.setType(type);
				}
			}

			this.checkReturnType(markers, this.method.getType());
			return;
		}

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			if (!parameter.getType().isUninferred())
			{
				continue;
			}

			final SourcePosition position = parameter.getPosition();
			final IType methodParamType = this.method.getParameters().get(i).getType();
			final IType concreteType = methodParamType.getConcreteType(this.type);

			// Can't infer parameter type
			if ((this.flags & IMPLICIT_PARAMETERS) == 0 && concreteType.isUninferred())
			{
				// markers.add(Markers.semanticError(position, "lambda.parameter.implicit"));
				markers.add(Markers.semanticError(position, "lambda.parameter.type", parameter.getName()));

				continue;
			}

			// asReturnType is required for Wildcard Types
			parameter.setType(concreteType.atPosition(position));
		}

		this.checkReturnType(markers, this.method.getType().getConcreteType(this.type));
	}

	public void inferReturnType(IType type, IType valueType)
	{
		if (this.hasImplicitReturnType())
		{
			this.returnType = valueType;
		}
		if ((this.flags & LAMBDA_TYPE_INFERRED) != 0 || type.canExtract(FunctionType.class))
		{
			this.type = this.makeType();
			return;
		}

		final ITypeContext tempContext = new MapTypeContext();
		final ParameterList methodParams = this.method.getParameters();
		final int size = Math.min(this.parameters.size(), methodParams.size());

		for (int i = 0; i < size; i++)
		{
			final IParameter lambdaParam = this.parameters.get(i);
			final IParameter methodParam = methodParams.get(i);

			methodParam.getType().inferTypes(lambdaParam.getType(), tempContext);
		}
		this.method.getType().inferTypes(valueType, tempContext);

		final IType classType = this.method.getEnclosingClass().getThisType();
		final IType concreteClassType = classType.getConcreteType(tempContext);

		this.type = inferRecursively(this.type.getTheClass(), concreteClassType, tempContext);
	}

	private static IType inferRecursively(IClass thisClass, IType concrete, ITypeContext context)
	{
		/*
		 * Example
		 *
		 * interface F<T>, interface G<U> : F<U>, interface H<V> : G<V>
		 *
		 * i1 = H<V>
		 * c = F<Int>
		 * inferRecursively(i1, c, tc = {})
		 *    i2 = H.itf[0] // = G<V>
		 *    c2 = inferRecursively(i2, c, tc)
		 *       i3 = G.itf[0] // = F<U>
		 *       c3 = inferRecursively(i3, c, tc = {})
		 *          // i3.class == c.class
		 *          rit = i3.class.type // = F<T>
		 *          rit.inferType(c, tc) // tc[T] = Int
		 *          return c // = F<Int>
		 *       i3.inferTypes(c3, tc) // tc[U] = Int
		 *       return i2.concrete(tc) // = G<Int>
		 *    i2.inferTypes(c2, tc) // tc[V] = Int
		 *    return i2.concrete(tc) // H<Int>
		 */

		if (thisClass == concrete.getTheClass())
		{
			thisClass.getThisType().inferTypes(concrete, context);
			return concrete;
		}

		for (final IType superType : superTypes(thisClass))
		{
			final IType concreteSuperType = inferRecursively(superType.getTheClass(), concrete, context);
			if (concreteSuperType != null)
			{
				superType.inferTypes(concreteSuperType, context);
				return thisClass.getThisType().getConcreteType(context);
			}
		}

		return null;
	}

	private static Iterable<IType> superTypes(IClass iclass)
	{
		final IType superType = iclass.getSuperType();
		return superType == null ? iclass.getInterfaces() : Iterables.prepend(superType, iclass.getInterfaces());
	}

	private void checkReturnType(MarkerList markers, IType expectedReturnType)
	{
		if (this.hasImplicitReturnType())
		{
			this.returnType = expectedReturnType;
			return;
		}

		if (!Types.isSuperType(expectedReturnType, this.returnType))
		{
			markers.add(TypeChecker.typeError(this.returnType.getPosition(), expectedReturnType, this.returnType,
			                                  "lambda.return_type.incompatible", "return.type", "lambda.return_type"));
		}
	}

	// --------------- Context ---------------

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.get(name);
	}

	@Override
	public IAccessible getAccessibleThis(IType type)
	{
		this.getCaptureHelper().setThisType(type);
		return VariableThis.DEFAULT;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.parameters.isParameter(variable) //
		       || this.captureHelper != null && this.captureHelper.isMember(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.getCaptureHelper().capture(variable);
	}

	@Override
	public byte checkException(IType type)
	{
		return this.method != null ? this.method.checkException(type) : FALSE;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.parameters.resolveTypes(markers, context);

		if (!this.hasImplicitReturnType())
		{
			this.returnType = this.returnType.resolveType(markers, context);
		}

		if (this.value != null)
		{
			context = context.push(this);
			this.value.resolveTypes(markers, context);
			context.pop();
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "lambda.value.invalid"));
			this.value = new WildcardValue(this.position);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		boolean delayResolve = false;

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IParameter parameter = this.parameters.get(i);

			if (parameter.getType().isUninferred())
			{
				parameter.setType(null); // avoid missing parameter type error
				parameter.resolve(markers, context);
				parameter.setType(Types.UNKNOWN);

				// Resolving the value happens in withType
				delayResolve = true;
			}
			else
			{
				parameter.resolve(markers, context);
			}
		}

		if (!this.hasImplicitReturnType())
		{
			this.returnType.resolve(markers, context);
		}

		if (delayResolve)
		{
			return this;
		}

		// All parameter types are known, we can actually resolve the return value now

		context = context.push(this);
		this.value = this.value.resolve(markers, context);
		context.pop();

		this.flags |= VALUE_RESOLVED;
		if (this.hasImplicitReturnType())
		{
			this.returnType = this.value.getType();
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.parameters.checkTypes(markers, context);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.checkType(markers, context, IType.TypePosition.RETURN_TYPE);
		}

		context = context.push(this);
		this.value.checkTypes(markers, context);
		context = context.pop();

		if (this.captureHelper != null)
		{
			this.captureHelper.checkCaptures(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.parameters.check(markers, context);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.check(markers, context);
		}

		context = context.push(this);
		this.value.check(markers, context);
		context.pop();
	}

	@Override
	public IValue foldConstants()
	{
		this.parameters.foldConstants();

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.foldConstants();
		}

		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.parameters.cleanup(compilableList, classCompilableList);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.cleanup(compilableList, classCompilableList);
		}

		this.value = this.value.cleanup(compilableList, classCompilableList);

		if (this.captureHelper == null || !this.captureHelper.hasCaptures())
		{
			// Check if we can use a direct method reference
			if (this.value instanceof AbstractCall)
			{
				final AbstractCall call = (AbstractCall) this.value;
				final IMethod method = call.getMethod();

				if (method != null && this.checkCall(call.getReceiver(), call.getArguments(), method))
				{
					this.setHandleType(ClassFormat.insnToHandle(method.getInvokeOpcode()));
					this.name = method.getInternalName();
					this.owner = method.getEnclosingClass().getInternalName();
					this.descriptor = method.getDescriptor();
					return this;
				}
			}
			// To avoid trouble with anonymous classes
			else if (this.value.getClass() == ConstructorCall.class)
			{
				final ConstructorCall call = (ConstructorCall) this.value;
				final IConstructor constructor = call.getConstructor();

				if (constructor != null && this.checkCall(null, call.getArguments(), constructor))
				{
					this.setHandleType(ClassFormat.H_NEWINVOKESPECIAL);
					this.name = constructor.getInternalName();
					this.owner = constructor.getEnclosingClass().getInternalName();
					this.descriptor = constructor.getDescriptor();
					return this;
				}
			}
		}

		this.owner = classCompilableList.getInternalName();
		this.name = "lambda$" + classCompilableList.classCompilableCount();
		classCompilableList.addClassCompilable(this);

		return this;
	}

	// --------------- Direct Reference Transformation (via cleanup) ---------------

	private boolean checkCall(IValue receiver, ArgumentList arguments, IMethod method)
	{
		if (method.isTypeParametric())
		{
			for (ITypeParameter param : method.getTypeParameters())
			{
				if (param.getReifiedKind() != null)
				{
					// do not use a direct method reference if the method has a reified type parameter
					return false;
				}
			}
		}

		return this.checkCall(receiver, arguments, (IParametric) method);
	}

	private boolean checkCall(IValue receiver, ArgumentList arguments, IParametric parametric)
	{
		final int parameterCount = this.parameters.size();
		final ParameterList parameterList = parametric.getParameters();

		if (receiver == null)
		{
			if (arguments.size() != parameterCount)
			{
				return false;
			}

			for (int i = 0; i < parameterCount; i++)
			{
				final IValue argument = arguments.get(parameterList.get(i));
				if (!isFieldAccess(argument, this.parameters.get(i)))
				{
					return false;
				}
			}

			return true;
		}

		if (arguments.size() != parameterCount - 1 || !isFieldAccess(receiver, this.parameters.get(0)))
		{
			return false;
		}

		for (int i = 1; i < parameterCount; i++)
		{
			final IValue argument = arguments.get(parameterList.get(i - 1));
			if (!isFieldAccess(argument, this.parameters.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	private static boolean isFieldAccess(IValue value, IDataMember member)
	{
		return value instanceof FieldAccess && ((FieldAccess) value).getField() == member;
	}

	// --------------- Expression Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		int handleType = this.getHandleType();
		if (handleType == 0)
		{
			handleType = ClassFormat.H_INVOKESTATIC;
			if (this.captureHelper != null)
			{
				this.captureHelper.writeCaptures(writer, this.lineNumber());
			}
		}

		final String desc = this.getTargetDescriptor();
		final String invokedName = this.method.getInternalName();
		final String invokedType = this.getInvokeDescriptor();

		final Type methodDescriptorType = Type.getMethodType(this.method.getDescriptor());
		final Type lambdaDescriptorType = Type.getMethodType(this.getLambdaDescriptor());
		final Handle handle = new Handle(handleType, this.owner, this.name, desc);

		writer.visitLineNumber(this.lineNumber());
		writer.visitInvokeDynamicInsn(invokedName, invokedType, BOOTSTRAP, methodDescriptorType, handle,
		                              lambdaDescriptorType);

		if (type != null)
		{
			this.type.writeCast(writer, type, this.lineNumber());
		}
	}

	// --------------- Method Compilation ---------------

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.getHandleType() != 0)
		{
			return;
		}

		final boolean thisCaptured = this.captureHelper != null && this.captureHelper.isThisCaptured();

		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
			Modifiers.PRIVATE | Modifiers.STATIC, this.name, this.getTargetDescriptor(), null, null));

		if (this.captureHelper != null)
		{
			final int index;

			if (thisCaptured)
			{
				methodWriter.setLocalType(0, this.captureHelper.getThisType().getFrameType());
				index = 1;
			}
			else
			{
				index = 0;
			}

			this.captureHelper.writeCaptureParameters(methodWriter, index);
		}

		this.parameters.write(methodWriter);

		// Write the Value

		methodWriter.visitCode();
		this.value.writeExpression(methodWriter, this.returnType);
		methodWriter.visitEnd(this.returnType);
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		IParameter parameter;
		final int parameterCount = this.parameters.size();

		if (parameterCount == 1 && (parameter = this.parameters.get(0)).getType().isUninferred() // single parameter
		    && !Formatting.getBoolean("lambda.single.wrap"))
		{
			buffer.append(parameter.getName());

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (parameterCount > 0)
		{
			buffer.append('(');
			if (Formatting.getBoolean("lambda.open_paren.space_after"))
			{
				buffer.append(' ');
			}

			Util.astToString(prefix, this.parameters.getParameters(), parameterCount,
			                 Formatting.getSeparator("lambda.separator", ','), buffer);

			if (Formatting.getBoolean("lambda.close_paren.space_before"))
			{
				buffer.append(' ');
			}

			buffer.append(')');

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (Formatting.getBoolean("lambda.empty.wrap"))
		{
			buffer.append("()");
			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append("=>");
		if (Formatting.getBoolean("lambda.arrow.space_after"))
		{
			buffer.append(' ');
		}

		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
		}
	}
}
