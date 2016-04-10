package dyvil.tools.repl.context;

import dyvil.collection.Set;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.ASMConstants;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class REPLMemberClass implements IClass
{
	private REPLContext  context;
	private Name         name;
	private IClassMember member;

	public REPLMemberClass(REPLContext context, Name name)
	{
		this.context = context;
		this.name = name;
	}

	@Override
	public ICodePosition getPosition()
	{
		return ICodePosition.ORIGIN;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	public IClassMember getMember()
	{
		return this.member;
	}

	public void setMember(IClassMember member)
	{
		this.member = member;
	}

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
	public void setType(IType type)
	{
	}

	@Override
	public IType getType()
	{
		return new ClassType(this);
	}

	@Override
	public IType getClassType()
	{
		return new ClassType(this);
	}

	@Override
	public int getAccessLevel()
	{
		return 0;
	}

	@Override
	public void setModifiers(ModifierSet modifiers)
	{
	}

	@Override
	public ModifierSet getModifiers()
	{
		return new FlagModifierSet();
	}

	@Override
	public boolean hasModifier(int mod)
	{
		return false;
	}

	@Override
	public AnnotationList getAnnotations()
	{
		return null;
	}

	@Override
	public void setAnnotations(AnnotationList annotations)
	{
	}

	@Override
	public void addAnnotation(IAnnotation annotation)
	{
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return null;
	}

	@Override
	public ElementType getElementType()
	{
		return null;
	}

	@Override
	public void setTypeParametric()
	{
	}

	@Override
	public boolean isTypeParametric()
	{
		return false;
	}

	@Override
	public int typeParameterCount()
	{
		return 0;
	}

	@Override
	public void setTypeParameters(ITypeParameter[] typeParameters, int count)
	{
	}

	@Override
	public void setTypeParameter(int index, ITypeParameter typeParameter)
	{
	}

	@Override
	public void addTypeParameter(ITypeParameter typeParameter)
	{
	}

	@Override
	public ITypeParameter[] getTypeParameters()
	{
		return null;
	}

	@Override
	public ITypeParameter getTypeParameter(int index)
	{
		return null;
	}

	@Override
	public int parameterCount()
	{
		return 0;
	}

	@Override
	public void setParameter(int index, IParameter parameter)
	{
	}

	@Override
	public void addParameter(IParameter parameter)
	{
	}

	@Override
	public IParameter getParameter(int index)
	{
		return null;
	}

	@Override
	public IParameter[] getParameters()
	{
		return null;
	}

	@Override
	public void setParameters(IParameter[] parameters, int parameterCount)
	{
	}

	@Override
	public void setHeader(IDyvilHeader unit)
	{
	}

	@Override
	public IDyvilHeader getHeader()
	{
		return this.context;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
	}

	@Override
	public IClass getEnclosingClass()
	{
		return null;
	}

	@Override
	public boolean isAbstract()
	{
		return false;
	}

	@Override
	public boolean isInterface()
	{
		return false;
	}

	@Override
	public boolean isAnnotation()
	{
		return false;
	}

	@Override
	public boolean isObject()
	{
		return false;
	}

	@Override
	public void setFullName(String name)
	{
	}

	@Override
	public String getFullName()
	{
		return null;
	}

	@Override
	public void setSuperType(IType type)
	{
	}

	@Override
	public IType getSuperType()
	{
		return null;
	}

	@Override
	public boolean isSubClassOf(IType type)
	{
		return false;
	}

	@Override
	public int getSuperTypeDistance(IType superType)
	{
		return 0;
	}

	@Override
	public int interfaceCount()
	{
		return 0;
	}

	@Override
	public void setInterface(int index, IType type)
	{
	}

	@Override
	public void addInterface(IType type)
	{
	}

	@Override
	public IType getInterface(int index)
	{
		return null;
	}

	@Override
	public void setBody(IClassBody body)
	{
	}

	@Override
	public IClassBody getBody()
	{
		return null;
	}

	@Override
	public void setMetadata(IClassMetadata metadata)
	{
	}

	@Override
	public IClassMetadata getMetadata()
	{
		return null;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		return false;
	}

	@Override
	public void checkMethods(MarkerList markers, IClass iclass, ITypeContext typeContext, Set<IClass> checkedClasses)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public byte checkStatic()
	{
		return TRUE;
	}

	@Override
	public IClass getThisClass()
	{
		return this;
	}

	@Override
	public IType getThisType()
	{
		return new ClassType(this);
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		return this.context.resolveTypeAlias(name, arity);
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		return this.context.resolveTypeParameter(name);
	}

	@Override
	public IOperator resolveOperator(Name name, int type)
	{
		return this.context.resolveOperator(name, type);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.context.resolveField(name);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}

	@Override
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getEnclosingClass();
		if (iclass == null || iclass == this || iclass instanceof REPLMemberClass)
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
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}

		return INVISIBLE;
	}

	@Override
	public byte checkException(IType type)
	{
		return TRUE;
	}

	@Override
	public IType getReturnType()
	{
		return null;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return variable == this.member;
	}

	@Override
	public IDataMember capture(IVariable capture)
	{
		return capture;
	}

	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		return null;
	}

	@Override
	public IValue getImplicit()
	{
		return null;
	}

	@Override
	public IType resolveType(ITypeParameter typeVar, IType concrete)
	{
		return null;
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public IDataMember getSuperField(Name name)
	{
		return null;
	}

	@Override
	public boolean isMember(IClassMember member)
	{
		return false;
	}

	@Override
	public int compilableCount()
	{
		return this.context.innerClassCount();
	}

	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		this.context.addInnerClass(compilable);
	}

	@Override
	public IClassCompilable getCompilable(int index)
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.name.qualified;
	}

	@Override
	public String getSignature()
	{
		return 'L' + this.name.qualified + ';';
	}

	@Override
	public String[] getInterfaceArray()
	{
		return null;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String name = this.name.qualified;
		writer.visit(ClassFormat.CLASS_VERSION, Modifiers.PUBLIC | ASMConstants.ACC_SUPER, name, null, "java/lang/Object",
		             null);
		writer.visitSource(name, null);

		this.member.write(writer);

		for (IClassCompilable c : this.context.compilableList)
		{
			c.write(writer);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{

	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{

	}

	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
