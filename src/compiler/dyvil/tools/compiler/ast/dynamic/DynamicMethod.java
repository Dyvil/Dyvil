package dyvil.tools.compiler.ast.dynamic;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;

import dyvil.array.ObjectArray;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class DynamicMethod extends ASTNode implements IMethod
{
	public static final Handle	BOOTSTRAP	= new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/dyn/DynamicLinker", "linkMethod",
													"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
	
	public Name					name;
	
	public DynamicMethod(Name name)
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
		return Types.DYNAMIC;
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
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		return IContext.VISIBLE;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
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
	
	// Name
	
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
	
	// Modifiers
	
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
	public Annotation getAnnotation(IClass type)
	{
		return null;
	}
	
	@Override
	public int parameterCount()
	{
		return 0;
	}
	
	@Override
	public void setParameters(IParameter[] parameters, int parameterCount)
	{
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
	public MethodParameter getParameter(int index)
	{
		return null;
	}
	
	@Override
	public IParameter[] getParameters()
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
	public int getSignatureMatch(Name name, IValue instance, IArguments arguments)
	{
		return 0;
	}
	
	@Override
	public IValue checkArguments(MarkerList markers, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		return instance;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public GenericData getGenericData(GenericData data, IValue instance, IArguments arguments)
	{
		return null;
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
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type) throws BytecodeException
	{
		StringBuilder desc = new StringBuilder();
		desc.append('(');
		
		if (instance != null && instance.valueTag() != IValue.CLASS_ACCESS)
		{
			instance.writeExpression(writer);
			instance.getType().appendExtendedName(desc);
		}
		
		for (IValue v : arguments)
		{
			v.writeExpression(writer);
			v.getType().appendExtendedName(desc);
		}
		desc.append(')');
		desc.append("Ljava/lang/Object;");
		
		writer.writeInvokeDynamic(this.name.qualified, desc.toString(), BOOTSTRAP, ObjectArray.EMPTY);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
