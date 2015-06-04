package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.Opcodes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.TypeVariableType;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public class CodeClass extends ASTNode implements IClass
{
	protected IDyvilHeader			unit;
	protected IClass				outerClass;
	
	protected Annotation[]			annotations;
	protected int					annotationCount;
	protected int					modifiers;
	
	protected Name					name;
	protected String				fullName;
	protected String				internalName;
	
	protected ITypeVariable[]		generics;
	protected int					genericCount;
	
	protected IParameter[]			parameters;
	protected int					parameterCount;
	
	protected IType					superType	= Types.OBJECT;
	protected IType[]				interfaces;
	protected int					interfaceCount;
	
	protected IType					type;
	
	protected IClassCompilable[]	compilables;
	protected int					compilableCount;
	
	protected IClassBody			body;
	protected IClassMetadata		metadata;
	
	public CodeClass()
	{
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit)
	{
		this.position = position;
		this.unit = unit;
		this.interfaces = new IType[1];
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit, int modifiers)
	{
		this.position = position;
		this.unit = unit;
		this.modifiers = modifiers;
		this.interfaces = new IType[1];
	}
	
	@Override
	public void setUnit(IDyvilHeader unit)
	{
		this.unit = unit;
	}
	
	@Override
	public IDyvilHeader getUnit()
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
		switch (type)
		{
		case "dyvil.lang.annotation.sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "dyvil.lang.annotation.Strict":
			this.modifiers |= Modifiers.STRICT;
			return false;
		case "java.lang.Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		case "java.lang.FunctionalInterface":
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
	public Annotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (a.type.getTheClass() == type)
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
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public boolean isAbstract()
	{
		return (this.modifiers & Modifiers.ABSTRACT) != 0;
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers & Modifiers.ACCESS_MODIFIERS;
	}
	
	// Names
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
		this.internalName = this.unit.getInternalName(name.qualified);
		this.fullName = this.unit.getFullName(name.qualified);
	}
	
	@Override
	public Name getName()
	{
		return this.name;
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
		if (index >= this.generics.length)
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
	
	// Class Parameters
	
	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}
	
	@Override
	public void setParameter(int index, IParameter param)
	{
		param.setTheClass(this);
		this.parameters[index] = param;
	}
	
	@Override
	public void addParameter(IParameter param)
	{
		param.setTheClass(this);
		IConstructor constructor = this.metadata.getConstructor();
		
		if (this.parameters == null)
		{
			this.parameters = new ClassParameter[2];
			this.parameters[0] = param;
			this.parameterCount = 1;
			
			if (constructor != null)
			{
				constructor.setParameters(this.parameters, 1);
			}
			return;
		}
		
		int index = this.parameterCount++;
		if (this.parameterCount > this.parameters.length)
		{
			ClassParameter[] temp = new ClassParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = param;
		
		if (constructor != null)
		{
			constructor.setParameters(this.parameters, this.parameterCount);
		}
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
	public int typeCount()
	{
		return this.interfaceCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.interfaces[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		this.addInterface(type);
	}
	
	@Override
	public IType getType(int index)
	{
		return this.interfaces[index];
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
		if (index >= this.interfaces.length)
		{
			IType[] temp = new IType[this.interfaceCount];
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
	public void setMetadata(IClassMetadata metadata)
	{
		this.metadata = metadata;
	}
	
	@Override
	public IClassMetadata getMetadata()
	{
		return this.metadata;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		// Copy in ExternalClass
		if ((this.modifiers & Modifiers.ABSTRACT) == 0)
		{
			return null;
		}
		
		IMethod m;
		if (this.body != null)
		{
			m = this.body.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		if (this.superType != null)
		{
			m = this.superType.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			m = this.interfaces[i].getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.internalName;
	}
	
	@Override
	public String getSignature()
	{
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
		
		if (this.superType != null)
		{
			this.superType.appendSignature(buffer);
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].appendSignature(buffer);
		}
		return buffer.toString();
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
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (this.superType != null)
		{
			IType type = this.superType.resolveType(typeVar);
			if (type != Types.ANY)
			{
				return type.getConcreteType(concrete);
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i].resolveType(typeVar);
			if (type != Types.ANY)
			{
				return type.getConcreteType(concrete);
			}
		}
		return Types.ANY;
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
				type.addType(new TypeVariableType(var));
			}
			
			this.type = type;
		}
		else
		{
			this.type = new Type(this);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
		
		int index = 1;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			IType type = param.getType();
			param.resolveTypes(markers, this);
			param.setIndex(index);
			if (type == Types.LONG || type == Types.DOUBLE)
			{
				index += 2;
			}
			else
			{
				index++;
			}
		}
		
		if (this.superType != null)
		{
			this.superType = this.superType.resolve(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolve(markers, this);
		}
		
		if (this.body != null)
		{
			this.body.resolveTypes(markers, this);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			String fullName = a.type.getInternalName();
			if (fullName != null && !this.addRawAnnotation(fullName))
			{
				this.removeAnnotation(i--);
				continue;
			}
			
			a.resolve(markers, context);
		}
		
		this.metadata.resolve(markers, context);
		
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
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.metadata.checkTypes(markers, context);
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].checkTypes(markers, context);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, this);
		}
		
		if (this.body != null)
		{
			this.body.checkTypes(markers, this);
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
			this.annotations[i].check(markers, context, ElementType.TYPE);
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
	public IClass getThisClass()
	{
		return this;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		if (this.body != null)
		{
			IClass clazz = this.body.getClass(name);
			if (clazz != null)
			{
				return clazz;
			}
		}
		
		if (this.outerClass != null)
		{
			return this.outerClass.resolveClass(name);
		}
		return this.unit.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		ITypeVariable var;
		for (int i = 0; i < this.genericCount; i++)
		{
			var = this.generics[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
		return null;
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
		
		if (this.body != null)
		{
			// Own properties
			IField field = this.body.getProperty(name);
			if (field != null)
			{
				return field;
			}
			
			// Own fields
			field = this.body.getField(name);
			if (field != null)
			{
				return field;
			}
		}
		
		IField match = this.metadata.resolveField(name);
		if (match != null)
		{
			return match;
		}
		
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
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.body != null)
		{
			this.body.getMethodMatches(list, instance, name, arguments);
		}
		
		this.metadata.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		
		if (!list.isEmpty())
		{
			return;
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
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.body != null)
		{
			this.body.getConstructorMatches(list, arguments);
		}
		
		this.metadata.getConstructorMatches(list, arguments);
	}
	
	@Override
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount)
	{
		if (this.body != null)
		{
			IMethod m = this.body.getMethod(name, parameters, parameterCount);
			if (m != null)
			{
				return m;
			}
		}
		return null;
	}
	
	@Override
	public IMethod getSuperMethod(Name name, IParameter[] parameters, int parameterCount)
	{
		if (this.superType != null)
		{
			IClass iclass = this.superType.getTheClass();
			if (iclass != null)
			{
				IMethod m = iclass.getMethod(name, parameters, parameterCount);
				if (m != null)
				{
					return m;
				}
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IClass iclass = this.interfaces[i].getTheClass();
			if (iclass != null)
			{
				IMethod m = iclass.getMethod(name, parameters, parameterCount);
				if (m != null)
				{
					return m;
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean isMember(IMember member)
	{
		return this == member.getTheClass();
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return false;
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == this || iclass == null)
		{
			return VISIBLE;
		}
		
		int level = member.getAccessLevel();
		if ((level & Modifiers.SEALED) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return SEALED;
			}
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.DERIVED)
		{
			if (this.superType != null && this.superType.getTheClass() == iclass)
			{
				return VISIBLE;
			}
			
			for (int i = 0; i < this.interfaceCount; i++)
			{
				if (this.interfaces[i].getTheClass() == iclass)
				{
					return VISIBLE;
				}
			}
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.PACKAGE)
		{
			IDyvilHeader unit1 = this.unit;
			IDyvilHeader unit2 = iclass.getUnit();
			if (unit1 != null && unit2 != null && unit1.getPackage() == unit2.getPackage())
			{
				return VISIBLE;
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
	public void write(ClassWriter writer) throws BytecodeException
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
		
		int mods = this.modifiers & 0x67631;
		if ((mods & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
		{
			mods |= Opcodes.ACC_SUPER;
		}
		writer.visit(DyvilCompiler.classVersion, mods, internalName, signature, superClass, interfaces);
		
		// Outer Class
		
		if (this.outerClass != null)
		{
			writer.visitOuterClass(this.outerClass.getInternalName(), null, null);
		}
		
		// Annotations
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			writer.visitAnnotation("Ldyvil/annotation/object;", true);
		}
		if ((this.modifiers & Modifiers.SEALED) != 0)
		{
			writer.visitAnnotation("Ldyvil/annotation/sealed;", false);
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
		int constructors = 0;
		int methods = 0;
		int properties = 0;
		if (this.body != null)
		{
			fields = this.body.fieldCount();
			methods = this.body.methodCount();
			constructors = this.body.constructorCount();
			properties = this.body.propertyCount();
			
			int classes = this.body.classCount();
			for (int i = 0; i < classes; i++)
			{
				this.body.getClass(i).writeInnerClassInfo(writer);
			}
		}
		
		ThisValue thisValue = new ThisValue(this.type);
		StatementList instanceFields = new StatementList();
		StatementList staticFields = new StatementList();
		
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
				assign.name = f.getName();
				assign.value = f.getValue();
				assign.field = f;
				staticFields.addValue(assign);
			}
			else
			{
				FieldAssign assign = new FieldAssign(null);
				assign.name = f.getName();
				assign.instance = thisValue;
				assign.value = f.getValue();
				assign.field = f;
				instanceFields.addValue(assign);
			}
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(writer);
		}
		
		for (int i = 0; i < constructors; i++)
		{
			this.body.getConstructor(i).write(writer, instanceFields);
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
		
		this.metadata.write(writer, instanceFields);
		
		IField instanceField = this.metadata.getInstanceField();
		if (instanceField != null)
		{
			FieldAssign assign = new FieldAssign(null);
			assign.name = Name.instance;
			assign.field = instanceField;
			ConstructorCall call = new ConstructorCall(null);
			call.type = this.type;
			call.constructor = this.metadata.getConstructor();
			assign.value = call;
			staticFields.addValue(assign);
		}
		else if (staticFields.isEmpty())
		{
			return;
		}
		
		// Create the classinit method
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.STATIC, "<clinit>", "()V", null, null));
		mw.begin();
		staticFields.writeStatement(mw);
		mw.end(Types.VOID);
	}
	
	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
		if (this.outerClass != null)
		{
			int mods = this.modifiers & 0x761F;
			if ((mods & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
			{
				mods |= Opcodes.ACC_STATIC;
			}
			else
			{
				mods &= ~Opcodes.ACC_STATIC;
			}
			writer.visitInnerClass(this.internalName, this.outerClass.getInternalName(), this.name.qualified, mods);
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
		
		if (this.genericCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, this.genericCount, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		
		if (this.parameterCount > 0)
		{
			buffer.append('(');
			Util.astToString(prefix, this.parameters, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
			buffer.append(')');
		}
		
		if (this.superType == null)
		{
			buffer.append(" extends void");
		}
		else if (this.superType != Types.OBJECT)
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
			buffer.append('\n').append(prefix);
			this.body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(';');
		}
	}
}
