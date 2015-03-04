package dyvil.tools.compiler.ast.method;

import static dyvil.reflect.Opcodes.ARGUMENTS;
import static dyvil.reflect.Opcodes.IFEQ;
import static dyvil.reflect.Opcodes.IFNE;
import static dyvil.reflect.Opcodes.INSTANCE;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
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
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	protected ITypeVariable[]	generics;
	protected int				genericCount;
	
	protected Parameter[]		parameters	= new Parameter[3];
	protected int				parameterCount;
	public List<IType>			throwsDeclarations;
	
	public IValue				value;
	
	protected boolean			isConstructor;
	
	protected IMethod			overrideMethod;
	protected int[]				intrinsicOpcodes;
	
	public Method(IClass iclass)
	{
		super(iclass);
	}
	
	public Method(IClass iclass, String name)
	{
		super(iclass, name);
		if (name.equals("new"))
		{
			this.isConstructor = true;
		}
	}
	
	public Method(IClass iclass, String name, IType type)
	{
		super(iclass, name, type);
		if (name.equals("new"))
		{
			this.isConstructor = true;
		}
	}
	
	@Override
	public void setName(String name)
	{
		if (name.equals("new"))
		{
			this.qualifiedName = "<init>";
			this.name = "new";
			this.isConstructor = true;
		}
		else
		{
			this.qualifiedName = Symbols.qualify(name);
			this.name = name;
		}
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		if (name.equals("<init>"))
		{
			this.qualifiedName = "<init>";
			this.name = "new";
			this.isConstructor = true;
		}
		else
		{
			this.qualifiedName = name;
			this.name = Symbols.unqualify(name);
		}
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
	
	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}
	
	@Override
	public void setParameter(int index, Parameter param)
	{
		this.parameters[index] = param;
		param.method = this;
	}
	
	@Override
	public void addParameter(Parameter param)
	{
		int index = this.parameterCount++;
		if (this.parameterCount > this.parameters.length)
		{
			Parameter[] temp = new Parameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = param;
		param.method = this;
	}
	
	@Override
	public Parameter getParameter(int index)
	{
		return this.parameters[index];
	}
	
	@Override
	public boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.fullName;
		switch (name)
		{
		case "dyvil.lang.annotation.inline":
			this.modifiers |= Modifiers.INLINE;
			return true;
		case "dyvil.lang.annotation.infix":
			this.modifiers |= Modifiers.INFIX;
			return true;
		case "dyvil.lang.annotation.prefix":
			this.modifiers |= Modifiers.PREFIX;
			return true;
		case "dyvil.lang.annotation.sealed":
			this.modifiers |= Modifiers.SEALED;
			return true;
		case "dyvil.lang.annotation.Intrinsic":
			ValueList array = (ValueList) annotation.arguments.getValue("value");
			if (array != null)
			{
				int len = array.valueCount();
				int[] opcodes = new int[len];
				for (int i = 0; i < len; i++)
				{
					IntValue v = (IntValue) array.getValue(i).foldConstants();
					opcodes[i] = v.value;
				}
				this.intrinsicOpcodes = opcodes;
			}
			return false;
		case "java.lang.Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return true;
		case "java.lang.Override":
			this.modifiers |= Modifiers.OVERRIDE;
			return true;
		}
		return false;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.METHOD;
	}
	
	@Override
	public void setThrows(List<IType> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public List<IType> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public void addThrows(IType throwsDecl)
	{
		if (this.throwsDeclarations == null)
		{
			this.throwsDeclarations = new ArrayList(2);
		}
		this.throwsDeclarations.add(throwsDecl);
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
	public int getSignatureMatch(String name, IValue instance, IArguments arguments)
	{
		if (name == null)
		{
			return 1;
		}
		
		if (!name.equals(this.qualifiedName))
		{
			return 0;
		}
		
		// Only matching the name
		if (arguments == null)
		{
			return 1;
		}
		
		int pOff = 0;
		int match = 1;
		int len = arguments.size();
		
		// infix modifier implementation
		int mods = this.modifiers & Modifiers.INFIX;
		if (instance != null && mods == Modifiers.INFIX)
		{
			if (len != this.parameterCount - 1)
			{
				return 0;
			}
			
			IType t2 = this.parameters[0].type;
			int m = instance.getTypeMatch(t2);
			if (m == 0)
			{
				return 0;
			}
			match += m;
			
			pOff = 1;
		}
		else if (mods == Modifiers.STATIC && instance != null && instance.getValueType() != IValue.CLASS_ACCESS)
		{
			return 0;
		}
		else if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			int parCount = this.parameterCount - 1;
			if (len <= parCount)
			{
				return 0;
			}
			
			int m;
			Parameter varParam = this.parameters[parCount];
			varParam.index = parCount;
			for (int i = 0; i < parCount; i++)
			{
				Parameter par = this.parameters[i + pOff];
				par.index = i + pOff;
				m = arguments.getTypeMatch(par);
				if (m == 0)
				{
					return 0;
				}
				match += m;
			}
			for (int i = parCount; i < len; i++)
			{
				m = arguments.getVarargsTypeMatch(varParam);
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
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			Parameter par = this.parameters[i + pOff];
			par.index = i;
			int m = arguments.getTypeMatch(par);
			if (m == 0)
			{
				return 0;
			}
			match += m;
		}
		
		return match;
	}
	
	@Override
	public void checkArguments(List<Marker> markers, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		int pOff = 0;
		int len = arguments.size();
		Parameter par;
		IType parType;
		
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			par = this.parameters[0];
			parType = par.getType(typeContext);
			IValue instance1 = instance.withType(parType);
			if (instance1 == null)
			{
				Marker marker = Markers.create(instance.getPosition(), "access.method.infix_type", par.name);
				marker.addInfo("Required Type: " + parType);
				marker.addInfo("Value Type: " + instance.getType());
				markers.add(marker);
			}
			pOff = 1;
		}
		else if (instance == null && (this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			parType = this.theClass.getType();
			instance = arguments.getFirstValue();
			IValue instance1 = instance.withType(parType);
			if (instance1 == null)
			{
				Marker marker = Markers.create(instance.getPosition(), "access.method.prefix_type", this.name);
				marker.addInfo("Required Type: " + parType);
				marker.addInfo("Value Type: " + instance.getType());
				markers.add(marker);
			}
			return;
		}
		
		if ((this.modifiers & Modifiers.VARARGS) != 0)
		{
			len = this.parameterCount - 1;
			par = this.parameters[len];
			par.index = len;
			arguments.checkVarargsValue(markers, par, typeContext);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			par = this.parameters[i + pOff];
			par.index = i;
			arguments.checkValue(markers, par, typeContext);
		}
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.generics != null || this.theClass.isGeneric();
	}
	
	@Override
	public IType resolveType(String name, IValue instance, IArguments arguments, ITypeList generics)
	{
		if (this.genericCount > 0)
		{
			int len = Math.min(this.genericCount, generics.typeCount());
			for (int i = 0; i < len; i++)
			{
				ITypeVariable var = this.generics[i];
				if (var.isName(name))
				{
					return generics.getType(i);
				}
			}
		}
		
		IType type;
		int len = arguments.size();
		Parameter param;
		if (instance != null && (this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			type = this.parameters[0].type.resolveType(name, instance.getType());
			if (type != null)
			{
				return type;
			}
			
			for (int i = 0; i < len; i++)
			{
				param = this.parameters[i + 1];
				type = param.type.resolveType(name, arguments.getType(param));
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
			type = param.type.resolveType(name, arguments.getType(param));
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(markers, context);
		}
		
		super.resolveTypes(markers, this);
		
		if (this.throwsDeclarations != null)
		{
			int len = this.throwsDeclarations.size();
			for (int i = 0; i < len; i++)
			{
				IType t1 = this.throwsDeclarations.get(i);
				IType t2 = t1.resolve(markers, context);
				if (t1 != t2)
				{
					this.throwsDeclarations.set(i, t2);
				}
			}
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
	public void resolve(List<Marker> markers, IContext context)
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
		
		int index = this.hasModifier(Modifiers.STATIC) ? 0 : 1;
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			Parameter param = this.parameters[i];
			param.index = index++;
			param.resolve(markers, context);
			
			if (param.defaultValue != null)
			{
				this.theClass.getBody().addCompilable(param);
			}
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			if (this.overrideMethod == null)
			{
				if ((this.modifiers & Modifiers.OVERRIDE) != 0)
				{
					markers.add(Markers.create(this.position, "method.override", this.name));
				}
			}
			else if (!this.isConstructor)
			{
				if ((this.modifiers & Modifiers.OVERRIDE) == 0)
				{
					markers.add(Markers.create(this.position, "method.overrides", this.name));
				}
				else if (this.overrideMethod.hasModifier(Modifiers.FINAL))
				{
					markers.add(Markers.create(this.position, "method.override.final", this.name));
				}
				else
				{
					IType type = this.overrideMethod.getType();
					if (!Type.isSuperType(type, this.type))
					{
						Marker marker = Markers.create(this.position, "method.override.type", this.name);
						marker.addInfo("Return Type: " + this.type);
						marker.addInfo("Overriden Return Type: " + type);
						markers.add(marker);
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
			if (this.isConstructor)
			{
				if (!this.value.isType(Type.VOID))
				{
					Marker error = Markers.create(this.position, "constructor.return");
					error.addInfo("Expression Type: " + this.value.getType());
					markers.add(error);
				}
			}
			else
			{
				IValue value1 = this.value.withType(this.type);
				if (value1 == null)
				{
					Marker marker = Markers.create(this.position, "method.type", this.name);
					marker.addInfo("Return Type: " + this.type);
					marker.addInfo("Value Type: " + this.value.getType());
					markers.add(marker);
				}
				else
				{
					this.value = value1;
				}
				this.value.check(markers, context);
			}
		}
		// If the method does not have an implementation and is static
		else if (this.isStatic())
		{
			markers.add(Markers.create(this.position, "method.static", this.name));
		}
		// Or not declared abstract and a member of a non-abstract class
		else if ((this.modifiers & Modifiers.ABSTRACT) == 0)
		{
			if (this.theClass.isAbstract())
			{
				this.modifiers |= Modifiers.ABSTRACT;
			}
			else
			{
				markers.add(Markers.create(this.position, "method.unimplemented", this.name));
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
	public IType getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.isName(name))
			{
				return var.getCaptureClass();
			}
		}
		
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			Parameter param = this.parameters[i];
			if (param.isName(name))
			{
				return new FieldMatch(param, 1);
			}
		}
		
		return this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return this.theClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
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
		if ((this.modifiers & Modifiers.STATIC) != 0 && iclass == this.theClass && !member.hasModifier(Modifiers.STATIC) && !member.isName("<init>"))
		{
			return STATIC;
		}
		return this.theClass.getAccessibility(member);
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
			this.parameters[i].type.appendExtendedName(buffer);
		}
		buffer.append(')');
		if (this.isConstructor)
		{
			buffer.append('V');
		}
		else
		{
			this.type.appendExtendedName(buffer);
		}
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
			this.parameters[i].type.appendSignature(buffer);
		}
		buffer.append(')');
		if (this.isConstructor)
		{
			buffer.append('V');
		}
		else
		{
			this.type.appendExtendedName(buffer);
		}
		return buffer.toString();
	}
	
	@Override
	public String[] getExceptions()
	{
		if (this.throwsDeclarations == null)
		{
			return null;
		}
		
		int len = this.throwsDeclarations.size();
		if (len == 0)
		{
			return null;
		}
		
		String[] array = new String[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = this.throwsDeclarations.get(i).getInternalName();
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
		MethodWriter mw = new MethodWriter(writer, writer.visitMethod(modifiers, this.qualifiedName, this.getDescriptor(), this.getSignature(),
				this.getExceptions()));
		
		if (this.isConstructor)
		{
			mw.setConstructor(this.type);
		}
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.addLocal(this.theClass.getType());
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.visitAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/sealed;", false);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(mw);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.visitCode();
			mw.visitLabel(start, false);
			this.value.writeExpression(mw);
			mw.visitLabel(end, false);
			mw.visitEnd(this.isConstructor ? Type.VOID : this.type);
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.visitLocalVariable("this", this.theClass.getType(), start, end, 0);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			Parameter param = this.parameters[i];
			mw.visitLocalVariable(param.qualifiedName, param.type, start, end, param.index);
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments)
	{
		if (instance != null && (this.modifiers & Modifiers.STATIC) != 0 && instance.getValueType() == IValue.CLASS_ACCESS)
		{
			instance = null;
		}
		
		if (this.intrinsicOpcodes != null && (instance == null || instance.isPrimitive()))
		{
			if (this.type == Type.BOOLEAN)
			{
				Label ifEnd = new Label();
				Label elseEnd = new Label();
				this.writeIntrinsic(writer, ifEnd, instance, arguments);
				
				// If Block
				writer.visitLdcInsn(1);
				writer.pop();
				writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
				writer.visitLabel(ifEnd);
				// Else Block
				writer.visitLdcInsn(0);
				writer.visitLabel(elseEnd);
				return;
			}
			this.writeIntrinsic(writer, instance, arguments);
			return;
		}
		
		this.writeInvoke(writer, instance, arguments);
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
		
		this.writeInvoke(writer, instance, arguments);
		writer.visitJumpInsn(IFNE, dest);
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
		
		this.writeInvoke(writer, instance, arguments);
		writer.visitJumpInsn(IFEQ, dest);
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
				for (int j = 0; j < this.parameterCount; j++)
				{
					arguments.writeValue(this.parameters[j], writer);
				}
			}
			else
			{
				writer.visitInsn(i);
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
				for (int j = 0; j < this.parameterCount; j++)
				{
					arguments.writeValue(this.parameters[j], writer);
				}
			}
			else if (Opcodes.isJumpOpcode(i))
			{
				writer.visitJumpInsn(i, dest);
			}
			else
			{
				writer.visitInsn(i);
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
				for (int j = 0; j < this.parameterCount; j++)
				{
					arguments.writeValue(this.parameters[j], writer);
				}
			}
			else if (Opcodes.isJumpOpcode(i))
			{
				writer.visitJumpInsn(Opcodes.getInverseOpcode(i), dest);
			}
			else
			{
				writer.visitInsn(i);
			}
		}
	}
	
	private void writeInvoke(MethodWriter writer, IValue instance, IArguments arguments)
	{
		int args = 0;
		if (instance != null)
		{
			instance.writeExpression(writer);
			args = 1;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			arguments.writeValue(this.parameters[i], writer);
		}
		args += this.parameterCount;
		
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
		String name = this.qualifiedName;
		String desc = this.getDescriptor();
		IType type = this.type;
		writer.visitMethodInsn(opcode, owner, name, desc, this.theClass.hasModifier(Modifiers.INTERFACE_CLASS), args, type);
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
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.genericCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, this.genericCount, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		
		buffer.append(Formatting.Method.parametersStart);
		Util.astToString(prefix, this.parameters, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
		buffer.append(Formatting.Method.parametersEnd);
		
		if (this.throwsDeclarations != null && !this.throwsDeclarations.isEmpty())
		{
			buffer.append(Formatting.Method.signatureThrowsSeperator);
			Util.astToString(prefix, this.throwsDeclarations, Formatting.Method.throwsSeperator, buffer);
		}
		
		IValue value = this.getValue();
		if (value != null)
		{
			buffer.append(Formatting.Method.signatureBodySeperator);
			Formatting.appendValue(value, prefix, buffer);
		}
		buffer.append(';');
	}
}
