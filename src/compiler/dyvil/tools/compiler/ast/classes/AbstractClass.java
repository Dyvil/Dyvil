package dyvil.tools.compiler.ast.classes;

import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public abstract class AbstractClass implements IClass, IDefaultContext
{
	// Modifiers and Annotations

	protected AnnotationList annotations;
	protected ModifierSet    modifiers;

	// Signature

	protected Name name;

	protected ITypeParameter[] typeParameters;
	protected int              typeParameterCount;

	protected IParameter[] parameters;
	protected int          parameterCount;

	protected IType superType = Types.OBJECT;
	protected IType[] interfaces;
	protected int     interfaceCount;

	// Body

	protected IClassBody body;
	// Metadata

	protected String fullName;
	protected String internalName;

	protected IClass         enclosingClass;
	protected IClassMetadata metadata;

	protected IClassCompilable[] compilables;
	protected int                compilableCount;

	protected IType thisType;

	protected IType classType = new ClassType(this);

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public abstract IDyvilHeader getHeader();

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public void setType(IType type)
	{
	}

	@Override
	public IType getType()
	{
		return this.thisType;
	}

	@Override
	public IType getClassType()
	{
		return this.classType;
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
		case "dyvil/annotation/Strict":
			this.modifiers.addIntModifier(Modifiers.STRICT);
			return false;
		case Deprecation.DYVIL_INTERNAL:
		case Deprecation.JAVA_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		case "java/lang/FunctionalInterface":
			this.modifiers.addIntModifier(Modifiers.FUNCTIONAL);
			return false;
		}
		return true;
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
	public void setTypeParametric()
	{
		this.typeParameters = new ITypeParameter[2];
	}

	@Override
	public boolean isTypeParametric()
	{
		return this.typeParameters != null;
	}

	@Override
	public int typeParameterCount()
	{
		return this.typeParameterCount;
	}

	@Override
	public void setTypeParameters(ITypeParameter[] typeParameters, int count)
	{
		this.typeParameters = typeParameters;
		this.typeParameterCount = count;
	}

	@Override
	public void setTypeParameter(int index, ITypeParameter typeParameter)
	{
		this.typeParameters[index] = typeParameter;
	}

	@Override
	public void addTypeParameter(ITypeParameter typeParameter)
	{
		if (this.typeParameters == null)
		{
			this.typeParameters = new ITypeParameter[3];
			this.typeParameters[0] = typeParameter;
			this.typeParameterCount = 1;
			return;
		}

		int index = this.typeParameterCount++;
		if (index >= this.typeParameters.length)
		{
			ITypeParameter[] temp = new ITypeParameter[this.typeParameterCount];
			System.arraycopy(this.typeParameters, 0, temp, 0, index);
			this.typeParameters = temp;
		}
		this.typeParameters[index] = typeParameter;

		typeParameter.setIndex(index);
	}

	@Override
	public ITypeParameter[] getTypeParameters()
	{
		return this.typeParameters;
	}

	@Override
	public ITypeParameter getTypeParameter(int index)
	{
		return this.typeParameters[index];
	}

	// Class Parameters

	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}

	@Override
	public IParameter createParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new ClassParameter(position, name, type, modifiers, annotations);
	}

	@Override
	public IParameter getParameter(int index)
	{
		return this.parameters[index];
	}

	@Override
	public void addParameter(IParameter parameter)
	{
		parameter.setEnclosingClass(this);

		if (this.parameters == null)
		{
			parameter.setIndex(0);

			this.parameters = new ClassParameter[2];
			this.parameters[0] = parameter;
			this.parameterCount = 1;
			return;
		}

		final int index = this.parameterCount++;

		parameter.setIndex(index);

		if (this.parameterCount > this.parameters.length)
		{
			IParameter[] temp = new IParameter[this.parameterCount];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = parameter;
	}

	@Override
	public void setParameter(int index, IParameter parameter)
	{
		parameter.setEnclosingClass(this);
		parameter.setIndex(index);
		this.parameters[index] = parameter;
	}

	@Override
	public IParameter[] getParameters()
	{
		return this.parameters;
	}

	@Override
	public void setParameters(IParameter[] parameters, int parameterCount)
	{
		this.parameters = parameters;
		this.parameterCount = parameterCount;
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
	public boolean isSubClassOf(IType type)
	{
		IClass iclass = type.getTheClass();
		if (this == iclass || this.superType != null && Types.isSuperClass(type, this.superType))
		{
			return true;
		}
		if (!iclass.isInterface())
		{
			return false;
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			if (Types.isSuperClass(type, this.interfaces[i]))
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
		if (iclass == null)
		{
			return 0;
		}
		if (this == iclass)
		{
			return 1;
		}

		int max = this.superType != null ? this.superType.getSuperTypeDistance(superType) : 0;
		if (!iclass.isInterface())
		{
			return max;
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			int m = this.interfaces[i].getSuperTypeDistance(superType);
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
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		if (candidate.getEnclosingClass() == this)
		{
			return !candidate.hasModifier(Modifiers.ABSTRACT);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			final IProperty property = this.parameters[i].getProperty();
			if (property != null && ClassBody.checkPropertyImplements(property, candidate, typeContext))
			{
				return true;
			}
		}

		if (this.body != null && this.body.checkImplements(candidate, typeContext))
		{
			return true;
		}

		if (this.superType != null)
		{
			final IClass superClass = this.superType.getTheClass();
			if (superClass != null && superClass
				                          .checkImplements(candidate, this.superType.getConcreteType(typeContext)))
			{
				return true;
			}
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			final IType interfaceType = this.interfaces[i];
			final IClass interfaceClass = interfaceType.getTheClass();
			if (interfaceClass != null && interfaceClass
				                              .checkImplements(candidate, interfaceType.getConcreteType(typeContext)))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext, Set<IClass> checkedClasses)
	{
		if (checkedClasses.contains(this))
		{
			return;
		}
		checkedClasses.add(this);

		for (int i = 0; i < this.parameterCount; i++)
		{
			final IProperty property = this.parameters[i].getProperty();
			if (property != null)
			{
				ClassBody.checkProperty(property, markers, checkedClass, typeContext);
			}
		}

		if (this.body != null)
		{
			this.body.checkMethods(markers, checkedClass, typeContext);
		}

		this.checkSuperMethods(markers, checkedClass, typeContext, checkedClasses);
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
			final IMethod method = this.body.getFunctionalMethod();
			if (method != null)
			{
				return method;
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

		if (this.typeParameterCount > 0)
		{
			buffer.append('<');
			for (int i = 0; i < this.typeParameterCount; i++)
			{
				this.typeParameters[i].appendSignature(buffer);
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
	public IType resolveType(ITypeParameter typeVar, IType concrete)
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
	public byte checkStatic()
	{
		return FALSE;
	}

	@Override
	public IClass getThisClass()
	{
		return this;
	}

	@Override
	public IType getThisType()
	{
		return this.thisType;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.resolveInnerClass(name);
	}

	private IClass resolveInnerClass(Name name)
	{
		if (this.body != null)
		{
			return this.body.getClass(name);
		}
		return null;
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters[i];
			if (typeParameter.getName() == name)
			{
				return typeParameter;
			}
		}

		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			final IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}

		IDataMember field;
		if (this.body != null)
		{
			// Own fields
			field = this.body.getField(name);
			if (field != null)
			{
				return field;
			}
		}

		field = this.metadata.resolveField(name);
		if (field != null)
		{
			return field;
		}

		// Inherited Fields
		if (this.superType != null)
		{
			field = this.superType.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}

		return null;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			final IProperty property = this.parameters[i].getProperty();
			if (property != null)
			{
				property.getMethodMatches(list, instance, name, arguments);
			}
		}

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
	public IType getReturnType()
	{
		return null;
	}

	@Override
	public byte checkException(IType type)
	{
		return FALSE;
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
		if (type == this || Types.isSuperClass(this, type))
		{
			return VariableThis.DEFAULT;
		}
		return null;
	}

	@Override
	public IValue getImplicit()
	{
		return null;
	}

	@Override
	public boolean isMember(IClassMember member)
	{
		return member.getEnclosingClass() == this;
	}

	@Override
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getEnclosingClass();
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

		this.modifiers.toString(buffer);
		ModifierUtil.writeClassType(this.modifiers.toFlags(), buffer);
		buffer.append(this.name);

		if (this.typeParameterCount > 0)
		{
			// TODO Append space if the name is a symbol

			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
			Util.astToString(prefix, this.typeParameters, this.typeParameterCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
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
			this.body.toString(prefix, buffer);
		}
		else if (Formatting.getBoolean("class.semicolon"))
		{
			buffer.append(';');
		}
	}
}
