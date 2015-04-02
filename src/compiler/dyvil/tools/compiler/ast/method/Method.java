package dyvil.tools.compiler.ast.method;

import static dyvil.reflect.Opcodes.ARGUMENTS;
import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;
import static dyvil.reflect.Opcodes.INSTANCE;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IntValue;
import dyvil.tools.compiler.ast.field.FieldMatch;
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
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.ArrayValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	private static final int INLINABLE = (Modifiers.STATIC | Modifiers.PRIVATE | Modifiers.INLINE);
	
	protected IClass			theClass;
	
	protected ITypeVariable[]	generics;
	protected int				genericCount;
	
	protected IParameter[]		parameters	= new MethodParameter[3];
	protected int				parameterCount;
	protected IType[]			exceptions;
	protected int				exceptionCount;
	
	public IValue				value;
	
	protected IMethod			overrideMethod;
	protected int[]				intrinsicOpcodes;
	
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
		case "dyvil.lang.annotation.inline":
			this.modifiers |= Modifiers.INLINE;
			return false;
		case "dyvil.lang.annotation.infix":
			this.modifiers |= Modifiers.INFIX;
			return false;
		case "dyvil.lang.annotation.prefix":
			this.modifiers |= Modifiers.PREFIX;
			return false;
		case "dyvil.lang.annotation.sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "java.lang.Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		case "java.lang.Override":
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
	public IType resolveType(Name name, IValue instance, IArguments arguments, ITypeList generics)
	{
		if (this.genericCount > 0 && generics != null)
		{
			int len = Math.min(this.genericCount, generics.typeCount());
			for (int i = 0; i < len; i++)
			{
				ITypeVariable var = this.generics[i];
				if (var.getName() == name)
				{
					return generics.getType(i);
				}
			}
		}
		
		IType type;
		int len = arguments.size();
		IParameter param;
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			type = this.parameters[0].getType().resolveType(name, instance.getType());
			if (type != null)
			{
				return type;
			}
			
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i + 1];
				type = param.getType().resolveType(name, arguments.getType(i, param));
				if (type != null)
				{
					return type;
				}
			}
			
			return null;
		}
		else if (instance == null && (this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			type = this.theClass.getThisType().resolveType(name, arguments.getFirstValue().getType());
			if (type != null)
			{
				return type;
			}
			return null;
		}
		
		if (instance != null)
		{
			type = this.theClass.getThisType().resolveType(name, instance.getType());
			if (type != null)
			{
				return type;
			}
		}
		len = Math.min(this.parameterCount, len);
		for (int i = 0; i < len; i++)
		{
			param = this.parameters[i];
			type = param.getType().resolveType(name, arguments.getType(i, param));
			if (type != null)
			{
				return type;
			}
		}
		return null;
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
			if (annotation.type.getTheClass() != Types.AIntrinsic.theClass)
			{
				continue;
			}
			
			try
			{
				ArrayValue array = (ArrayValue) annotation.arguments.getValue(0, Annotation.VALUE);
				
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
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, this);
		}
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, this);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			IType t = this.theClass.getSuperType();
			if (t != null)
			{
				IClass iclass = t.getTheClass();
				if (iclass != null)
				{
					this.overrideMethod = iclass.getBody().getMethod(this.name, this.parameters, this.parameterCount);
				}
			}
		}
		
		super.resolve(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
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
			IValue value1 = this.value.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.position, "method.type", this.name);
				marker.addInfo("Return Type: " + this.type);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
			
			this.value.checkTypes(markers, this);
		}
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0)
		{
			this.modifiers |= Modifiers.ABSTRACT;
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			if (this.overrideMethod == null)
			{
				if ((this.modifiers & Modifiers.OVERRIDE) != 0)
				{
					markers.add(this.position, "method.override", this.name);
				}
			}
			else
			{
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
					if (!type.isSuperTypeOf(this.type))
					{
						Marker marker = markers.create(this.position, "method.override.type", this.name);
						marker.addInfo("Return Type: " + this.type);
						marker.addInfo("Overriden Return Type: " + type);
					}
				}
			}
		}
		
		super.check(markers, context);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
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
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.theClass.resolveTypeVariable(name);
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return new FieldMatch(param, 1);
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
	public byte getAccessibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null)
		{
			return READ_WRITE_ACCESS;
		}
		if ((this.modifiers & Modifiers.STATIC) != 0 && iclass == this.theClass && !member.hasModifier(Modifiers.STATIC) && !(member instanceof IConstructor))
		{
			return STATIC;
		}
		return this.theClass.getAccessibility(member);
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
	public IValue checkArguments(MarkerList markers, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		int len = arguments.size();
		IType parType;
		
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			IParameter par = this.parameters[0];
			parType = par.getType(typeContext);
			IValue instance1 = instance.withType(parType);
			if (instance1 == null)
			{
				Marker marker = markers.create(instance.getPosition(), "access.method.infix_type", par.getName());
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
				Marker marker = markers.create(instance.getPosition(), "access.method.prefix_type", this.name);
				marker.addInfo("Required Type: " + parType);
				marker.addInfo("Value Type: " + instance.getType());
				
			}
			return instance;
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
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.generics != null || this.theClass.isGeneric();
	}
	
	@Override
	public boolean isIntrinsic()
	{
		return this.intrinsicOpcodes != null;
	}
	
	@Override
	public String getDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getType().appendExtendedName(buffer);
		}
		buffer.append(')');
		this.type.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	@Override
	public String getSignature()
	{
		if (this.generics == null && !this.theClass.isGeneric())
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
		this.type.appendExtendedName(buffer);
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
	public void write(ClassWriter writer)
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
			mw.setInstanceMethod();
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.addAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
		{
			mw.addAnnotation("Ldyvil/lang/annotation/sealed;", false);
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
			this.value.writeExpression(mw);
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.writeLocal("this", this.theClass.getType(), start, end, 0);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getName().qualified, param.getType(), start, end, param.getIndex());
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type)
	{
		if (instance != null && (this.modifiers & Modifiers.STATIC) != 0 && instance.getValueType() == IValue.CLASS_ACCESS)
		{
			instance = null;
		}
		
		if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			if (this.type.getTheClass() == Types.BOOLEAN_CLASS)
			{
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
			
			this.writeIntrinsic(writer, instance, arguments);
			return;
		}
		
		if ((this.modifiers & INLINABLE) != 0 && writer.inlineOffset() == 0)
		{
			Label inlineEnd = new Label();
			this.writeInlineArguments(writer, instance, arguments, writer.startInline(inlineEnd));
			this.value.writeExpression(writer);
			writer.endInline(inlineEnd);
			return;
		}
		
		this.writeInvoke(writer, instance, arguments);
		
		if (type == Types.VOID)
		{
			if (this.type != Types.VOID)
			{
				writer.writeInsn(Opcodes.POP);
			}
			return;
		}
		
		if (type != null)
		{
			if (type != this.type && !type.isSuperTypeOf(this.type))
			{
				writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
			}
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		if (instance != null && (this.modifiers & Modifiers.STATIC) != 0 && instance.getValueType() == IValue.CLASS_ACCESS)
		{
			instance = null;
		}
		
		if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			this.writeIntrinsic(writer, dest, instance, arguments);
			return;
		}
		
		if ((this.modifiers & INLINABLE) != 0 && writer.inlineOffset() == 0)
		{
			Label inlineEnd = new Label();
			this.writeInlineArguments(writer, instance, arguments, writer.startInline(inlineEnd));
			this.value.writeJump(writer, dest);
			writer.endInline(inlineEnd);
			return;
		}
		
		this.writeInvoke(writer, instance, arguments);
		writer.writeJumpInsn(IFEQ, dest);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		if (instance != null && (this.modifiers & Modifiers.STATIC) != 0 && instance.getValueType() == IValue.CLASS_ACCESS)
		{
			instance = null;
		}
		
		if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			this.writeInvIntrinsic(writer, dest, instance, arguments);
			return;
		}
		
		if ((this.modifiers & INLINABLE) != 0 && writer.inlineOffset() == 0)
		{
			Label inlineEnd = new Label();
			this.writeInlineArguments(writer, instance, arguments, writer.startInline(inlineEnd));
			this.value.writeInvJump(writer, dest);
			writer.endInline(inlineEnd);
			return;
		}
		
		this.writeInvoke(writer, instance, arguments);
		writer.writeJumpInsn(IFNE, dest);
	}
	
	private void writeArguments(MethodWriter writer, IArguments arguments)
	{
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
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
	
	private void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments)
	{
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
				this.writeArguments(writer, arguments);
			}
			else
			{
				writer.writeInsn(i);
			}
		}
	}
	
	private void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		for (int i : this.intrinsicOpcodes)
		{
			if (i == INSTANCE)
			{
				instance.writeExpression(writer);
			}
			else if (i == ARGUMENTS)
			{
				this.writeArguments(writer, arguments);
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
	
	private void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		for (int i : this.intrinsicOpcodes)
		{
			if (i == INSTANCE)
			{
				instance.writeExpression(writer);
			}
			else if (i == ARGUMENTS)
			{
				this.writeArguments(writer, arguments);
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
	
	private void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments)
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		
		this.writeArguments(writer, arguments);
		
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
		else if (instance != null && instance.getValueType() == IValue.SUPER)
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
	
	private void writeInlineArguments(MethodWriter writer, IValue instance, IArguments arguments, int localCount)
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
			writer.writeVarInsn(instance.getType().getStoreOpcode(), localCount);
		}
		
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
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
					
					param.setIndex(writer.registerLocal());
					param.writeSet(writer, null, null);
				}
				param = this.parameters[len];
				arguments.writeVarargsValue(len - 1, param.getName(), param.getType(), writer);
				
				param.setIndex(writer.registerLocal());
				param.writeSet(writer, null, null);
				return;
			}
			
			for (int i = 1, j = 0; i < this.parameterCount; i++, j++)
			{
				IParameter param = this.parameters[i];
				arguments.writeValue(j, param.getName(), param.getValue(), writer);
				
				param.setIndex(writer.registerLocal());
				param.writeSet(writer, null, null);
			}
			return;
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			arguments.writeValue(0, Name._this, null, writer);
			writer.writeVarInsn(Opcodes.ASTORE, localCount);
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
				param.setIndex(writer.registerLocal());
				param.writeSet(writer, null, null);
			}
			param = this.parameters[len];
			arguments.writeVarargsValue(len, param.getName(), param.getType(), writer);
			param.setIndex(writer.registerLocal());
			param.writeSet(writer, null, null);
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			arguments.writeValue(i, param.getName(), param.getValue(), writer);
			param.setIndex(writer.registerLocal());
			param.writeSet(writer, null, null);
		}
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
