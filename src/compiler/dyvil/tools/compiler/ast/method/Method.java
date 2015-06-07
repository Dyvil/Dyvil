package dyvil.tools.compiler.ast.method;

import static dyvil.reflect.Opcodes.ARGUMENTS;
import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;
import static dyvil.reflect.Opcodes.INSTANCE;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.Label;

import dyvil.annotation.mutating;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constant.IntValue;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	protected IClass			theClass;
	
	protected ITypeVariable[]	generics;
	protected int				genericCount;
	
	protected IParameter[]		parameters	= new MethodParameter[3];
	protected int				parameterCount;
	protected IType[]			exceptions;
	protected int				exceptionCount;
	
	public IValue				value;
	
	public String				descriptor;
	protected int[]				intrinsicOpcodes;
	protected IMethod			overrideMethod;
	
	public Method(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Method(IClass iclass, Name name)
	{
		this.theClass = iclass;
		this.name = name;
	}
	
	public Method(IClass iclass, Name name, IType type)
	{
		this.theClass = iclass;
		this.type = type;
		this.name = name;
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
	public boolean addRawAnnotation(String type)
	{
		switch (type)
		{
		case "dyvil/annotation/inline":
			this.modifiers |= Modifiers.INLINE;
			return false;
		case "dyvil/annotation/infix":
		case "dyvil/annotation/postfix":
			this.modifiers |= Modifiers.INFIX;
			return false;
		case "dyvil/annotation/prefix":
			this.modifiers |= Modifiers.PREFIX;
			return false;
		case "dyvil/annotation/sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "dyvil/annotation/Native":
			this.modifiers |= Modifiers.NATIVE;
			return false;
		case "dyvil/annotation/Strict":
			this.modifiers |= Modifiers.STRICT;
			return false;
		case "java/lang/Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		case "java/lang/Override":
			this.modifiers |= Modifiers.OVERRIDE;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.METHOD;
	}
	
	@Override
	public void exceptionCount()
	{
	}
	
	@Override
	public void setException(int index, IType exception)
	{
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
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(markers, context);
		}
		
		super.resolveTypes(markers, this);
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation annotation = this.annotations[i];
			if (annotation.type.getTheClass() != Types.INTRINSIC_CLASS)
			{
				continue;
			}
			
			try
			{
				Array array = (Array) annotation.arguments.getValue(0, Annotation.VALUE);
				
				int len = array.valueCount();
				int[] opcodes = new int[len];
				for (int j = 0; j < len; j++)
				{
					IntValue v = (IntValue) array.getValue(j);
					opcodes[j] = v.value;
				}
				this.intrinsicOpcodes = opcodes;
			}
			catch (NullPointerException | ClassCastException ex)
			{
			}
			break;
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolve(markers, this);
		}
		
		int index = (this.modifiers & Modifiers.STATIC) == 0 ? 1 : 0;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(markers, this);
			param.setIndex(index);
			
			IType type = param.getType();
			if (type == Types.LONG || type == Types.DOUBLE)
			{
				index += 2;
			}
			else
			{
				index++;
			}
		}
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, this);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
			
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "method.type.infer", this.name.unqualified);
				}
				return;
			}
			
			IValue value1 = this.value.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.position, "method.type", this.name.unqualified);
				marker.addInfo("Return Type: " + this.type);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(this.position, "method.type.abstract", this.name.unqualified);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}
		
		if (this.value != null)
		{
			this.value.checkTypes(markers, this);
		}
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0)
		{
			this.modifiers |= Modifiers.ABSTRACT;
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			this.checkOverride(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType t = this.exceptions[i];
			if (!Types.THROWABLE.isSuperTypeOf(t))
			{
				Marker m = markers.create(t.getPosition(), "method.exception.type");
				m.addInfo("Exception Type: " + t);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		// If the method does not have an implementation and is static
		else if (this.isStatic())
		{
			markers.add(this.position, "method.static", this.name);
		}
		// Or not declared abstract and a member of a non-abstract class
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0 && !this.theClass.isAbstract())
		{
			markers.add(this.position, "method.unimplemented", this.name);
		}
		
		// Check for duplicate methods
		
		String desc = this.getDescriptor();
		IClassBody body = this.theClass.getBody();
		for (int i = body.methodCount() - 1; i >= 0; i--)
		{
			IMethod m = body.getMethod(i);
			if (m != this && m.getName() == this.name)
			{
				if (m.getDescriptor().equals(desc))
				{
					markers.add(this.position, "method.duplicate", this.name, desc);
				}
			}
		}
	}
	
	private void checkOverride(MarkerList markers, IContext context)
	{
		this.overrideMethod = this.theClass.getSuperMethod(this.name, this.parameters, this.parameterCount);
		if (this.overrideMethod == null)
		{
			if ((this.modifiers & Modifiers.OVERRIDE) != 0)
			{
				markers.add(this.position, "method.override", this.name);
			}
			return;
		}
		
		if ((this.modifiers & Modifiers.OVERRIDE) == 0)
		{
			markers.add(this.position, "method.overrides", this.name);
		}
		else if (this.overrideMethod.hasModifier(Modifiers.FINAL))
		{
			markers.add(this.position, "method.override.final", this.name);
		}
		else
		{
			IType type = this.overrideMethod.getType();
			if (type != this.type && !type.isSuperTypeOf(this.type))
			{
				Marker marker = markers.create(this.position, "method.override.type", this.name);
				marker.addInfo("Return Type: " + this.type);
				marker.addInfo("Overriden Return Type: " + type);
			}
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
		
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return (this.modifiers & Modifiers.STATIC) != 0;
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
	public IField resolveField(Name name)
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
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
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
	public byte getVisibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null)
		{
			return VISIBLE;
		}
		if ((this.modifiers & Modifiers.STATIC) != 0 && iclass == this.theClass && !member.hasModifier(Modifiers.STATIC))
		{
			return STATIC;
		}
		return this.theClass.getVisibility(member);
	}
	
	@Override
	public int getSignatureMatch(Name name, IValue instance, IArguments arguments)
	{
		if (name != null && name != this.name)
		{
			return 0;
		}
		
		// Only matching the name
		if (arguments == null)
		{
			return 1;
		}
		
		if (instance == null && this.modifiers == Modifiers.PREFIX)
		{
			int m = arguments.getFirstValue().getTypeMatch(this.theClass.getType());
			if (m == 0)
			{
				return 0;
			}
			return 1 + m;
		}
		
		int parIndex = 0;
		int match = 1;
		int len = arguments.size();
		
		// infix modifier implementation
		int mods = this.modifiers & Modifiers.INFIX;
		if (instance != null && mods == Modifiers.INFIX)
		{
			IType t2 = this.parameters[0].getType();
			int m = instance.getTypeMatch(t2);
			if (m == 0)
			{
				return 0;
			}
			match += m;
			
			parIndex = 1;
		}
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int parCount = this.parameterCount - 1;
			if (len <= parCount)
			{
				return 0;
			}
			
			int m;
			IParameter varParam = this.parameters[parCount];
			for (int i = parIndex; i < parCount; i++)
			{
				IParameter par = this.parameters[i + parIndex];
				m = arguments.getTypeMatch(i, par);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			for (int i = parCount + parIndex; i < len; i++)
			{
				m = arguments.getVarargsTypeMatch(i, varParam);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			return match;
		}
		else if (len > this.parameterCount)
		{
			return 0;
		}
		
		for (int i = 0; parIndex < this.parameterCount; parIndex++, i++)
		{
			IParameter par = this.parameters[parIndex];
			int m = arguments.getTypeMatch(i, par);
			if (m == 0)
			{
				return 0;
			}
			match += m;
		}
		
		return match;
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
			genericData = new GenericData(this.genericCount);
			if (instance != null)
			{
				genericData.instanceType = instance.getType();
			}
			
			for (int i = 0; i < this.genericCount; i++)
			{
				genericData.generics[i] = this.inferType(this.generics[i], instance, arguments);
			}
			
			return genericData;
		}
		
		if (instance != null)
		{
			genericData.instanceType = instance.getType();
		}
		
		genericData.setTypeCount(this.genericCount);
		for (int i = genericData.typeCount(); i < this.genericCount; i++)
		{
			genericData.generics[i] = this.inferType(this.generics[i], instance, arguments);
		}
		
		return genericData;
	}
	
	@Override
	public IValue checkArguments(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		int len = arguments.size();
		IType parType;
		
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			IParameter par = this.parameters[0];
			parType = par.getType().getConcreteType(typeContext);
			IValue instance1 = instance.withType(parType);
			if (instance1 == null)
			{
				Marker marker = markers.create(instance.getPosition(), "method.access.infix_type", par.getName());
				marker.addInfo("Required Type: " + parType);
				marker.addInfo("Value Type: " + instance.getType());
			}
			else
			{
				instance = instance1;
			}
			
			if ((this.modifiers & Modifiers.VARARGS) != 0)
			{
				arguments.checkVarargsValue(this.parameterCount - 2, this.parameters[this.parameterCount - 1], markers, typeContext);
				
				for (int i = 0; i < this.parameterCount - 2; i++)
				{
					arguments.checkValue(i, this.parameters[i + 1], markers, typeContext);
				}
				return instance;
			}
			
			for (int i = 0; i < this.parameterCount - 1; i++)
			{
				arguments.checkValue(i, this.parameters[i + 1], markers, typeContext);
			}
			return instance;
		}
		else if (instance == null && (this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			parType = this.theClass.getType();
			instance = arguments.getFirstValue();
			IValue instance1 = instance.withType(parType);
			if (instance1 == null)
			{
				Marker marker = markers.create(instance.getPosition(), "method.access.prefix_type", this.name);
				marker.addInfo("Required Type: " + parType);
				marker.addInfo("Value Type: " + instance.getType());
			}
			return null;
		}
		
		if (instance != null)
		{
			if ((this.modifiers & Modifiers.STATIC) != 0)
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(position, "method.access.static", this.name.unqualified);
					instance = null;
				}
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(position, "method.access.instance", this.name.unqualified);
			}
			else if (this.intrinsicOpcodes == null && instance.isPrimitive())
			{
				instance = instance.withType(this.theClass.getType());
			}
		}
		else if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			if (context.isStatic())
			{
				markers.add(position, "method.access.instance", this.name);
			}
			else
			{
				markers.add(position, "method.access.unqualified", this.name.unqualified);
				instance = new ThisValue(position, context.getThisClass().getType());
			}
		}
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			len = this.parameterCount - 1;
			arguments.checkVarargsValue(len, this.parameters[len], markers, typeContext);
			
			for (int i = 0; i < len; i++)
			{
				arguments.checkValue(i, this.parameters[i], markers, typeContext);
			}
			return instance;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.checkValue(i, this.parameters[i], markers, typeContext);
		}
		return instance;
	}
	
	private IType inferType(ITypeVariable typeVar, IValue instance, IArguments arguments)
	{
		IType type;
		int len = arguments.size();
		IParameter param;
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			type = this.parameters[0].getType().resolveType(typeVar, instance.getType());
			if (type != null)
			{
				return type;
			}
			
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i + 1];
				type = param.getType().resolveType(typeVar, arguments.getType(i, param));
				if (type != null)
				{
					return type;
				}
			}
			
			return Types.ANY;
		}
		
		len = Math.min(this.parameterCount, len);
		for (int i = 0; i < len; i++)
		{
			param = this.parameters[i];
			type = param.getType().resolveType(typeVar, arguments.getType(i, param));
			if (type != null)
			{
				return type;
			}
		}
		return Types.ANY;
	}
	
	@Override
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			markers.add(position, "method.access.deprecated", this.name);
		}
		
		switch (context.getVisibility(this))
		{
		case IContext.STATIC:
			markers.add(position, "method.access.instance", this.name);
			break;
		case IContext.SEALED:
			markers.add(position, "method.access.sealed", this.name);
			break;
		case IContext.INVISIBLE:
			markers.add(position, "method.access.invisible", this.name);
			break;
		}
		
		if ((this.modifiers & Modifiers.PREFIX) != 0)
		{
			IValue value = arguments.getFirstValue();
			this.checkMutating(markers, value != null ? value : instance);
		}
		else if (instance != null)
		{
			this.checkMutating(markers, instance);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType type = this.exceptions[i];
			if (!Types.RUNTIME_EXCEPTION.isSuperTypeOf(type) && !context.handleException(type))
			{
				markers.add(position, "method.access.exception", type.toString());
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
		
		Annotation a = this.getAnnotation(Types.MUTATING_CLASS);
		if (a == null)
		{
			return;
		}
		
		IValue v = a.arguments.getValue(0, Annotation.VALUE);
		String s = v != null ? ((StringValue) v).value : mutating.VALUE_DEFAULT;
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
	public boolean hasTypeVariables()
	{
		return this.genericCount > 0 || this.theClass.isGeneric();
	}
	
	@Override
	public boolean isIntrinsic()
	{
		return this.intrinsicOpcodes != null;
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
			this.parameters[i].getType().appendExtendedName(buffer);
		}
		buffer.append(')');
		this.type.appendExtendedName(buffer);
		return this.descriptor = buffer.toString();
	}
	
	@Override
	public String getSignature()
	{
		if (this.genericCount == 0 && !this.theClass.isGeneric())
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
			this.parameters[i].getType().appendSignature(buffer);
		}
		buffer.append(')');
		this.type.appendSignature(buffer);
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
		int modifiers = this.modifiers & 0xFFFF;
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified, this.getDescriptor(), this.getSignature(),
				this.getExceptions()));
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.setThisType(this.theClass.getInternalName());
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.addAnnotation("Ldyvil/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.addAnnotation("Ldyvil/annotation/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.addAnnotation("Ldyvil/annotation/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.addAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
		{
			mw.addAnnotation("Ldyvil/annotation/sealed;", false);
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
			if (this.type == Types.VOID)
			{
				this.value.writeStatement(mw);
			}
			else
			{
				this.value.writeExpression(mw);
			}
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getIndex(), param.getName().qualified, param.getType(), start, end);
		}
		
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			return;
		}
		
		mw.writeLocal(0, "this", this.theClass.getType(), start, end);
		
		if (this.overrideMethod == null)
		{
			return;
		}
		
		// Check if a bridge method has to be generated
		if (this.descriptor.equals(this.overrideMethod.getDescriptor()))
		{
			return;
		}
		
		// Generate a bridge method
		mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers | Modifiers.SYNTHETIC | Modifiers.BRIDGE, this.name.qualified,
				this.overrideMethod.getDescriptor(), this.overrideMethod.getSignature(), this.overrideMethod.getExceptions()));
		
		start = new Label();
		end = new Label();
		
		mw.begin();
		mw.setThisType(this.theClass.getInternalName());
		mw.writeVarInsn(Opcodes.ALOAD, 0);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.overrideMethod.getParameter(i);
			IType type1 = this.parameters[i].getType();
			IType type2 = param.getType();
			
			param.write(mw);
			mw.writeVarInsn(type1.getLoadOpcode(), param.getIndex());
			if (!type1.equals(type2))
			{
				mw.writeTypeInsn(Opcodes.CHECKCAST, type1.getInternalName());
			}
		}
		
		mw.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, this.theClass.getInternalName(), this.name.qualified, this.getDescriptor(), false);
		mw.writeInsn(this.type.getReturnOpcode());
		mw.end();
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			if (instance != null && instance.valueTag() == IValue.CLASS_ACCESS)
			{
				instance = null;
			}
			// Intrinsic Case 1: Static (infix) Method, Instance not null
			if (this.intrinsicOpcodes != null)
			{
				this.writeIntrinsic(writer, instance, arguments);
				return;
			}
		}
		// Intrinsic Case 2: Member Method, Instance is Primitive
		else if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			this.writeIntrinsic(writer, instance, arguments);
			return;
		}
		
		this.writeArgumentsAndInvoke(writer, instance, arguments);
		
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
			if (type != this.type && !type.isSuperTypeOf(this.type))
			{
				writer.writeTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
			}
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			if (instance != null && instance.valueTag() == IValue.CLASS_ACCESS)
			{
				instance = null;
			}
			// Intrinsic Case 1: Static (infix) Method, Instance not null
			if (this.intrinsicOpcodes != null)
			{
				this.writeIntrinsic(writer, dest, instance, arguments);
				return;
			}
		}
		// Intrinsic Case 2: Member Method, Instance is Primitive
		else if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			this.writeIntrinsic(writer, dest, instance, arguments);
			return;
		}
		this.writeArgumentsAndInvoke(writer, instance, arguments);
		writer.writeJumpInsn(IFEQ, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			if (instance != null && instance.valueTag() == IValue.CLASS_ACCESS)
			{
				instance = null;
			}
			// Intrinsic Case 1: Static (infix) Method, Instance not null
			if (this.intrinsicOpcodes != null)
			{
				this.writeInvIntrinsic(writer, dest, instance, arguments);
				return;
			}
		}
		// Intrinsic Case 2: Member Method, Instance is Primitive
		else if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			this.writeInvIntrinsic(writer, dest, instance, arguments);
			return;
		}
		
		this.writeArgumentsAndInvoke(writer, instance, arguments);
		
		writer.writeJumpInsn(IFNE, dest);
	}
	
	private void writeArguments(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			int len = this.parameterCount;
			if ((this.modifiers & Modifiers.VARARGS) != 0)
			{
				len--;
				IParameter param;
				for (int i = 1, j = 0; i < len; i++, j++)
				{
					param = this.parameters[i];
					arguments.writeValue(j, param.getName(), param.getValue(), writer);
				}
				param = this.parameters[len];
				arguments.writeVarargsValue(len - 1, param.getName(), param.getType(), writer);
				return;
			}
			
			for (int i = 1, j = 0; i < this.parameterCount; i++, j++)
			{
				IParameter param = this.parameters[i];
				arguments.writeValue(j, param.getName(), param.getValue(), writer);
			}
			return;
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			arguments.writeValue(0, Name._this, null, writer);
			return;
		}
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int len = this.parameterCount - 1;
			IParameter param;
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i];
				arguments.writeValue(i, param.getName(), param.getValue(), writer);
			}
			param = this.parameters[len];
			arguments.writeVarargsValue(len, param.getName(), param.getType(), writer);
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			arguments.writeValue(i, param.getName(), param.getValue(), writer);
		}
	}
	
	private void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (this.type.getTheClass() == Types.BOOLEAN_CLASS)
		{
			int len = this.intrinsicOpcodes.length - 1;
			if (this.intrinsicOpcodes[len] == IFNE)
			{
				for (int i = 0; i < len; i++)
				{
					int insn = this.intrinsicOpcodes[i];
					if (insn == INSTANCE)
					{
						if (instance != null)
						{
							instance.writeExpression(writer);
						}
					}
					else if (insn == ARGUMENTS)
					{
						this.writeArguments(writer, instance, arguments);
					}
					else
					{
						writer.writeInsn(insn);
					}
				}
				return;
			}
			
			Label ifEnd = new Label();
			Label elseEnd = new Label();
			this.writeIntrinsic(writer, ifEnd, instance, arguments);
			
			// If Block
			writer.writeLDC(0);
			writer.writeJumpInsn(Opcodes.GOTO, elseEnd);
			writer.writeLabel(ifEnd);
			// Else Block
			writer.writeLDC(1);
			writer.writeLabel(elseEnd);
			return;
		}
		
		for (int i : this.intrinsicOpcodes)
		{
			if (i == INSTANCE)
			{
				if (instance != null)
				{
					instance.writeExpression(writer);
				}
			}
			else if (i == ARGUMENTS)
			{
				this.writeArguments(writer, instance, arguments);
			}
			else
			{
				writer.writeInsn(i);
			}
		}
	}
	
	private void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		for (int i : this.intrinsicOpcodes)
		{
			if (i == INSTANCE)
			{
				instance.writeExpression(writer);
			}
			else if (i == ARGUMENTS)
			{
				this.writeArguments(writer, instance, arguments);
			}
			else if (Opcodes.isJumpOpcode(i))
			{
				writer.writeJumpInsn(i, dest);
			}
			else
			{
				writer.writeInsn(i);
			}
		}
	}
	
	private void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
		for (int i : this.intrinsicOpcodes)
		{
			if (i == INSTANCE)
			{
				instance.writeExpression(writer);
			}
			else if (i == ARGUMENTS)
			{
				this.writeArguments(writer, instance, arguments);
			}
			else if (Opcodes.isJumpOpcode(i))
			{
				writer.writeJumpInsn(Opcodes.getInverseOpcode(i), dest);
			}
			else
			{
				writer.writeInsn(i);
			}
		}
	}
	
	private void writeArgumentsAndInvoke(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		this.writeArguments(writer, instance, arguments);
		this.writeInvoke(writer, instance, arguments);
	}
	
	@Override
	public void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		int opcode;
		int modifiers = this.modifiers;
		if ((modifiers & Modifiers.STATIC) != 0)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS) && this.value == null)
		{
			opcode = Opcodes.INVOKEINTERFACE;
		}
		else if ((modifiers & Modifiers.PRIVATE) == Modifiers.PRIVATE)
		{
			opcode = Opcodes.INVOKESPECIAL;
		}
		else if (instance != null && instance.valueTag() == IValue.SUPER)
		{
			opcode = Opcodes.INVOKESPECIAL;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.getDescriptor();
		writer.writeInvokeInsn(opcode, owner, name, desc, this.theClass.hasModifier(Modifiers.INTERFACE_CLASS));
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(ModifierTypes.METHOD.toString(this.modifiers));
		if (this.type != null)
		{
			this.type.toString("", buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
		if (this.genericCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, this.genericCount, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		
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
			buffer.append(Formatting.Method.signatureBodySeperator);
			this.value.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
