package dyvil.tools.compiler.ast.method;

import dyvil.annotation.mutating;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Handle;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.external.ExternalMethod;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;

public abstract class AbstractMethod extends Member implements IMethod, ILabelContext
{
	static final Handle EXTENSION_BSM = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/DynamicLinker",
	                                               "linkExtension",
	                                               "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/CallSite;");

	static final int VARARGS_MATCH = 100;
	
	protected ITypeVariable[] generics;
	protected int             genericCount;
	
	protected IParameter[] parameters = new MethodParameter[3];
	protected int parameterCount;
	
	protected IType[] exceptions;
	protected int     exceptionCount;
	
	protected IValue value;
	
	// Metadata
	protected IClass        theClass;
	protected String        descriptor;
	protected IntrinsicData intrinsicData;

	protected boolean sideEffects = true;
	
	public AbstractMethod(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public AbstractMethod(IClass iclass, Name name)
	{
		this.theClass = iclass;
		this.name = name;
	}
	
	public AbstractMethod(IClass iclass, Name name, IType type)
	{
		this.theClass = iclass;
		this.type = type;
		this.name = name;
	}
	
	public AbstractMethod(IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.theClass = iclass;
	}
	
	public AbstractMethod(ICodePosition position, IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.theClass = iclass;
		this.position = position;
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
	public void setGeneric()
	{
		this.generics = new ITypeVariable[2];
	}
	
	@Override
	public boolean isGeneric()
	{
		return this.genericCount > 0;
	}
	
	@Override
	public int genericCount()
	{
		return this.genericCount;
	}
	
	@Override
	public void setTypeVariables(ITypeVariable[] typeVars, int count)
	{
		this.generics = typeVars;
		this.genericCount = count;
	}
	
	@Override
	public void setTypeVariable(int index, ITypeVariable var)
	{
		this.generics[index] = var;
	}
	
	@Override
	public void addTypeVariable(ITypeVariable var)
	{
		if (this.generics == null)
		{
			this.generics = new ITypeVariable[3];
			this.generics[0] = var;
			this.genericCount = 1;
			return;
		}
		
		int index = this.genericCount++;
		if (this.genericCount > this.generics.length)
		{
			ITypeVariable[] temp = new ITypeVariable[this.genericCount];
			System.arraycopy(this.generics, 0, temp, 0, index);
			this.generics = temp;
		}
		this.generics[index] = var;
		
		var.setIndex(index);
	}
	
	@Override
	public ITypeVariable[] getTypeVariables()
	{
		return this.generics;
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		return this.generics[index];
	}
	
	@Override
	public void setVarargs()
	{
		this.modifiers.addIntModifier(Modifiers.VARARGS);
	}
	
	@Override
	public boolean isVarargs()
	{
		return this.modifiers.hasIntModifier(Modifiers.VARARGS);
	}
	
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
		case "dyvil/annotation/_internal/inline":
			this.modifiers.addIntModifier(Modifiers.INLINE);
			return false;
		case "dyvil/annotation/_internal/infix":
		case "dyvil/annotation/_internal/postfix":
			this.modifiers.addIntModifier(Modifiers.INFIX);
			return false;
		case "dyvil/annotation/_internal/extension":
			this.modifiers.addIntModifier(Modifiers.EXTENSION);
			return false;
		case "dyvil/annotation/_internal/prefix":
			this.modifiers.addIntModifier(Modifiers.PREFIX);
			return false;
		case "dyvil/annotation/_internal/internal":
			this.modifiers.addIntModifier(Modifiers.INTERNAL);
			return false;
		case "dyvil/annotation/_internal/sealed":
			this.modifiers.addIntModifier(Modifiers.SEALED);
			return false;
		case "dyvil/annotation/Native":
			this.modifiers.addIntModifier(Modifiers.NATIVE);
			return false;
		case "dyvil/annotation/Strict":
			this.modifiers.addIntModifier(Modifiers.STRICT);
			return false;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		case "java/lang/Override":
			this.modifiers.addIntModifier(Modifiers.OVERRIDE);
			return false;
		case "dyvil/annotation/Intrinsic":
			if (annotation != null)
			{
				this.intrinsicData = Intrinsics.readAnnotation(this, annotation);
				return this.getClass() != ExternalMethod.class;
			}
			return true;
		case "dyvil/annotation/pure":
			this.sideEffects = false;
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
	public boolean isStatic()
	{
		return this.modifiers.hasIntModifier(Modifiers.STATIC);
	}
	
	@Override
	public boolean isAbstract()
	{
		return this.modifiers.hasIntModifier(Modifiers.ABSTRACT) && !this.isObjectMethod();
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.sideEffects;
	}

	@Override
	public void setHasSideEffects(boolean sideEffects)
	{
		this.sideEffects = sideEffects;
	}

	protected boolean isObjectMethod()
	{
		switch (this.parameterCount)
		{
		case 0:
			if (this.name == Names.toString)
			{
				return true;
			}
			return this.name == Names.hashCode;
		case 1:
			if (this.name == Names.equals && this.parameters[0].getType().getTheClass() == Types.OBJECT_CLASS)
			{
				return true;
			}
		}
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
		return this.theClass;
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
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return new TypeVarType(var);
			}
		}
		
		return this.theClass.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
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
	public dyvil.tools.compiler.ast.statement.control.Label resolveLabel(Name name)
	{
		return null;
	}
	
	@Override
	public ILoop getEnclosingLoop()
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		float selfMatch = this.getSignatureMatch(name, instance, arguments);
		if (selfMatch > 0)
		{
			list.add(this, selfMatch);
		}
		
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
	public float getSignatureMatch(Name name, IValue receiver, IArguments arguments)
	{
		if (name != this.name && name != null)
		{
			return 0;
		}
		
		// Only matching the name
		if (arguments == null)
		{
			return 1;
		}
		
		int parameterStartIndex = 0;
		int totalMatch = 1;
		int argumentCount = arguments.size();
		
		// infix modifier implementation
		if (receiver != null)
		{
			int mod = this.modifiers.toFlags() & Modifiers.INFIX;
			if (mod != 0 && receiver.valueTag() == IValue.CLASS_ACCESS)
			{
				receiver = null;
			}
			else if (mod == Modifiers.INFIX)
			{
				IType t2 = this.parameters[0].getType();
				float receiverMatch = receiver.getTypeMatch(t2);
				if (receiverMatch == 0)
				{
					return 0;
				}
				totalMatch += receiverMatch;
				
				parameterStartIndex = 1;
			}
			else if (mod == Modifiers.STATIC && receiver.valueTag() != IValue.CLASS_ACCESS)
			{
				// Disallow non-static access to static method
				return 0;
			}
			else
			{
				float receiverMatch = receiver.getTypeMatch(this.theClass.getClassType());
				if (receiverMatch <= 0)
				{
					return 0;
				}
				totalMatch += receiverMatch;
			}
		}
		if (this.isVarargs())
		{
			int varargsStart = this.parameterCount - 1 - parameterStartIndex;
			
			for (int i = parameterStartIndex; i < varargsStart; i++)
			{
				IParameter par = this.parameters[i + parameterStartIndex];
				float valueMatch = arguments.getTypeMatch(i, par);
				if (valueMatch <= 0)
				{
					return 0;
				}
				totalMatch += valueMatch;
			}
			
			IParameter varParam = this.parameters[varargsStart + parameterStartIndex];
			float varargsMatch = arguments.getVarargsTypeMatch(varargsStart, varParam);
			if (varargsMatch <= 0)
			{
				return 0;
			}
			return totalMatch + varargsMatch;
		}
		
		int parameterLeft = this.parameterCount - parameterStartIndex;
		if (argumentCount > parameterLeft)
		{
			return 0;
		}
		
		for (int argumentIndex = 0; argumentIndex < parameterLeft; argumentIndex++)
		{
			IParameter par = this.parameters[argumentIndex + parameterStartIndex];
			float valueMatch = arguments.getTypeMatch(argumentIndex, par);
			if (valueMatch <= 0)
			{
				return 0;
			}
			totalMatch += valueMatch;
		}
		
		return totalMatch;
	}
	
	@Override
	public GenericData getGenericData(GenericData genericData, IValue instance, IArguments arguments)
	{
		if (!this.hasTypeVariables())
		{
			return genericData;
		}
		
		if (genericData == null)
		{
			genericData = new GenericData(this, this.genericCount);
			
			this.inferTypes(genericData, instance, arguments);
			
			return genericData;
		}
		
		genericData.method = this;
		
		genericData.setTypeCount(this.genericCount);
		this.inferTypes(genericData, instance, arguments);
		
		return genericData;
	}
	
	@Override
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		int len = arguments.size();
		
		if (this.modifiers.hasIntModifier(Modifiers.PREFIX) && !this.isStatic())
		{
			IValue argument = arguments.getFirstValue();
			arguments.setFirstValue(instance);
			instance = argument;
		}
		
		if (instance != null)
		{
			int mod = this.modifiers.toFlags() & Modifiers.INFIX;
			if (mod == Modifiers.INFIX && instance.valueTag() != IValue.CLASS_ACCESS)
			{
				IParameter par = this.parameters[0];
				IValue instance1 = IType.convertValue(instance, par.getType(), typeContext, markers, context);
				if (instance1 == null)
				{
					Util.createTypeError(markers, instance, par.getType(), typeContext, "method.access.infix_type",
					                     par.getName());
				}
				else
				{
					instance = instance1;
				}
				
				if (this.isVarargs())
				{
					arguments.checkVarargsValue(this.parameterCount - 2, this.parameters[this.parameterCount - 1],
					                            typeContext, markers, context);
					
					for (int i = 0; i < this.parameterCount - 2; i++)
					{
						arguments.checkValue(i, this.parameters[i + 1], typeContext, markers, context);
					}
					
					this.checkTypeVarsInferred(markers, position, typeContext);
					return instance;
				}
				
				for (int i = 0; i < this.parameterCount - 1; i++)
				{
					arguments.checkValue(i, this.parameters[i + 1], typeContext, markers, context);
				}
				
				this.checkTypeVarsInferred(markers, position, typeContext);
				return instance;
			}
			
			if ((mod & Modifiers.STATIC) != 0)
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(I18n.createMarker(position, "method.access.static", this.name));
				}
				else if (instance.getType().getTheClass() != this.theClass)
				{
					markers.add(I18n.createMarker(position, "method.access.static.type", this.name,
					                              this.theClass.getFullName()));
				}
				instance = null;
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				if (!instance.getType().getTheClass().isObject())
				{
					markers.add(I18n.createMarker(position, "method.access.instance", this.name));
				}
			}
			else
			{
				IType type = this.theClass.getClassType();
				IValue instance1 = IType.convertValue(instance, type, typeContext, markers, context);
				
				if (instance1 == null)
				{
					Util.createTypeError(markers, instance, type, typeContext, "method.access.receiver_type",
					                     this.name);
				}
				else
				{
					instance = instance1;
				}
			}
		}
		else if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			if (context.isStatic())
			{
				markers.add(I18n.createMarker(position, "method.access.instance", this.name));
			}
			else
			{
				markers.add(I18n.createMarker(position, "method.access.unqualified", this.name.unqualified));
				instance = new ThisExpr(position, this.theClass.getType(), context, markers);
			}
		}
		
		if (this.isVarargs())
		{
			len = this.parameterCount - 1;
			arguments.checkVarargsValue(len, this.parameters[len], typeContext, markers, null);
			
			for (int i = 0; i < len; i++)
			{
				arguments.checkValue(i, this.parameters[i], typeContext, markers, context);
			}
			
			this.checkTypeVarsInferred(markers, position, typeContext);
			return instance;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.checkValue(i, this.parameters[i], typeContext, markers, context);
		}
		
		this.checkTypeVarsInferred(markers, position, typeContext);
		return instance;
	}
	
	private void inferTypes(GenericData genericData, IValue instance, IArguments arguments)
	{
		if (instance != null)
		{
			genericData.instance = instance;
		}
		else
		{
			genericData.instance = new ThisExpr(this.theClass.getType());
		}

		int modifiers = this.modifiers.toFlags();
		
		int parIndex = 0;
		IParameter param;
		if (instance != null && (modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			this.parameters[0].getType().inferTypes(instance.getType(), genericData);
			parIndex = 1;
		}
		
		if ((modifiers & Modifiers.VARARGS) != 0)
		{
			int len = this.parameterCount - parIndex - 1;
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i + parIndex];
				arguments.inferType(i, param, genericData);
			}
			
			arguments.inferVarargsType(len, this.parameters[len + parIndex], genericData);
			return;
		}
		
		int len = this.parameterCount - parIndex;
		for (int i = 0; i < len; i++)
		{
			param = this.parameters[i + parIndex];
			arguments.inferType(i, param, genericData);
		}
	}
	
	private void checkTypeVarsInferred(MarkerList markers, ICodePosition position, ITypeContext typeContext)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable typeVar = this.generics[i];
			IType type = typeContext.resolveType(typeVar);
			if (type == null || type.typeTag() == IType.TYPE_VAR_TYPE && type.getTypeVariable() == typeVar)
			{
				markers.add(I18n.createMarker(position, "method.typevar.infer", this.name, typeVar.getName()));
				typeContext.addMapping(typeVar, Types.ANY);
			}
		}
	}
	
	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		Deprecation.checkAnnotations(markers, position, this, "method");

		switch (IContext.getVisibility(context, this))
		{
		case IContext.INTERNAL:
			markers.add(I18n.createMarker(position, "method.access.internal", this.name));
			break;
		case IContext.INVISIBLE:
			markers.add(I18n.createMarker(position, "method.access.invisible", this.name));
			break;
		}
		
		if (instance != null)
		{
			this.checkMutating(markers, instance);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			if (IContext.isUnhandled(context, exceptionType))
			{
				markers.add(I18n.createMarker(position, "exception.unhandled", exceptionType.toString()));
			}
		}
	}
	
	private void checkMutating(MarkerList markers, IValue instance)
	{
		IType type = instance.getType();
		if (!Types.IMMUTABLE.isSuperTypeOf(type))
		{
			return;
		}
		
		IAnnotation a = this.getAnnotation(Types.MUTATING_CLASS);
		if (a == null)
		{
			return;
		}
		
		IValue v = a.getArguments().getValue(0, Annotation.VALUE);
		String s = v != null ? v.stringValue() : mutating.VALUE_DEFAULT;
		StringBuilder builder = new StringBuilder(s);
		
		int index = builder.indexOf("{method}");
		if (index >= 0)
		{
			builder.replace(index, index + 8, this.name.unqualified);
		}
		
		index = builder.indexOf("{type}");
		if (index >= 0)
		{
			builder.replace(index, index + 6, type.toString());
		}
		
		markers.add(new SemanticError(instance.getPosition(), builder.toString()));
	}
	
	@Override
	public boolean checkOverride(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext)
	{
		// Check Name
		if (candidate.getName() != this.name)
		{
			return false;
		}
		
		// Check Parameter Count
		if (candidate.parameterCount() != this.parameterCount)
		{
			return false;
		}
		// Check Parameter Types
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType parType = this.parameters[i].getType();
			IType methodParType = candidate.getParameter(i).getType().getConcreteType(typeContext);
			if (!parType.isSameType(methodParType))
			{
				return false;
			}
		}
		
		if (iclass == this.theClass)
		{
			this.addOverride(candidate);
		}
		return true;
	}
	
	protected abstract void addOverride(IMethod override);
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.genericCount > 0 || this.theClass.isGeneric();
	}
	
	@Override
	public boolean isIntrinsic()
	{
		return this.intrinsicData != null;
	}
	
	@Override
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getActualType().appendExtendedName(buffer);
		}
		buffer.append(')');
		this.type.appendExtendedName(buffer);
		return this.descriptor = buffer.toString();
	}
	
	private boolean needsSignature()
	{
		if (this.genericCount != 0 || this.theClass.isGeneric() || this.type.isGenericType())
		{
			return true;
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameters[i].getActualType().isGenericType())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getSignature()
	{
		if (!this.needsSignature())
		{
			return null;
		}
		
		StringBuilder buffer = new StringBuilder();
		if (this.genericCount > 0)
		{
			buffer.append('<');
			for (int i = 0; i < this.genericCount; i++)
			{
				this.generics[i].appendSignature(buffer);
			}
			buffer.append('>');
		}
		
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getActualType().appendSignature(buffer);
		}
		buffer.append(')');
		this.type.appendSignature(buffer);
		return buffer.toString();
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
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type, int lineNumber)
			throws BytecodeException
	{
		if (this.intrinsicData != null)
		{
			this.intrinsicData.writeIntrinsic(writer, instance, arguments, lineNumber);
		}
		else
		{
			this.writeArgumentsAndInvoke(writer, instance, arguments, lineNumber);
		}
		
		if (type == Types.VOID)
		{
			if (this.type != Types.VOID)
			{
				writer.writeInsn(Opcodes.AUTO_POP);
			}
			return;
		}
		
		if (type != null)
		{
			this.type.writeCast(writer, type, lineNumber);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		if (this.intrinsicData != null)
		{
			this.intrinsicData.writeIntrinsic(writer, dest, instance, arguments, lineNumber);
			return;
		}
		
		this.writeArgumentsAndInvoke(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(IFNE, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		if (this.intrinsicData != null)
		{
			this.intrinsicData.writeInvIntrinsic(writer, dest, instance, arguments, lineNumber);
			return;
		}
		
		this.writeArgumentsAndInvoke(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(IFEQ, dest);
	}
	
	private void writeReceiver(MethodWriter writer, IValue receiver) throws BytecodeException
	{
		if (receiver != null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.INFIX))
			{
				receiver.writeExpression(writer, this.parameters[0].getType());
				return;
			}
			
			if (receiver.isPrimitive() && this.intrinsicData != null)
			{
				receiver.writeExpression(writer, null);
				return;
			}
			
			receiver.writeExpression(writer, this.theClass.getType());
		}
	}
	
	private void writeArguments(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		int parIndex = 0;
		int modifiers = this.modifiers.toFlags();
		
		if (instance != null && (modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			parIndex = 1;
		}
		
		if ((modifiers & Modifiers.VARARGS) != 0)
		{
			int len = this.parameterCount - 1 - parIndex;
			if (len < 0)
			{
				return;
			}
			
			IParameter param;
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i + parIndex];
				arguments.writeValue(i, param, writer);
			}
			param = this.parameters[len];
			arguments.writeVarargsValue(len, param, writer);
			return;
		}
		
		int len = this.parameterCount - parIndex;
		for (int i = 0; i < len; i++)
		{
			IParameter param = this.parameters[i + parIndex];
			arguments.writeValue(i, param, writer);
		}
	}
	
	private void writeArgumentsAndInvoke(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		this.writeReceiver(writer, instance);
		this.writeArguments(writer, instance, arguments);
		this.writeInvoke(writer, instance, arguments, lineNumber);
	}
	
	@Override
	public void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		writer.writeLineNumber(lineNumber);
		
		int opcode;
		int modifiers = this.modifiers.toFlags();
		
		String owner = this.theClass.getInternalName();
		if ((modifiers & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			writer.writeInvokeDynamic(this.name.qualified, this.getDescriptor(), EXTENSION_BSM,
			                          new Handle(ClassFormat.H_INVOKESTATIC, owner, this.name.qualified,
			                                     this.getDescriptor()));
			return;
		}
		
		if (instance != null && instance.valueTag() == IValue.SUPER)
		{
			opcode = Opcodes.INVOKESPECIAL;
		}
		else
		{
			opcode = this.getInvokeOpcode();
		}
		
		String name = this.name.qualified;
		String desc = this.getDescriptor();
		writer.writeInvokeInsn(opcode, owner, name, desc, this.theClass.isInterface());
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
		if (this.theClass.isInterface())
		{
			return Opcodes.INVOKEINTERFACE;
		}
		return Opcodes.INVOKEVIRTUAL;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		this.modifiers.toString(buffer);
		if (this.type != null)
		{
			this.type.toString("", buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
		if (this.genericCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '[');
			Util.astToString(prefix, this.generics, this.genericCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", ']');
		}
		
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');
		Util.astToString(prefix, this.parameters, this.parameterCount,
		                 Formatting.getSeparator("parameters.separator", ','), buffer);
		Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
		
		if (this.exceptionCount > 0)
		{
			String throwsPrefix = prefix;
			if (Formatting.getBoolean("method.throws.newline"))
			{
				throwsPrefix = Formatting.getIndent("method.throws.indent", prefix);
				buffer.append('\n').append(throwsPrefix).append("throws ");
			}
			else
			{
				buffer.append(" throws ");
			}

			Util.astToString(throwsPrefix, this.exceptions, this.exceptionCount,
			                 Formatting.getSeparator("method.throws", ','), buffer);
		}
		
		if (this.value != null)
		{
			if (Util.formatStatementList(prefix, buffer, this.value))
			{
				return;
			}

			if (Formatting.getBoolean("method.declaration.space_before"))
			{
				buffer.append(' ');
			}

			buffer.append('=');

			String valuePrefix = Formatting.getIndent("method.declaration.indent", prefix);
			if (Formatting.getBoolean("method.declaration.newline_after"))
			{
				buffer.append('\n').append(valuePrefix);
			}
			else if (Formatting.getBoolean("method.declaration.space_after"))
			{
				buffer.append(' ');
			}

			this.value.toString(prefix, buffer);
		}

		if (Formatting.getBoolean("method.semicolon"))
		{
			buffer.append(';');
		}
	}
}
