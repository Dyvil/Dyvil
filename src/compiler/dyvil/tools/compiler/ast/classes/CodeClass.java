package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.SuperValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ClassParameterSetter;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class CodeClass extends ASTNode implements IClass
{
	protected IDyvilUnit			unit;
	protected IClass				outerClass;
	
	protected Annotation[]			annotations;
	protected int					annotationCount;
	protected int					modifiers;
	
	protected String				name;
	protected String				qualifiedName;
	protected String				fullName;
	protected String				internalName;
	
	protected ITypeVariable[]		generics;
	protected int					genericCount;
	
	protected Parameter[]			parameters;
	protected int					parameterCount;
	
	protected IType					superType	= Type.OBJECT;
	protected IType[]				interfaces;
	protected int					interfaceCount;
	
	protected IType					type;
	
	protected IClassBody			body;
	protected IField				instanceField;
	protected IClassCompilable[]	compilables;
	protected int					compilableCount;
	protected IMethod				constructor;
	protected IMethod				superConstructor;
	
	public CodeClass()
	{
		this.type = new Type(this);
	}
	
	public CodeClass(ICodePosition position, IDyvilUnit unit)
	{
		this.position = position;
		this.unit = unit;
		this.type = new Type(this);
		this.interfaces = new IType[1];
	}
	
	public CodeClass(ICodePosition position, IDyvilUnit unit, int modifiers)
	{
		this.position = position;
		this.unit = unit;
		this.modifiers = modifiers;
		this.type = new Type(this);
		this.interfaces = new IType[1];
	}
	
	@Override
	public IDyvilUnit getUnit()
	{
		return this.unit;
	}
	
	@Override
	public void setOuterClass(IClass iclass)
	{
		this.outerClass = iclass;
	}
	
	@Override
	public IClass getOuterClass()
	{
		return this.outerClass;
	}
	
	@Override
	public void setType(IType type)
	{
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this;
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
		this.annotations = annotations;
		this.annotationCount = count;
	}
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
		this.annotations[index] = annotation;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		annotation.target = ElementType.TYPE;
		
		if (this.annotations == null)
		{
			this.annotations = new Annotation[3];
			this.annotations[0] = annotation;
			this.annotationCount = 1;
			return;
		}
		
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			Annotation[] temp = new Annotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
	
	@Override
	public final void removeAnnotation(int index)
	{
		int numMoved = this.annotationCount - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.annotations, index + 1, this.annotations, index, numMoved);
		}
		this.annotations[--this.annotationCount] = null;
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		if ("dyvil.lang.annotation.sealed".equals(type))
		{
			this.modifiers |= Modifiers.SEALED;
			return false;
		}
		if ("java.lang.Deprecated".equals(type))
		{
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		}
		if ("java.lang.FunctionalInterface".equals(type))
		{
			this.modifiers |= Modifiers.FUNCTIONAL;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.TYPE;
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return this.annotations[index];
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (a.type.classEquals(type))
			{
				return a;
			}
		}
		return null;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		boolean flag = (this.modifiers & mod) == mod;
		this.modifiers |= mod;
		return flag;
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.modifiers &= ~mod;
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) != 0;
	}
	
	@Override
	public boolean isAbstract()
	{
		return (this.modifiers & Modifiers.INTERFACE_CLASS) != 0 || (this.modifiers & Modifiers.ABSTRACT) != 0;
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers & Modifiers.ACCESS_MODIFIERS;
	}
	
	@Override
	public byte getAccessibility()
	{
		return IContext.READ_ACCESS;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
		this.internalName = this.unit.getInternalName(this.qualifiedName);
		this.fullName = this.unit.getFullName(this.qualifiedName);
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.name = name;
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.name.equals(name);
	}
	
	@Override
	public void setFullName(String name)
	{
		this.fullName = name;
	}
	
	@Override
	public String getFullName()
	{
		return this.fullName;
	}
	
	// Generics
	
	@Override
	public void setGeneric()
	{
		this.generics = new ITypeVariable[2];
	}
	
	@Override
	public boolean isGeneric()
	{
		return this.generics != null;
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
	
	// Class Parameters
	
	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}
	
	@Override
	public void setParameter(int index, Parameter param)
	{
		this.parameters[index] = param;
	}
	
	@Override
	public void addParameter(Parameter param)
	{
		param.parameterized = this;
		IMethod constructor = this.getConstructor();
		
		if (this.parameters == null)
		{
			this.parameters = new Parameter[2];
			this.parameters[0] = param;
			this.parameterCount = 1;
			constructor.setParameters(this.parameters, 1);
			return;
		}
		
		int index = this.parameterCount++;
		if (this.parameterCount > this.parameters.length)
		{
			Parameter[] temp = new Parameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = param;
		constructor.setParameters(this.parameters, this.parameterCount);
	}
	
	@Override
	public Parameter getParameter(int index)
	{
		return this.parameters[index];
	}
	
	// Super Types
	
	@Override
	public void setSuperType(IType type)
	{
		this.superType = type;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.superType;
	}
	
	@Override
	public boolean isSubTypeOf(IType type)
	{
		if (this.superType != null && type.isSuperTypeOf2(this.superType))
		{
			return true;
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			if (type.isSuperTypeOf2(this.interfaces[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int interfaceCount()
	{
		return this.interfaceCount;
	}
	
	@Override
	public void setInterface(int index, IType type)
	{
		this.interfaces[index] = type;
	}
	
	@Override
	public void addInterface(IType type)
	{
		int index = this.interfaceCount++;
		if (index > this.interfaces.length)
		{
			IType[] temp = new IType[this.interfaces.length + 1];
			System.arraycopy(this.interfaces, 0, temp, 0, this.interfaces.length);
			this.interfaces = temp;
		}
		this.interfaces[index] = type;
	}
	
	@Override
	public IType getInterface(int index)
	{
		return this.interfaces[index];
	}
	
	// Body
	
	@Override
	public void setBody(IClassBody body)
	{
		this.body = body;
	}
	
	@Override
	public IClassBody getBody()
	{
		return this.body;
	}
	
	@Override
	public IField getInstanceField()
	{
		return this.instanceField;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		if (this.body == null)
		{
			return null;
		}
		return this.body.getFunctionalMethod();
	}
	
	@Override
	public String getInternalName()
	{
		return this.internalName;
	}
	
	@Override
	public String getSignature()
	{
		return null;
	}
	
	@Override
	public String[] getInterfaceArray()
	{
		String[] interfaces = new String[this.interfaceCount];
		for (int i = 0; i < this.interfaceCount; i++)
		{
			interfaces[i] = this.interfaces[i].getInternalName();
		}
		return interfaces;
	}
	
	private IMethod getConstructor()
	{
		if (this.constructor != null)
		{
			return this.constructor;
		}
		
		Method constructor = new Method(this, "new", Type.VOID);
		constructor.modifiers = Modifiers.PUBLIC | Modifiers.SYNTHETIC;
		return this.constructor = constructor;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.genericCount > 0)
		{
			GenericType type = new GenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(markers, context);
				type.addType(new WildcardType(null, 0, var.getCaptureClass()));
			}
			
			this.type = type;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, this);
		}
		
		if (this.superType != null)
		{
			if (this.superType.isName("void"))
			{
				this.superType = null;
			}
			else
			{
				this.superType = this.superType.resolve(markers, context);
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolve(markers, context);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
		
		if (this.body != null)
		{
			this.body.resolveTypes(markers, this);
			
			if (this.body.getMethod("<init>") != null)
			{
				return;
			}
		}
		
		if (this.superType != null)
		{
			MethodMatch match = this.superType.resolveConstructor(EmptyArguments.INSTANCE);
			if (match != null)
			{
				this.superConstructor = match.theMethod;
			}
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (this.addRawAnnotation(a.type.fullName))
			{
				this.removeAnnotation(i--);
				continue;
			}
			
			a.resolve(markers, context);
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			Field f = new Field(this, "$instance", this.getType());
			f.modifiers = Modifiers.PUBLIC | Modifiers.CONST | Modifiers.SYNTHETIC;
			this.instanceField = f;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, this);
		}
		
		if (this.body != null)
		{
			this.body.resolve(markers, this);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.superType != null)
		{
			IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				int modifiers = superClass.getModifiers();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != 0)
				{
					markers.add(this.superType.getPosition(), "class.extend.type", ModifierTypes.CLASS_TYPE.toString(modifiers), superClass.getName());
				}
				else if ((modifiers & Modifiers.FINAL) != 0)
				{
					markers.add(this.superType.getPosition(), "class.extend.final", superClass.getName());
				}
				else if ((modifiers & Modifiers.DEPRECATED) != 0)
				{
					markers.add(this.superType.getPosition(), "class.extend.deprecated", superClass.getName());
				}
			}
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			IMethod m = this.body.getMethod("<init>");
			if (m != null)
			{
				markers.add(m.getPosition(), "class.object.constructor", this.name);
			}
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				int modifiers = iclass.getModifiers();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != Modifiers.INTERFACE_CLASS)
				{
					markers.add(type.getPosition(), "class.implement.type", ModifierTypes.CLASS_TYPE.toString(modifiers), iclass.getName());
				}
				else if ((modifiers & Modifiers.DEPRECATED) != 0)
				{
					markers.add(type.getPosition(), "class.implement.deprecated", iclass.getName());
				}
			}
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].check(markers, context);
		}
		
		if (this.body != null)
		{
			this.body.check(markers, this);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].foldConstants();
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
		
		if (this.body != null)
		{
			this.body.foldConstants();
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return this.type;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
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
		
		IClass clazz = this.body.getClass(name);
		if (clazz != null)
		{
			return clazz;
		}
		
		return this.unit.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			Parameter param = this.parameters[i];
			if (name.equals(param.qualifiedName))
			{
				return new FieldMatch(param, 1);
			}
		}
		
		if (this.body != null)
		{
			// Own properties
			IField field = this.body.getProperty(name);
			if (field != null)
			{
				return new FieldMatch(field, 1);
			}
			
			// Own fields
			field = this.body.getField(name);
			if (field != null)
			{
				return new FieldMatch(field, 1);
			}
		}
		
		if (this.instanceField != null && "instance".equals(name))
		{
			return new FieldMatch(this.instanceField, 1);
		}
		
		FieldMatch match;
		
		// Inherited Fields
		if (this.superType != null)
		{
			match = this.superType.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		
		if (this.unit != null && this.unit.hasStaticImports())
		{
			// Static Imports
			match = this.unit.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		List<MethodMatch> list = new ArrayList();
		this.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			Collections.sort(list);
			return list.get(0);
		}
		
		return null;
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		List<MethodMatch> list = new ArrayList();
		this.getConstructorMatches(list, arguments);
		
		if (!list.isEmpty())
		{
			Collections.sort(list);
			return list.get(0);
		}
		
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		if (this.body != null)
		{
			this.body.getMethodMatches(list, instance, name, arguments);
		}
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, instance, name, arguments);
		}
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (this.unit != null && this.unit.hasStaticImports())
		{
			this.unit.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		if (this.constructor != null)
		{
			int match = this.constructor.getSignatureMatch("<init>", null, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(this.constructor, match));
			}
		}
		
		if (this.body != null)
		{
			this.body.getMethodMatches(list, null, "<init>", arguments);
		}
	}
	
	@Override
	public boolean isMember(IMember member)
	{
		return this == member.getTheClass();
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == this || iclass == null)
		{
			return member.getAccessibility();
		}
		
		int level = member.getAccessLevel();
		if ((level & Modifiers.SEALED) != 0)
		{
			if (iclass instanceof BytecodeClass)
			{
				return SEALED;
			}
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return member.getAccessibility();
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.DERIVED)
		{
			if (this.superType != null && this.superType.getTheClass() == iclass)
			{
				return member.getAccessibility();
			}
			
			for (int i = 0; i < this.interfaceCount; i++)
			{
				if (this.interfaces[i].getTheClass() == iclass)
				{
					return member.getAccessibility();
				}
			}
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.PACKAGE)
		{
			if (this.unit.getPackage() == iclass.getUnit().getPackage())
			{
				return member.getAccessibility();
			}
		}
		
		return INVISIBLE;
	}
	
	@Override
	public int compilableCount()
	{
		return this.compilableCount;
	}
	
	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		if (this.compilables == null)
		{
			this.compilables = new IClassCompilable[2];
			this.compilables[0] = compilable;
			this.compilableCount = 1;
			return;
		}
		
		int index = this.compilableCount++;
		if (this.compilableCount > this.compilables.length)
		{
			IClassCompilable[] temp = new IClassCompilable[this.compilableCount];
			System.arraycopy(this.compilables, 0, temp, 0, index);
			this.compilables = temp;
		}
		this.compilables[index] = compilable;
	}
	
	@Override
	public IClassCompilable getCompilable(int index)
	{
		return this.compilables[index];
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		// Header
		
		String internalName = this.getInternalName();
		String signature = this.getSignature();
		String superClass = null;
		String[] interfaces = this.getInterfaceArray();
		
		if (this.superType != null)
		{
			superClass = this.superType.getInternalName();
		}
		
		writer.visit(MethodWriter.V1_8, this.modifiers & 0xFFFF, internalName, signature, superClass, interfaces);
		
		// Outer Class
		
		if (this.outerClass != null)
		{
			writer.visitOuterClass(this.outerClass.getInternalName(), null, null);
		}
		
		// Annotations
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/object;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/sealed;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			writer.visitAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.FUNCTIONAL) != 0)
		{
			writer.visitAnnotation("Ljava/lang/FunctionalInterface;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(writer);
		}
		
		// Inner Class Info
		
		if (this.outerClass != null)
		{
			this.writeInnerClassInfo(writer);
		}
		
		if (this.superType != null)
		{
			IClass iclass = this.superType.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}
		
		// Fields, Methods and Properties
		
		int fields = 0;
		int methods = 0;
		int properties = 0;
		if (this.body != null)
		{
			fields = this.body.fieldCount();
			methods = this.body.methodCount();
			properties = this.body.propertyCount();
			
			int classes = this.body.classCount();
			for (int i = 0; i < classes; i++)
			{
				this.body.getClass(i).writeInnerClassInfo(writer);
			}
		}
		
		ThisValue thisValue = new ThisValue(null, this.type);
		IField instanceField = this.instanceField;
		StatementList instanceFields = new StatementList(null);
		StatementList staticFields = new StatementList(null);
		
		for (int i = 0; i < fields; i++)
		{
			IField f = this.body.getField(i);
			f.write(writer);
			
			if (f.hasModifier(Modifiers.LAZY))
			{
				continue;
			}
			
			if (f.hasModifier(Modifiers.STATIC))
			{
				FieldAssign assign = new FieldAssign(null);
				assign.qualifiedName = f.getQualifiedName();
				assign.value = f.getValue();
				assign.field = f;
				staticFields.addValue(assign);
			}
			else
			{
				FieldAssign assign = new FieldAssign(null);
				assign.qualifiedName = f.getQualifiedName();
				assign.instance = thisValue;
				assign.value = f.getValue();
				assign.field = f;
				instanceFields.addValue(assign);
			}
		}
		
		if (this.superConstructor != null)
		{
			MethodCall call = new MethodCall(null);
			call.instance = new SuperValue(null, this.superType);
			call.method = this.superConstructor;
			call.name = "new";
			call.qualifiedName = "<init>";
			call.arguments = EmptyArguments.INSTANCE;
			instanceFields.addValue(0, call);
		}
		
		if (this.constructor != null)
		{
			for (int i = 0; i < this.parameterCount; i++)
			{
				Parameter param = this.parameters[i];
				String desc = param.getDescription();
				writer.visitField(param.modifiers & 0xFFFF, param.qualifiedName, desc, param.getSignature(), null);
				instanceFields.addValue(new ClassParameterSetter(param, this.internalName, desc));
			}
			
			this.constructor.setValue(instanceFields);
			this.constructor.write(writer);
		}
		
		for (int i = 0; i < properties; i++)
		{
			this.body.getProperty(i).write(writer);
		}
		
		for (int i = 0; i < methods; i++)
		{
			this.body.getMethod(i).write(writer);
		}
		
		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].write(writer);
		}
		
		if ((this.modifiers & Modifiers.CASE_CLASS) != 0)
		{
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals", "(Ljava/lang/Object;)Z", null,
					null));
			mw.registerLocal(this.type);
			mw.registerParameter("obj", "Ljava/lang/Object;");
			mw.begin();
			CaseClasses.writeEquals(mw, this, this.body);
			mw.end();
			
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "hashCode", "()I", null, null));
			mw.registerLocal(this.type);
			mw.begin();
			CaseClasses.writeHashCode(mw, this, this.body);
			mw.end();
			
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "toString", "()Ljava/lang/String;", null, null));
			mw.registerLocal(this.type);
			mw.begin();
			CaseClasses.writeToString(mw, this, this.body);
			mw.end();
		}
		
		if (instanceField != null)
		{
			instanceField.write(writer);
			FieldAssign assign = new FieldAssign(null);
			assign.name = assign.qualifiedName = "instance";
			assign.field = instanceField;
			ConstructorCall call = new ConstructorCall(null);
			call.type = this.type;
			call.method = this.constructor;
			assign.value = call;
			staticFields.addValue(assign);
		}
		if (!staticFields.isEmpty())
		{
			// Create the classinit method
			Method m = new Method(this);
			m.setQualifiedName("<clinit>");
			m.setType(Type.VOID);
			m.setModifiers(Modifiers.STATIC | Modifiers.MANDATED);
			m.setValue(staticFields);
			m.write(writer);
		}
	}
	
	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
		if (this.outerClass != null)
		{
			writer.visitInnerClass(this.internalName, this.outerClass.getInternalName(), this.qualifiedName, this.modifiers | Modifiers.STATIC);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			buffer.append(prefix);
			this.annotations[i].toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix).append(ModifierTypes.CLASS.toString(this.modifiers));
		buffer.append(ModifierTypes.CLASS_TYPE.toString(this.modifiers)).append(this.name);
		
		if (this.parameterCount > 0)
		{
			buffer.append('(');
			Util.astToString(prefix, this.parameters, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
			buffer.append(')');
		}
		
		if (this.genericCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, this.genericCount, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		
		if (this.superType == null)
		{
			buffer.append(" extends void");
		}
		else if (this.superType != Type.OBJECT)
		{
			buffer.append(" extends ");
			this.superType.toString("", buffer);
		}
		
		if (this.interfaceCount > 0)
		{
			buffer.append(" implements ");
			Util.astToString(prefix, this.interfaces, this.interfaceCount, Formatting.Class.superClassesSeperator, buffer);
		}
		
		if (this.body != null)
		{
			buffer.append('\n');
			this.body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(';');
		}
	}
}
