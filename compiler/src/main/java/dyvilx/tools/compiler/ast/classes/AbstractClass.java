package dyvilx.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.external.ExternalClass;
import dyvilx.tools.compiler.ast.field.*;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.generic.ClassGenericType;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.ast.type.typevar.TypeVarType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractClass implements IClass, IDefaultContext
{
	// =============== Fields ===============

	protected @NonNull AttributeList attributes;

	protected @Nullable Name name;

	protected @NonNull TypeParameterList typeParameters = new TypeParameterList();

	protected @NonNull ParameterList parameters = new ParameterList();

	protected @Nullable IType    superType  = Types.OBJECT;
	protected @NonNull  TypeList interfaces = new TypeList();

	protected @Nullable ClassBody body;

	// --------------- Metadata ---------------

	protected Package     enclosingPackage;
	protected IHeaderUnit enclosingHeader;
	protected IClass      enclosingClass;

	protected IClassMetadata metadata;

	protected ClassCompilable[] compilables;
	protected int               compilableCount;

	// --------------- Cache ---------------

	protected String fullName;
	protected String internalName;
	protected String internalSimpleName;

	protected IType thisType;

	protected IType classType = new ClassType(this);

	// =============== Constructors ===============

	public AbstractClass()
	{
		this.attributes = new AttributeList();
	}

	public AbstractClass(@NonNull AttributeList attributes)
	{
		this.attributes = attributes;
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
		this.enclosingPackage = enclosingClass.getPackage();
		this.enclosingHeader = enclosingClass.getHeader();
	}

	@Override
	public Package getPackage()
	{
		return this.enclosingPackage;
	}

	@Override
	public void setPackage(Package pack)
	{
		this.enclosingPackage = pack;
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this.enclosingHeader;
	}

	@Override
	public void setHeader(IHeaderUnit header)
	{
		this.enclosingHeader = header;
		this.enclosingPackage = header.getPackage();
	}

	@Override
	public IType getClassType()
	{
		return this.classType;
	}

	// --------------- Attributes ---------------

	@Override
	public MemberKind getKind()
	{
		return this.getMetadata().getKind();
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
	public AttributeList getAttributes()
	{
		return this.attributes;
	}

	@Override
	public void setAttributes(AttributeList attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public boolean skipAnnotation(String type, Annotation annotation)
	{
		switch (type)
		{
		case ModifierUtil.STRICTFP_INTERNAL:
			this.attributes.addFlag(Modifiers.STRICT);
			return true;
		case Deprecation.DYVIL_INTERNAL:
		case Deprecation.JAVA_INTERNAL:
			this.attributes.addFlag(Modifiers.DEPRECATED);
			return false;
		case AnnotationUtil.JAVA_NAME_INTERNAL:
			if (annotation == null)
			{
				return false;
			}

			final IValue firstValue = annotation.getArguments().getFirst();
			if (firstValue != null)
			{
				// In Dyvil source code, the @DyvilName is called @BytecodeName,
				// and it sets the name to be used in the bytecode
				this.setInternalSimpleName(firstValue.stringValue());
			}
			return true;
		}
		return false;
	}

	// --------------- Name ---------------

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	// --------------- Type Parameters ---------------

	@Override
	public boolean isTypeParametric()
	{
		return !this.getTypeParameters().isEmpty();
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		return this.typeParameters;
	}

	@Override
	public void setTypeParameters(TypeParameterList typeParameters)
	{
		this.typeParameters = typeParameters;
	}

	// --------------- Class Parameters ---------------

	@Override
	public ParameterList getParameters()
	{
		return this.parameters;
	}

	@Override
	public IParameter createParameter(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new ClassParameter(this, position, name, type, attributes);
	}

	// --------------- Super Type ---------------

	@Override
	public IType getSuperType()
	{
		return this.superType;
	}

	@Override
	public void setSuperType(IType type)
	{
		this.superType = type;
	}

	@Override
	public boolean isSubClassOf(IType type)
	{
		IClass iclass = type.getTheClass();
		if (this == iclass)
		{
			return true;
		}

		final IType superType = this.getSuperType();
		if (superType != null && Types.isSuperClass(type, superType))
		{
			return true;
		}

		if (!iclass.isInterface())
		{
			return false;
		}
		for (IType interfaceType : this.getInterfaces())
		{
			if (Types.isSuperClass(type, interfaceType))
			{
				return true;
			}
		}
		return false;
	}

	// --------------- Interfaces ---------------

	@Override
	public TypeList getInterfaces()
	{
		return this.interfaces;
	}

	public void setInterfaces(TypeList interfaces)
	{
		this.interfaces = interfaces;
	}

	// --------------- Body ---------------

	@Override
	public ClassBody getBody()
	{
		return this.body;
	}

	@Override
	public ClassBody createBody()
	{
		if (this.body != null)
		{
			return this.body;
		}
		return this.body = new ClassBody(this);
	}

	@Override
	public void setBody(ClassBody body)
	{
		this.body = body;
	}

	// --------------- Class Compilables ---------------

	@Override
	public int classCompilableCount()
	{
		return this.compilableCount;
	}

	@Override
	public void addClassCompilable(ClassCompilable compilable)
	{
		if (this.compilables == null)
		{
			this.compilables = new ClassCompilable[2];
			this.compilables[0] = compilable;
			this.compilableCount = 1;
			return;
		}

		int index = this.compilableCount++;
		if (this.compilableCount > this.compilables.length)
		{
			ClassCompilable[] temp = new ClassCompilable[this.compilableCount];
			System.arraycopy(this.compilables, 0, temp, 0, index);
			this.compilables = temp;
		}
		this.compilables[index] = compilable;
	}

	// --------------- Metadata ---------------

	@Override
	public IClassMetadata getMetadata()
	{
		return this.metadata;
	}

	@Override
	public void setMetadata(IClassMetadata metadata)
	{
		this.metadata = metadata;
	}

	// --------------- Methods ---------------

	@Override
	public Collection<IMethod> allMethods()
	{
		final Collection<IMethod> bodyMethods = this.body != null ? this.body.allMethods() : Collections.emptyList();
		final int parameters = this.parameters.size();

		final Collection<IMethod> result = new ArrayList<>(parameters * 2 + bodyMethods.size());

		for (int i = 0; i < parameters; i++)
		{
			final IProperty property = this.parameters.get(i).getProperty();
			if (property != null)
			{
				ClassBody.addPropertyMethods(result, property);
			}
		}

		result.addAll(bodyMethods);

		return result;
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		if (candidate.getEnclosingClass() == this)
		{
			return !candidate.hasModifier(Modifiers.ABSTRACT);
		}

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IProperty property = this.parameters.get(i).getProperty();
			if (property != null && ClassBody.checkPropertyImplements(property, candidate, typeContext))
			{
				return true;
			}
		}

		if (this.body != null && this.body.checkImplements(candidate, typeContext))
		{
			// TODO perhaps this should continue going through the super types and interfaces, to collect ALL overriden methods?
			return true;
		}

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			final IClass superClass = superType.getTheClass();
			if (superClass != null && superClass.checkImplements(candidate, superType.getConcreteType(typeContext)))
			{
				return true;
			}
		}

		for (IType interfaceType : this.getInterfaces())
		{
			final IClass interfaceClass = interfaceType.getTheClass();
			if (interfaceClass != null && interfaceClass.checkImplements(candidate, interfaceType.getConcreteType(
				typeContext)))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext,
		Set<IClass> checkedClasses)
	{
		if (!checkedClasses.add(this))
		{
			// was already checked
			return;
		}

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IProperty property = this.parameters.get(i).getProperty();
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

	public void checkSuperMethods(MarkerList markers, IClass thisClass, ITypeContext typeContext,
		Set<IClass> checkedClasses)
	{
		final IType superType = this.getSuperType();
		if (superType != null)
		{
			final IClass superClass = superType.getTheClass();
			if (superClass != null)
			{
				superClass.checkMethods(markers, thisClass, superType.getConcreteType(typeContext), checkedClasses);
			}
		}

		for (IType type : this.getInterfaces())
		{
			final IClass iClass = type.getTheClass();
			if (iClass != null)
			{
				iClass.checkMethods(markers, thisClass, type.getConcreteType(typeContext), checkedClasses);
			}
		}
	}

	// --------------- Context ---------------

	@Override
	public IType resolveType(ITypeParameter typeParameter, IType concrete)
	{
		final IType superType = this.getSuperType();
		if (superType != null)
		{
			final IType type = superType.resolveType(typeParameter);
			if (type != null)
			{
				return type.getConcreteType(concrete);
			}
		}

		for (IType interfaceType : this.getInterfaces())
		{
			final IType type = interfaceType.resolveType(typeParameter);
			if (type != null)
			{
				return type.getConcreteType(concrete);
			}
		}
		return null;
	}

	@Override
	public boolean isThisAvailable()
	{
		// not by default; only in non-static members
		return false;
	}

	@Override
	public IClass getThisClass()
	{
		return this;
	}

	@Override
	public IType getThisType()
	{
		if (this.thisType != null)
		{
			return this.thisType;
		}

		final TypeParameterList typeParams = this.getTypeParameters();
		if (typeParams.isEmpty())
		{
			return this.thisType = this.classType;
		}

		final ClassGenericType type = new ClassGenericType(this);
		final TypeList arguments = type.getArguments();
		for (ITypeParameter typeParameter : typeParams)
		{
			arguments.add(new TypeVarType(typeParameter));
		}
		return this.thisType = type;
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
		return this.body != null ? this.body.getClasses().get(name) : null;
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		return this.getTypeParameters().get(name);
	}

	@Override
	public IContext getTypeParameterContext()
	{
		return this.enclosingClass == null || this.isStatic() ?
			       this :
			       new CombiningContext(this, this.enclosingClass.getTypeParameterContext());
	}

	protected IParameter resolveClassParameter(Name name)
	{
		final IParameter parameter = this.parameters.get(name);
		// do not expose override class parameters
		return parameter == null || parameter.hasModifier(Modifiers.OVERRIDE) ? null : parameter;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		final IParameter parameter = this.resolveClassParameter(name);
		if (parameter != null)
		{
			return parameter;
		}

		final IDataMember bodyField;
		if (this.body != null && (bodyField = this.body.getField(name)) != null)
		{
			return bodyField;
		}

		final IType superType = this.getSuperType();
		final IDataMember superField;
		if (superType != null && (superField = superType.resolveField(name)) != null)
		{
			return superField;
		}

		return null;
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		if (type == null)
		{
			return null;
		}

		// for implicit objects

		IParameter candidate = null;
		for (IParameter param : this.parameters)
		{
			if (!param.isImplicit() || !Types.isSuperType(type, param.getType()))
			{
				continue;
			}
			if (candidate != null)
			{
				return null; // ambiguous -> pick none
			}
			candidate = param;
		}
		if (candidate != null)
		{
			return new FieldAccess(candidate);
		}

		return this.body == null ? null : this.body.resolveImplicit(type);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IProperty property = this.parameters.get(i).getProperty();
			if (property != null)
			{
				property.checkMatch(list, receiver, name, arguments);
			}
		}

		if (this.body != null)
		{
			this.body.getMethodMatches(list, receiver, name, arguments);
		}

		if (list.hasCandidate())
		{
			return;
		}

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.getMethodMatches(list, receiver, name, arguments);
		}

		if (list.hasCandidate())
		{
			return;
		}

		for (IType type : this.getInterfaces())
		{
			type.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.body != null)
		{
			this.body.getImplicitMatches(list, value, targetType);
		}

		// TODO look into super types?
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		if (this.body != null)
		{
			this.body.getConstructorMatches(list, arguments);
		}
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
		return this.parameters.isParameter(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		return variable;
	}

	@Override
	public IAccessible getAccessibleThis(IType type)
	{
		return Types.isSuperType(type, this.getThisType()) ? VariableThis.DEFAULT : null;
	}

	@Override
	public boolean isMember(ClassMember member)
	{
		return member.getEnclosingClass() == this;
	}

	@Override
	public byte getVisibility(ClassMember member)
	{
		if (member == this)
		{
			return VISIBLE;
		}

		final IClass enclosingClass = member.getEnclosingClass();
		if (enclosingClass == this)
		{
			return VISIBLE;
		}

		int level = member.getAccessLevel();
		if ((level & Modifiers.INTERNAL) != 0)
		{
			if (enclosingClass instanceof ExternalClass)
			{
				return INTERNAL;
			}
			level &= ~Modifiers.INTERNAL;
		}

		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.PRIVATE_PROTECTED)
		{
			if (enclosingClass != null && Types.isSuperClass(enclosingClass, this))
			{
				// The enclosing class of the member is a super class of this
				return VISIBLE;
			}
		}
		if (level == Modifiers.PROTECTED || level == Modifiers.PACKAGE)
		{
			final Package thatPackage = enclosingClass != null ?
				                            enclosingClass.getPackage() :
				                            member instanceof IClass ? ((IClass) member).getPackage() : null;
			if (this.getPackage() == thatPackage)
			{
				// The two units are in the same package
				return VISIBLE;
			}
		}

		return INVISIBLE;
	}

	// --------------- Compilation ---------------

	// - - - - - - - - Internal Name - - - - - - - -

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}

		if (this.enclosingClass != null)
		{
			return this.internalName = this.enclosingClass.getInternalName() + '$' + this.getInternalSimpleName();
		}
		if (this.enclosingPackage != null && this.enclosingPackage != Package.rootPackage)
		{
			return this.enclosingPackage.getInternalName() + '/' + this.getInternalSimpleName();
		}
		return this.getInternalSimpleName();
	}

	public void setInternalName(String internalName)
	{
		this.internalName = internalName;
	}

	public String getInternalSimpleName()
	{
		if (this.internalSimpleName != null)
		{
			return this.internalSimpleName;
		}
		return this.internalSimpleName = this.name.qualified;
	}

	public void setInternalSimpleName(String internalSimpleName)
	{
		this.internalSimpleName = internalSimpleName;
		this.internalName = null;
	}

	// - - - - - - - - Full Name - - - - - - - -

	@Override
	public String getFullName()
	{
		if (this.fullName != null)
		{
			return this.fullName;
		}

		if (this.enclosingClass != null)
		{
			return this.enclosingClass.getFullName() + '.' + this.name.qualified;
		}
		if (this.enclosingPackage != null)
		{
			return this.fullName = this.enclosingPackage.getFullName() + "." + this.name.qualified;
		}

		return this.name.unqualified;
	}

	@Override
	public void setFullName(String name)
	{
		this.fullName = name;
	}

	// - - - - - - - - File Name - - - - - - - -

	@Override
	public String getFileName()
	{
		final String internalName = this.getInternalName();
		final int slashIndex = internalName.lastIndexOf('/');

		if (slashIndex < 0)
		{
			return internalName + DyvilFileType.CLASS_EXTENSION;
		}
		// Strip the package/ part
		return internalName.substring(slashIndex + 1) + DyvilFileType.CLASS_EXTENSION;
	}

	// - - - - - - - - Inner Class Info - - - - - - - -

	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
		if (this.enclosingClass != null)
		{
			final int modifiers = (int) (this.getAttributes().flags() & Modifiers.JAVA_MODIFIER_MASK);
			final String outerName = this.enclosingClass.getInternalName();
			writer.visitInnerClass(this.getInternalName(), outerName, this.name.qualified, modifiers);
		}
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributes.toString(indent, buffer);

		ModifierUtil.writeClassType(this.attributes.flags(), buffer);
		buffer.append(this.name);

		this.typeParameters.toString(indent, buffer);

		final AttributeList constructorAttributes = this.getConstructorAttributes();
		if (constructorAttributes != null && !constructorAttributes.isEmpty())
		{
			buffer.append(' ');
			constructorAttributes.toInlineString(indent, buffer);

			if (this.parameters.isEmpty())
			{
				// when there are constructor modifiers but no parameters, we still display the ()
				buffer.append("()");
			}
		}

		if (!this.parameters.isEmpty())
		{
			this.parameters.toString(indent, buffer);
		}

		final IType superType = this.getSuperType();
		if (superType != null && superType.getTheClass() != Types.OBJECT_CLASS)
		{
			String extendsPrefix = indent;
			if (Formatting.getBoolean("class.extends.newline"))
			{
				extendsPrefix = Formatting.getIndent("class.extends.indent", extendsPrefix);
				buffer.append('\n').append(extendsPrefix).append("extends ");
			}
			else
			{
				buffer.append(" extends ");
			}

			superType.toString("", buffer);
		}

		if (!this.interfaces.isEmpty())
		{
			final String itfIndent;
			if (Formatting.getBoolean("class.implements.newline"))
			{
				itfIndent = Formatting.getIndent("class.implements.indent", indent);
				buffer.append('\n').append(itfIndent);
			}
			else
			{
				itfIndent = indent;
				buffer.append(' ');
			}

			buffer.append(this.isInterface() ? "extends " : "implements ");

			Util.astToString(itfIndent, this.interfaces.getTypes(), this.interfaces.size(),
			                 Formatting.getSeparator("class.implements.separator", ','), buffer);
		}

		if (this.body != null)
		{
			this.body.toString(indent, buffer);
		}
		else if (Formatting.getBoolean("class.semicolon"))
		{
			buffer.append(';');
		}
	}
}
