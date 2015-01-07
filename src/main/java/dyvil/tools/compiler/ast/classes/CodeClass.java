package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.util.*;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.expression.MethodCall;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.statement.FieldAssign;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.SuperValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.parser.type.ITypeVariable;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class CodeClass extends ASTNode implements IClass
{
	protected CompilationUnit		unit;
	
	protected int					modifiers;
	
	protected String				name;
	protected String				qualifiedName;
	protected String				internalName;
	
	protected List<Annotation>		annotations	= new ArrayList(1);
	
	protected IType					superType	= Type.OBJECT;
	protected List<IType>			interfaces	= new ArrayList(1);
	
	protected Type					type;
	protected List<ITypeVariable>	generics;
	
	protected ClassBody				body;
	
	public CodeClass()
	{
	}
	
	public CodeClass(ICodePosition position, CompilationUnit unit)
	{
		this.position = position;
		this.unit = unit;
	}
	
	@Override
	public CompilationUnit getUnit()
	{
		return this.unit;
	}
	
	@Override
	public Package getPackage()
	{
		return this.unit.pack;
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
		if (this.type == null)
		{
			this.type = new Type(this.name, this);
		}
		return this.type;
	}
	
	@Override
	public IClass getTheClass()
	{
		// TODO Outer class
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
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.qualifiedName;
		if ("dyvil.lang.annotation.sealed".equals(name))
		{
			this.modifiers |= Modifiers.SEALED;
			return true;
		}
		return false;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		for (Annotation a : this.annotations)
		{
			if (a.type.equals(type))
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
		this.internalName = this.unit.getInternalName(this.name);
		this.qualifiedName = this.unit.getQualifiedName(this.name);
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
		return this.qualifiedName.equals(name);
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
	public void addType(IType type)
	{
		this.generics.add((ITypeVariable) type);
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
	public boolean isSuperType(IType type)
	{
		if (this.superType != null && this.superType.isAssignableFrom(type))
		{
			return true;
		}
		for (IType i : this.interfaces)
		{
			if (i.isAssignableFrom(type))
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
		if (this.superType != null)
		{
			if (this.superType.isName("void"))
			{
				this.superType = null;
			}
			else
			{
				this.superType = this.superType.resolve(context);
			}
		}
		
		for (ListIterator<IType> iterator = this.interfaces.listIterator(); iterator.hasNext();)
		{
			IType i1 = iterator.next();
			IType i2 = i1.resolve(context);
			if (i1 != i2)
			{
				iterator.set(i2);
			}
		}
		
		if (this.generics != null)
		{
			for (ITypeVariable v : this.generics)
			{
				v.resolveTypes(markers, context);
			}
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolveTypes(markers, this);
		}
		
		this.body.resolveTypes(markers, this);
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		for (Iterator<Annotation> iterator = this.annotations.iterator(); iterator.hasNext();)
		{
			Annotation a = iterator.next();
			if (this.processAnnotation(a))
			{
				iterator.remove();
				continue;
			}
			
			a.resolve(markers, context);
		}
		
		this.body.resolve(markers, this);
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
					markers.add(new SemanticError(this.superType.getPosition(), "The " + Modifiers.CLASS_TYPE.toString(modifiers) + "'" + superClass.getName() + "' cannot be extended, only classes are allowed"));
				}
				else if ((modifiers & Modifiers.FINAL) != 0)
				{
					markers.add(new SemanticError(this.superType.getPosition(), "The final class '" + superClass.getName() + "' cannot be extended"));
				}
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
					markers.add(new SemanticError(t.getPosition(), "The " + Modifiers.CLASS_TYPE.toString(modifiers) + "'" + iclass.getName() + "' cannot be implemented, only interfaces are allowed"));
				}
			}
		}
		
		for (Annotation a : this.annotations)
		{
			a.check(markers, context);
		}
		
		this.body.check(markers, this);
	}
	
	@Override
	public void foldConstants()
	{
		for (Annotation a : this.annotations)
		{
			a.foldConstants();
		}
		
		this.body.foldConstants();
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Type getThisType()
	{
		return new Type(this.name, this);
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
					return var.getTheClass();
				}
			}
		}
		
		return this.unit.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
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
		
		FieldMatch match;
		
		// Inherited Fields
		if (this.superType != null && this != Type.PREDEF_CLASS)
		{
			match = this.superType.resolveField(context, name);
			if (match != null)
			{
				return match;
			}
		}
		
		for (IType i : this.interfaces)
		{
			match = i.resolveField(context, name);
			if (match != null)
			{
				return match;
			}
		}
		
		// Predef
		if (this != Type.PREDEF_CLASS)
		{
			match = Type.PREDEF_CLASS.resolveField(context, name);
			if (match != null)
			{
				return match;
			}
		}
		
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType... argumentTypes)
	{
		if (argumentTypes == null)
		{
			return null;
		}
		
		List<MethodMatch> list = new ArrayList();
		this.getMethodMatches(list, null, name, argumentTypes);
		
		if (!list.isEmpty())
		{
			Collections.sort(list);
			return list.get(0);
		}
		
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType... argumentTypes)
	{
		if (this.body != null)
		{
			this.body.getMethodMatches(list, type, name, argumentTypes);
			
			if (!list.isEmpty())
			{
				return;
			}
		}
		
		if (this.superType != null && this != Type.PREDEF_CLASS)
		{
			this.superType.getMethodMatches(list, type, name, argumentTypes);
		}
		for (IType i : this.interfaces)
		{
			i.getMethodMatches(list, type, name, argumentTypes);
		}
		
		if (list.isEmpty() && this != Type.PREDEF_CLASS)
		{
			Type.PREDEF_CLASS.getMethodMatches(list, type, name, argumentTypes);
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
			// Clear the SEALED bit by ORing with 0b1111
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
		String internalName = this.getInternalName();
		String signature = this.getSignature();
		String superClass = this.superType == null ? null : this.superType.getInternalName();
		String[] interfaces = this.getInterfaceArray();
		writer.visit(Opcodes.V1_8, this.modifiers & 0xFFFF, internalName, signature, superClass, interfaces);
		
		List<IField> fields = this.body.fields;
		List<IMethod> methods = this.body.methods;
		
		ThisValue thisValue = new ThisValue(null, this.type);
		StatementList instanceFields = new StatementList(null);
		StatementList staticFields = new StatementList(null);
		boolean instanceFieldsAdded = false;
		boolean staticFieldsAdded = false;
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/object;", true);
		}
		if ((this.modifiers & Modifiers.MODULE) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/module;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/sealed;", false);
		}
		
		for (Annotation a : this.annotations)
		{
			a.write(writer);
		}
		
		for (IField f : fields)
		{
			f.write(writer);
			
			if (f.hasModifier(Modifiers.LAZY))
			{
				continue;
			}
			
			IValue v = f.getValue();
			if (v != null)
			{
				if (f.hasModifier(Modifiers.STATIC))
				{
					FieldAssign assign = new FieldAssign(null, f.getName(), null);
					assign.value = v;
					assign.field = f;
					staticFields.addValue(assign);
				}
				else
				{
					FieldAssign assign = new FieldAssign(null, f.getName(), thisValue);
					assign.value = v;
					assign.field = f;
					instanceFields.addValue(assign);
				}
			}
		}
		
		for (IProperty p : this.body.properties)
		{
			p.write(writer);
		}
		
		for (IMethod m : methods)
		{
			String name = m.getName();
			if (name.equals("<init>"))
			{
				Util.prependValue(m, instanceFields);
				instanceFieldsAdded = true;
			}
			else if (name.equals("<clinit>"))
			{
				Util.prependValue(m, staticFields);
				staticFieldsAdded = true;
			}
			m.write(writer);
		}
		
		if (!instanceFieldsAdded && !instanceFields.isEmpty())
		{
			// Create the default constructor
			Method m = new Method(this);
			m.setQualifiedName("<init>");
			m.setType(Type.VOID);
			m.setModifiers(Modifiers.PUBLIC | Modifiers.MANDATED);
			
			// If this class has a superclass...
			if (this.superType != null)
			{
				IClass iclass = this.superType.getTheClass();
				if (iclass != null)
				{
					IMethod m1 = iclass.getBody().getMethod("<init>");
					// ... and the superclass has a default constructor
					if (m1 != null)
					{
						// Create the call to the super constructor
						MethodCall superConstructor = new MethodCall(null, new SuperValue(null, this.superType), "<init>");
						superConstructor.method = m1;
						instanceFields.getValues().add(0, superConstructor);
					}
				}
			}
			m.setValue(instanceFields);
			m.write(writer);
		}
		if (!staticFieldsAdded && !staticFields.isEmpty())
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
	public void toString(String prefix, StringBuilder buffer)
	{
		for (Annotation annotation : this.annotations)
		{
			buffer.append(prefix);
			annotation.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix).append(Modifiers.CLASS.toString(this.modifiers));
		buffer.append(Modifiers.CLASS_TYPE.toString(this.modifiers)).append(this.name);
		
		if (this.generics != null)
		{
			buffer.append('<');
			Util.astToString(this.generics, Formatting.Type.genericSeperator, buffer);
			buffer.append('>');
		}
		
		if (this.superType != null)
		{
			buffer.append(" extends ");
			this.superType.toString("", buffer);
		}
		else
		{
			buffer.append(" extends void");
		}
		if (!this.interfaces.isEmpty())
		{
			buffer.append(" implements ");
			Util.astToString(this.interfaces, Formatting.Class.superClassesSeperator, buffer);
		}
		
		if (this.body != null)
		{
			this.body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(';');
		}
	}
}
