package dyvil.tools.compiler.ast.dynamic;

import java.util.List;

import jdk.internal.org.objectweb.asm.*;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public class DynamicMethod extends ASTNode implements IMethod
{
	public static final Handle	BOOTSTRAP	= new Handle(Opcodes.H_INVOKEVIRTUAL, "dyvil/dyn/DynamicLinker", "linkMethod",
													"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType)Ljava/lang/invoke/CallSite;");
	
	public String				name;
	
	public DynamicMethod(String name, List<IValue> arguments)
	{
		
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
	}
	
	@Override
	public void setName(String name)
	{
	}
	
	@Override
	public String getName()
	{
		return null;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return null;
	}
	
	@Override
	public boolean isName(String name)
	{
		return false;
	}
	
	@Override
	public void setType(IType type)
	{
	}
	
	@Override
	public IType getType()
	{
		return null;
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
	public void setAnnotations(List<Annotation> annotations)
	{
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
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
	public void setTypes(List<IType> types)
	{
	}
	
	@Override
	public List<IType> getTypes()
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
	public void setThrows(List<IType> throwsDecls)
	{
	}
	
	@Override
	public List<IType> getThrows()
	{
		return null;
	}
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
	}
	
	@Override
	public List<Parameter> getParameters()
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
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void checkArguments(List<Marker> markers, IValue instance, List<IValue> arguments)
	{
	}
	
	@Override
	public int getSignatureMatch(String name, IValue instance, List<IValue> arguments)
	{
		return 0;
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
	public void write(ClassWriter writer)
	{
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, List<IValue> arguments)
	{
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, List<IValue> arguments)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}
