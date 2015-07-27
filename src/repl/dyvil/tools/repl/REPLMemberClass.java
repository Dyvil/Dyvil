package dyvil.tools.repl;

import java.lang.annotation.ElementType;
import java.security.ProtectionDomain;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class REPLMemberClass implements IClass
{
	private static final ClassLoader		CLASS_LOADER		= REPLVariable.class.getClassLoader();
	private static final ProtectionDomain	PROTECTION_DOMAIN	= REPLVariable.class.getProtectionDomain();
	
	private Name			name;
	private IClassMember	member;
	
	public REPLMemberClass(Name name, IClassMember member)
	{
		this.name = name;
		this.member = member;
	}
	
	public void setMember(IClassMember member)
	{
		this.member = member;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return ICodePosition.ORIGIN;
	}
	
	@Override
	public int getAccessLevel()
	{
		return 0;
	}
	
	@Override
	public void setName(Name name)
	{
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
	public void setModifiers(int modifiers)
	{
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		return false;
	}
	
	@Override
	public void removeModifier(int mod)
	{
	}
	
	@Override
	public int getModifiers()
	{
		return 0;
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return false;
	}
	
	@Override
	public int annotationCount()
	{
		return 0;
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
	}
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
	}
	
	@Override
	public void removeAnnotation(int index)
	{
	}
	
	@Override
	public Annotation[] getAnnotations()
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		return null;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.TYPE;
	}
	
	@Override
	public void setGeneric()
	{
	}
	
	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public int genericCount()
	{
		return 0;
	}
	
	@Override
	public void setTypeVariables(ITypeVariable[] typeVars, int count)
	{
	}
	
	@Override
	public void setTypeVariable(int index, ITypeVariable var)
	{
	}
	
	@Override
	public void addTypeVariable(ITypeVariable var)
	{
	}
	
	@Override
	public ITypeVariable[] getTypeVariables()
	{
		return null;
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		return null;
	}
	
	@Override
	public int parameterCount()
	{
		return 0;
	}
	
	@Override
	public void setParameter(int index, IParameter param)
	{
	}
	
	@Override
	public void addParameter(IParameter param)
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
	public void setHeader(IDyvilHeader unit)
	{
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return DyvilREPL.context;
	}
	
	@Override
	public void setOuterClass(IClass iclass)
	{
	}
	
	@Override
	public IClass getOuterClass()
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
	public boolean isSubTypeOf(IType type)
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
	public IClass getThisClass()
	{
		return this;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return DyvilREPL.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return DyvilREPL.context.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return DyvilREPL.context.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return DyvilREPL.context.resolveTypeVariable(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return DyvilREPL.context.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		DyvilREPL.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null || iclass == this || iclass instanceof REPLMemberClass)
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
		
		return INVISIBLE;
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return true;
	}
	
	@Override
	public IVariable capture(IVariable variable)
	{
		return null;
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		return null;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		return null;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount, IType concrete)
	{
		return null;
	}
	
	@Override
	public IMethod getSuperMethod(Name name, IParameter[] parameters, int parameterCount)
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
		return DyvilREPL.context.innerClassCount();
	}
	
	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		DyvilREPL.context.addInnerClass(compilable);
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
	
	public static Class compile(IClass iclass)
	{
		try
		{
			ClassWriter cw = new ClassWriter(DyvilCompiler.asmVersion);
			iclass.write(cw);
			cw.visitEnd();
			byte[] bytes = cw.toByteArray();
			return loadClass(iclass.getName().qualified, bytes);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return null;
		}
	}
	
	protected static Class loadClass(String name, byte[] bytes)
	{
		return ReflectUtils.unsafe.defineClass(name, bytes, 0, bytes.length, CLASS_LOADER, PROTECTION_DOMAIN);
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String name = this.name.qualified;
		writer.visit(DyvilCompiler.classVersion, Modifiers.PUBLIC | Opcodes.ACC_SUPER, name, null, "java/lang/Object", null);
		writer.visitSource(name, null);
		
		this.member.write(writer);
		
		for (IClassCompilable c : REPLContext.compilableList)
		{
			c.write(writer);
		}
	}
	
	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
