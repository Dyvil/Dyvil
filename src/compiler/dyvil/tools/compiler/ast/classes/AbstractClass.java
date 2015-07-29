package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.VariableThis;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;

public abstract class AbstractClass implements IClass
{
	// Metadata
	
	protected IClass			outerClass;
	protected IClassMetadata	metadata;
	protected IType				type;
	
	// Modifiers and Annotations
	
	protected IAnnotation[]	annotations;
	protected int			annotationCount;
	protected int			modifiers;
	
	// Signature
	
	protected Name		name;
	protected String	fullName;
	protected String	internalName;
	
	protected ITypeVariable[]	generics;
	protected int				genericCount;
	
	protected IParameter[]	parameters;
	protected int			parameterCount;
	
	protected IType		superType	= Types.OBJECT;
	protected IType[]	interfaces;
	protected int		interfaceCount;
	
	// Body
	
	protected IClassBody			body;
	protected IClassCompilable[]	compilables;
	protected int					compilableCount;
	
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
	public int annotationCount()
	{
		return this.annotationCount;
	}
	
	@Override
	public void setAnnotations(IAnnotation[] annotations, int count)
	{
		this.annotations = annotations;
		this.annotationCount = count;
	}
	
	@Override
	public void setAnnotation(int index, IAnnotation annotation)
	{
		this.annotations[index] = annotation;
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new IAnnotation[3];
			this.annotations[0] = annotation;
			this.annotationCount = 1;
			return;
		}
		
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			IAnnotation[] temp = new IAnnotation[this.annotationCount];
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
		case "dyvil/annotation/sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "dyvil/annotation/Strict":
			this.modifiers |= Modifiers.STRICT;
			return false;
		case "dyvil/annotation/object":
			this.modifiers |= Modifiers.OBJECT_CLASS;
			return false;
		case "java/lang/Deprecated":
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		case "java/lang/FunctionalInterface":
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
	public IAnnotation[] getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public IAnnotation getAnnotation(int index)
	{
		return this.annotations[index];
	}
	
	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			IAnnotation a = this.annotations[i];
			if (a.getType().getTheClass() == type)
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
	public boolean isInterface()
	{
		return (this.modifiers & Modifiers.INTERFACE_CLASS) == Modifiers.INTERFACE_CLASS;
	}
	
	@Override
	public boolean isObject()
	{
		return (this.modifiers & Modifiers.OBJECT_CLASS) != 0;
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
		
		if (this.parameters == null)
		{
			this.parameters = new ClassParameter[2];
			this.parameters[0] = param;
			this.parameterCount = 1;
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
		IClass iclass = type.getTheClass();
		if (this == iclass || this.superType != null && type.isSuperClassOf(this.superType))
		{
			return true;
		}
		if (!iclass.isInterface())
		{
			return false;
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			if (type.isSuperClassOf(this.interfaces[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getSuperTypeDistance(IType superType)
	{
		IClass iclass = superType.getTheClass();
		if (this == iclass)
		{
			return 1;
		}
		
		int max = this.superType != null ? superType.getSubClassDistance(this.superType) : 0;
		if (!iclass.isInterface())
		{
			return max;
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			int m = superType.getSubClassDistance(this.interfaces[i]);
			if (m > max)
			{
				max = m;
			}
		}
		return max == 0 ? 0 : 1 + max;
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
	public int compilableCount()
	{
		return this.compilableCount;
	}
	
	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		compilable.setInnerIndex(this.internalName, this.compilableCount);
		
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
			if (type != Types.UNKNOWN)
			{
				return type.getConcreteType(concrete);
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i].resolveType(typeVar);
			if (type != Types.UNKNOWN)
			{
				return type.getConcreteType(concrete);
			}
		}
		return Types.UNKNOWN;
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
			IClass iclass = this.body.getClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		
		if (this.outerClass != null)
		{
			return this.outerClass.resolveClass(name);
		}
		
		IDyvilHeader header = this.getHeader();
		return header == null ? null : header.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		if (name == this.name)
		{
			return this.type;
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return new TypeVarType(var);
			}
		}
		
		IClass iclass = this.resolveClass(name);
		return iclass != null ? new ClassType(iclass) : null;
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
		
		return null;
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
		
		if (this.body != null)
		{
			// Own properties
			IDataMember field = this.body.getProperty(name);
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
		
		IDataMember match = this.metadata.resolveField(name);
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
		
		// Static Imports
		IDyvilHeader header = this.getHeader();
		if (header != null && header.hasMemberImports())
		{
			match = header.resolveField(name);
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
		
		IDyvilHeader header = this.getHeader();
		if (header != null && header.hasMemberImports())
		{
			header.getMethodMatches(list, instance, name, arguments);
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
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount, IType concrete)
	{
		if (this.body != null)
		{
			IMethod m = this.body.getMethod(name, parameters, parameterCount, concrete);
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
				IMethod m = iclass.getMethod(name, parameters, parameterCount, this.superType);
				if (m != null)
				{
					return m;
				}
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				IMethod m = iclass.getMethod(name, parameters, parameterCount, type);
				if (m != null)
				{
					return m;
				}
			}
		}
		return null;
	}
	
	@Override
	public IDataMember getSuperField(Name name)
	{
		if (this.superType != null)
		{
			IClass iclass = this.superType.getTheClass();
			if (iclass != null)
			{
				IDataMember m = iclass.resolveField(name);
				if (m != null)
				{
					return m;
				}
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				IDataMember m = iclass.resolveField(name);
				if (m != null)
				{
					return m;
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return false;
	}
	
	@Override
	public IVariable capture(IVariable variable)
	{
		return null;
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		return type == this ? VariableThis.DEFAULT : null;
	}
	
	@Override
	public boolean isMember(IClassMember member)
	{
		return this == member.getTheClass();
	}
	
	@Override
	public byte getVisibility(IClassMember member)
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
			IDyvilHeader unit1 = this.getHeader();
			IDyvilHeader unit2 = iclass.getHeader();
			if (unit1 != null && unit2 != null && unit1.getPackage() == unit2.getPackage())
			{
				return VISIBLE;
			}
		}
		
		return INVISIBLE;
	}
	
	@Override
	public String getFileName()
	{
		return this.getName().qualified;
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
