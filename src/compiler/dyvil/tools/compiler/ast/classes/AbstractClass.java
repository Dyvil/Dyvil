package dyvil.tools.compiler.ast.classes;

import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.VariableThis;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
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
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public abstract class AbstractClass implements IClass
{
	// Metadata
	
	protected IClass         outerClass;
	protected IClassMetadata metadata;
	protected IType          type;
	
	// Modifiers and Annotations
	
	protected AnnotationList annotations;
	protected ModifierSet    modifiers;
	
	// Signature
	
	protected Name   name;
	protected String fullName;
	protected String internalName;
	
	protected ITypeVariable[] generics;
	protected int             genericCount;
	
	protected IParameter[] parameters;
	protected int          parameterCount;
	
	protected IType superType = Types.OBJECT;
	protected IType[] interfaces;
	protected int     interfaceCount;
	
	// Body
	
	protected IClassBody         body;
	protected IClassCompilable[] compilables;
	protected int                compilableCount;
	
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
	public IType getClassType()
	{
		return new ClassType(this);
	}
	
	@Override
	public AnnotationList getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public void setAnnotations(AnnotationList annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
		}
		this.annotations.addAnnotation(annotation);
	}
	
	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case "dyvil/annotation/_internal/internal":
			this.modifiers.addIntModifier(Modifiers.INTERNAL);
			return false;
		case "dyvil/annotation/_internal/sealed":
			this.modifiers.addModifier(BaseModifiers.INTERNAL);
			return false;
		case "dyvil/annotation/Strict":
			this.modifiers.addIntModifier(Modifiers.STRICT);
			return false;
		case "dyvil/annotation/_internal/object":
			this.modifiers.addIntModifier(Modifiers.OBJECT_CLASS);
			return false;
		case Deprecation.DYVIL_INTERNAL:
		case Deprecation.JAVA_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		case "java/lang/FunctionalInterface":
			this.modifiers.addIntModifier(Modifiers.FUNCTIONAL);
			return false;
		case "dyvil/annotation/_internal/ClassParameters":
			if (annotation != null)
			{
				this.readClassParameters(annotation);
				return false;
			}
		}
		return true;
	}
	
	protected void readClassParameters(IAnnotation annotation)
	{
	}
	
	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.annotations.getAnnotation(type);
	}
	
	@Override
	public ElementType getElementType()
	{
		if (this.isAnnotation())
		{
			return ElementType.ANNOTATION_TYPE;
		}
		return ElementType.TYPE;
	}
	
	@Override
	public void setModifiers(ModifierSet modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public ModifierSet getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public boolean isAbstract()
	{
		return this.modifiers.hasIntModifier(Modifiers.ABSTRACT);
	}
	
	@Override
	public boolean isInterface()
	{
		return this.modifiers.hasIntModifier(Modifiers.INTERFACE_CLASS);
	}

	@Override
	public boolean isAnnotation()
	{
		return this.modifiers.hasIntModifier(Modifiers.ANNOTATION);
	}

	@Override
	public boolean isObject()
	{
		return this.modifiers.hasIntModifier(Modifiers.OBJECT_CLASS);
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers.toFlags() & Modifiers.ACCESS_MODIFIERS;
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
			IParameter[] temp = new IParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = param;
		param.setIndex(index);
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
	public boolean checkImplements(MarkerList markers, IClass iclass, IMethod candidate, ITypeContext typeContext)
	{
		if (candidate.getTheClass() == this)
		{
			return !candidate.hasModifier(Modifiers.ABSTRACT);
		}
		
		if (this.body != null && this.body.checkImplements(markers, iclass, candidate, typeContext))
		{
			return true;
		}
		
		if (this.superType != null)
		{
			if (this.superType.getTheClass()
			                  .checkImplements(markers, iclass, candidate, this.superType.getConcreteType(typeContext)))
			{
				return true;
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			if (type.getTheClass().checkImplements(markers, iclass, candidate, type.getConcreteType(typeContext)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void checkMethods(MarkerList markers, IClass iclass, ITypeContext typeContext, Set<IClass> checkedClasses)
	{
		if (checkedClasses.contains(this))
		{
			return;
		}
		checkedClasses.add(this);

		if (this.body != null)
		{
			this.body.checkMethods(markers, iclass, typeContext);
		}
		
		this.checkSuperMethods(markers, iclass, typeContext, checkedClasses);
	}

	public void checkSuperMethods(MarkerList markers, IClass thisClass, ITypeContext typeContext, Set<IClass> checkedClasses)
	{
		if (this.superType != null)
		{
			final IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				superClass
						.checkMethods(markers, thisClass, this.superType.getConcreteType(typeContext), checkedClasses);
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			final IType type = this.interfaces[i];
			final IClass iClass = type.getTheClass();
			if (iClass != null && iClass != this)
			{
				iClass.checkMethods(markers, thisClass, type.getConcreteType(typeContext), checkedClasses);
			}
		}
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		// Copy in ExternalClass
		if (!this.isAbstract())
		{
			return null;
		}
		
		if (this.body != null)
		{
			IMethod m = this.body.getFunctionalMethod();
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
		if (this.interfaceCount <= 0)
		{
			return null;
		}
		
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
			if (type != null)
			{
				return type.getConcreteType(concrete);
			}
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i].resolveType(typeVar);
			if (type != null)
			{
				return type.getConcreteType(concrete);
			}
		}
		return null;
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
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
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
		return variable;
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		return type == this ? VariableThis.DEFAULT : null;
	}
	
	@Override
	public IAccessible getAccessibleImplicit()
	{
		return null;
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
		if ((level & Modifiers.INTERNAL) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return INTERNAL;
			}
			// Clear the INTERNAL bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		if (level == Modifiers.PROTECTED)
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
		int index = this.internalName.lastIndexOf('/');
		if (index < 0)
		{
			return this.internalName;
		}
		return this.internalName.substring(index + 1);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}
		
		buffer.append(prefix);

		this.modifiers.toString(buffer);
		ModifierUtil.writeClassType(this.modifiers.toFlags(), buffer);
		buffer.append(this.name);
		
		if (this.genericCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '[');
			Util.astToString(prefix, this.generics, this.genericCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", ']');
		}
		
		if (this.parameterCount > 0)
		{
			Formatting.appendSeparator(buffer, "parameters.open_paren", '(');
			Util.astToString(prefix, this.parameters, this.parameterCount,
			                 Formatting.getSeparator("parameters.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "parameters.close_paren", ')');
		}
		
		if (this.superType == null)
		{
			buffer.append(" extends void");
		}
		else if (this.superType != Types.OBJECT)
		{
			String extendsPrefix = prefix;
			if (Formatting.getBoolean("class.extends.newline"))
			{
				extendsPrefix = Formatting.getIndent("class.extends.indent", extendsPrefix);
				buffer.append('\n').append(extendsPrefix).append("extends ");
			}
			else
			{
				buffer.append(" extends ");
			}

			this.superType.toString("", buffer);
		}
		
		if (this.interfaceCount > 0)
		{
			String implementsPrefix = prefix;
			if (Formatting.getBoolean("class.implements.newline"))
			{
				implementsPrefix = Formatting.getIndent("class.implements.indent", implementsPrefix);
				buffer.append('\n').append(implementsPrefix).append("implements ");
			}
			else
			{
				buffer.append(" implements ");
			}

			Util.astToString(implementsPrefix, this.interfaces, this.interfaceCount,
			                 Formatting.getSeparator("class.implements.separator", ','), buffer);
		}
		
		if (this.body != null)
		{
			String bodyPrefix = Formatting.getIndent("class.body.indent", prefix);
			if (Formatting.getBoolean("class.body.open_bracket.newline"))
			{
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
			}

			buffer.append('{').append('\n');

			this.body.toString(bodyPrefix, buffer);
			buffer.append(prefix).append('}');

			if (Formatting.getBoolean("class.body.close_bracket.newline_after"))
			{
				buffer.append('\n');
			}
		}
		else if (Formatting.getBoolean("class.semicolon"))
		{
			buffer.append(';');
		}
	}
}
