package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.util.*;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.DyvilFile;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.SuperValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class CodeClass extends ASTNode implements IClass
{
	protected DyvilFile				unit;
	protected IClass				outerClass;
	
	protected int					modifiers;
	
	protected String				name;
	protected String				qualifiedName;
	protected String				fullName;
	protected String				internalName;
	
	protected List<Annotation>		annotations;
	
	protected IType					superType	= Type.OBJECT;
	protected List<IType>			interfaces	= new ArrayList(1);
	
	protected Type					type;
	protected List<ITypeVariable>	generics;
	
	protected ClassBody				body;
	protected IMethod				functionalMethod;
	protected IField				instanceField;
	protected IMethod				constructor;
	protected IMethod				superConstructor;
	
	public CodeClass()
	{
		this.type = new Type(this);
	}
	
	public CodeClass(ICodePosition position, DyvilFile unit)
	{
		this.position = position;
		this.unit = unit;
		this.type = new Type(this);
	}
	
	public CodeClass(ICodePosition position, DyvilFile unit, int modifiers, List<Annotation> annotations)
	{
		this.position = position;
		this.unit = unit;
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.type = new Type(this);
	}
	
	@Override
	public DyvilFile getUnit()
	{
		return this.unit;
	}
	
	@Override
	public Package getPackage()
	{
		return this.unit.pack;
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
	public boolean equals(IClass iclass)
	{
		return this == iclass;
	}
	
	@Override
	public void setType(IType type)
	{
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		if (!this.processAnnotation(annotation))
		{
			annotation.target = ElementType.TYPE;
			
			if (this.annotations == null)
			{
				this.annotations = new ArrayList(2);
			}
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.fullName;
		if ("dyvil.lang.annotation.sealed".equals(name))
		{
			this.modifiers |= Modifiers.SEALED;
			return true;
		}
		if ("java.lang.Deprecated".equals(name))
		{
			this.modifiers |= Modifiers.DEPRECATED;
			return true;
		}
		if ("java.lang.FunctionalInterface".equals(name))
		{
			this.modifiers |= Modifiers.FUNCTIONAL;
			return true;
		}
		return false;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		for (Annotation a : this.annotations)
		{
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
	
	@Override
	public void setGeneric()
	{
		this.generics = new ArrayList(2);
	}
	
	@Override
	public boolean isGeneric()
	{
		return this.generics != null;
	}
	
	@Override
	public void setTypeVariables(List<ITypeVariable> list)
	{
		this.generics = list;
	}
	
	@Override
	public List<ITypeVariable> getTypeVariables()
	{
		return this.generics;
	}
	
	@Override
	public void addTypeVariable(ITypeVariable var)
	{
		this.generics.add(var);
	}
	
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
		for (IType i : this.interfaces)
		{
			if (type.isSuperTypeOf2(i))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setInterfaces(List<IType> interfaces)
	{
		this.interfaces = interfaces;
	}
	
	@Override
	public List<IType> getInterfaces()
	{
		return this.interfaces;
	}
	
	@Override
	public void addInterface(IType type)
	{
		this.interfaces.add(type);
	}
	
	@Override
	public void setBody(ClassBody body)
	{
		this.body = body;
	}
	
	@Override
	public ClassBody getBody()
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
		if (this.functionalMethod == null)
		{
			if ((this.modifiers & Modifiers.FUNCTIONAL) != Modifiers.FUNCTIONAL)
			{
				return null;
			}
			
			for (IMethod m : this.body.methods)
			{
				if (m.hasModifier(Modifiers.ABSTRACT))
				{
					this.functionalMethod = m;
					return m;
				}
			}
		}
		return this.functionalMethod;
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
		int len = this.interfaces.size();
		String[] interfaces = new String[len];
		for (int i = 0; i < len; i++)
		{
			interfaces[i] = this.interfaces.get(i).getInternalName();
		}
		return interfaces;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.generics != null)
		{
			GenericType type = new GenericType(this);
			
			for (ITypeVariable v : this.generics)
			{
				v.resolveTypes(markers, context);
				type.addType(new WildcardType(null, 0, v.getCaptureClass()));
			}
			
			this.type = type;
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
		
		for (ListIterator<IType> iterator = this.interfaces.listIterator(); iterator.hasNext();)
		{
			IType i1 = iterator.next();
			IType i2 = i1.resolve(markers, context);
			if (i1 != i2)
			{
				iterator.set(i2);
			}
		}
		
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.resolveTypes(markers, this);
			}
		}
		
		if (this.body != null)
		{
			this.body.resolveTypes(markers, this);
			
			for (IMethod m : this.body.methods)
			{
				if (m.isName("<init>"))
				{
					return;
				}
			}
		}
		
		Method constructor = new Method(this);
		constructor.setName("new", "<init>");
		constructor.setType(Type.VOID);
		constructor.setModifiers(Modifiers.PUBLIC | Modifiers.SYNTHETIC);
		this.constructor = constructor;
		
		if (this.superType != null)
		{
			MethodMatch match = this.superType.resolveConstructor(Util.EMPTY_VALUES);
			if (match != null)
			{
				this.superConstructor = match.theMethod;
			}
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		if (this.annotations != null)
		{
			Iterator<Annotation> iterator = this.annotations.iterator();
			while (iterator.hasNext())
			{
				Annotation a = iterator.next();
				if (this.processAnnotation(a))
				{
					iterator.remove();
					continue;
				}
				
				a.resolve(markers, context);
			}
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			this.instanceField = new Field(this, "$instance", this.getType(), Modifiers.PUBLIC | Modifiers.CONST | Modifiers.SYNTHETIC, Collections.EMPTY_LIST);
		}
		
		if (this.body != null)
		{
			this.body.resolve(markers, this);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.superType != null)
		{
			IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				int modifiers = superClass.getModifiers();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != 0)
				{
					markers.add(Markers.create(this.superType.getPosition(), "class.extend.type", ModifierTypes.CLASS_TYPE.toString(modifiers),
							superClass.getName()));
				}
				else if ((modifiers & Modifiers.FINAL) != 0)
				{
					markers.add(Markers.create(this.superType.getPosition(), "class.extend.final", superClass.getName()));
				}
				else if ((modifiers & Modifiers.DEPRECATED) != 0)
				{
					markers.add(Markers.create(this.superType.getPosition(), "class.extend.deprecated", superClass.getName()));
				}
			}
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			IMethod m = this.body.getMethod("<init>");
			if (m != null)
			{
				markers.add(Markers.create(m.getPosition(), "class.object.constructor", this.name));
			}
		}
		
		for (IType t : this.interfaces)
		{
			IClass iclass = t.getTheClass();
			if (iclass != null)
			{
				int modifiers = iclass.getModifiers();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != Modifiers.INTERFACE_CLASS)
				{
					markers.add(Markers.create(t.getPosition(), "class.implement.type", ModifierTypes.CLASS_TYPE.toString(modifiers), iclass.getName()));
				}
				else if ((modifiers & Modifiers.DEPRECATED) != 0)
				{
					markers.add(Markers.create(t.getPosition(), "class.implement.deprecated", iclass.getName()));
				}
			}
		}
		
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.check(markers, context);
			}
		}
		
		if (this.body != null)
		{
			this.body.check(markers, this);
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.foldConstants();
			}
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
	public Type getThisType()
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
		if (this.generics != null)
		{
			for (ITypeVariable var : this.generics)
			{
				if (var.isName(name))
				{
					return var.getCaptureClass();
				}
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
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
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
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		if (this.constructor != null && arguments.isEmpty())
		{
			return new MethodMatch(this.constructor, 1);
		}
		
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
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
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
		for (IType i : this.interfaces)
		{
			i.getMethodMatches(list, instance, name, arguments);
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
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
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
			
			for (IType i : this.interfaces)
			{
				if (i.getTheClass() == iclass)
				{
					return member.getAccessibility();
				}
			}
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.PACKAGE)
		{
			if (iclass.getPackage() == this.unit.pack)
			{
				return member.getAccessibility();
			}
		}
		
		return INVISIBLE;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		// Header
		
		String internalName = this.getInternalName();
		String signature = this.getSignature();
		String superClass = null;
		int interfaceCount = this.interfaces.size();
		String[] interfaces = new String[interfaceCount];
		
		if (this.superType != null)
		{
			superClass = this.superType.getInternalName();
		}
		
		for (int i = 0; i < interfaceCount; i++)
		{
			IType type = this.interfaces.get(i);
			interfaces[i] = type.getInternalName();
		}
		
		writer.visit(Opcodes.V1_8, this.modifiers & 0xFFFF, internalName, signature, superClass, interfaces);
		
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
		
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.write(writer);
			}
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
		
		for (int i = 0; i < interfaceCount; i++)
		{
			IType type = this.interfaces.get(i);
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}
		
		// Fields, Methods and Properties
		
		List<IField> fields;
		List<IMethod> methods;
		List<IProperty> properties;
		if (this.body != null)
		{
			fields = this.body.fields;
			methods = this.body.methods;
			properties = this.body.properties;
			
			for (IClass iclass : this.body.classes)
			{
				iclass.writeInnerClassInfo(writer);
			}
			
			if (this.body.lambdas != null)
			{
				for (IBaseMethod m : this.body.lambdas)
				{
					m.write(writer);
				}
			}
		}
		else
		{
			fields = Collections.EMPTY_LIST;
			methods = Collections.EMPTY_LIST;
			properties = Collections.EMPTY_LIST;
		}
		
		ThisValue thisValue = new ThisValue(null, this.type);
		IField instanceField = this.instanceField;
		StatementList instanceFields = new StatementList(null);
		StatementList staticFields = new StatementList(null);
		boolean hasConstructor = false;
		
		for (IField f : fields)
		{
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
			call.arguments = Util.EMPTY_VALUES;
			instanceFields.getValues().add(0, call);
		}
		
		if (this.constructor != null)
		{
			this.constructor.setValue(instanceFields);
			this.constructor.write(writer);
		}
		
		for (IProperty p : properties)
		{
			p.write(writer);
		}
		
		for (IMethod m : methods)
		{
			String name = m.getName();
			m.write(writer);
		}
		
		if ((this.modifiers & Modifiers.CASE_CLASS) != 0)
		{
			MethodWriter mw = new MethodWriter(writer,
					writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals", "(Ljava/lang/Object;)Z", null, null));
			mw.visitParameter("obj", "Ljava/lang/Object;", 0);
			mw.addLocal(this.type);
			mw.visitCode();
			CaseClasses.writeEquals(mw, this, fields);
			mw.visitEnd();
			
			mw = new MethodWriter(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "hashCode", "()I", null, null));
			mw.addLocal(this.type);
			mw.visitCode();
			CaseClasses.writeHashCode(mw, this, fields);
			mw.visitEnd();
			
			mw = new MethodWriter(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "toString", "()Ljava/lang/String;", null, null));
			mw.addLocal(this.type);
			mw.visitCode();
			CaseClasses.writeToString(mw, this, fields);
			mw.visitEnd();
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
		if (this.annotations != null)
		{
			for (Annotation annotation : this.annotations)
			{
				buffer.append(prefix);
				annotation.toString(prefix, buffer);
				buffer.append('\n');
			}
		}
		
		buffer.append(prefix).append(ModifierTypes.CLASS.toString(this.modifiers));
		buffer.append(ModifierTypes.CLASS_TYPE.toString(this.modifiers)).append(this.name);
		
		if (this.generics != null && !this.generics.isEmpty())
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, Formatting.Type.genericSeperator, buffer);
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
		
		if (!this.interfaces.isEmpty())
		{
			buffer.append(" implements ");
			Util.astToString(prefix, this.interfaces, Formatting.Class.superClassesSeperator, buffer);
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
