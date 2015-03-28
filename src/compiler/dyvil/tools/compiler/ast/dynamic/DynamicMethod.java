package dyvil.tools.compiler.ast.dynamic;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;

import dyvil.arrays.ArrayUtils;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class DynamicMethod extends ASTNode implements IMethod
{
	public static final Handle	BOOTSTRAP	= new Handle(MethodWriter.H_INVOKESTATIC, "dyvil/dyn/DynamicLinker", "linkMethod",
													"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
	
	public String				name;
	
	public DynamicMethod(String name)
	{
		this.name = name;
	}
	
	@Override
	public void setType(IType type)
	{
	}
	
	@Override
	public IType getType()
	{
		return Type.DYNAMIC;
	}
	
	@Override
	public int typeCount()
	{
		return 0;
	}
	
	@Override
	public void setType(int index, IType type)
	{
	}
	
	@Override
	public void addType(IType type)
	{
	}
	
	@Override
	public IType getType(int index)
	{
		return null;
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
	public void setTypeVariable(int index, ITypeVariable var)
	{
	}
	
	@Override
	public void addTypeVariable(ITypeVariable var)
	{
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		return null;
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return null;
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return IContext.READ_ACCESS;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return null;
	}
	
	@Override
	public IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public int getAccessLevel()
	{
		return 0;
	}
	
	@Override
	public byte getAccessibility()
	{
		return 0;
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
	public void setName(String name, String qualifiedName)
	{
		this.name = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
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
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.name.equals(name);
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
	public Annotation getAnnotation(int index)
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public int parameterCount()
	{
		return 0;
	}
	
	@Override
	public void setParameters(Parameter[] parameters, int parameterCount)
	{
	}
	
	@Override
	public void setParameter(int index, Parameter param)
	{
	}
	
	@Override
	public void addParameter(Parameter param)
	{
	}
	
	@Override
	public Parameter getParameter(int index)
	{
		return null;
	}
	
	@Override
	public void exceptionCount()
	{
	}
	
	@Override
	public void setException(int index, IType exception)
	{
	}
	
	@Override
	public void addException(IType exception)
	{
	}
	
	@Override
	public IType getException(int index)
	{
		return null;
	}
	
	@Override
	public int getSignatureMatch(String name, IValue instance, IArguments arguments)
	{
		return 0;
	}
	
	@Override
	public void checkArguments(MarkerList markers, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType resolveType(String name, IValue instance, IArguments arguments, ITypeList generics)
	{
		return Type.DYNAMIC;
	}
	
	@Override
	public boolean isIntrinsic()
	{
		return false;
	}
	
	@Override
	public String getDescriptor()
	{
		return null;
	}
	
	@Override
	public String getSignature()
	{
		return null;
	}
	
	@Override
	public String[] getExceptions()
	{
		return null;
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type)
	{
		int len = arguments.size();
		StringBuilder desc = new StringBuilder();
		desc.append('(');
		
		if (instance != null && instance.getValueType() != IValue.CLASS_ACCESS)
		{
			instance.writeExpression(writer);
			instance.getType().appendExtendedName(desc);
			len++;
		}
		
		for (IValue v : arguments)
		{
			v.writeExpression(writer);
			v.getType().appendExtendedName(desc);
		}
		desc.append(')');
		desc.append("Ljava/lang/Object;");
		
		writer.push((Object) null);
		writer.pop();
		writer.writeInvokeDynamic(this.name, desc.toString(), len, Type.DYNAMIC, BOOTSTRAP, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
