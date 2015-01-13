package dyvil.tools.compiler.ast.bytecode;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.ClassFormat;

public class InvokeInstruction extends Instruction implements IMethod
{
	private String	owner;
	private String	methodName;
	private String	desc;
	private int		args;
	private IType	type;
	
	public InvokeInstruction(int opcode, String name)
	{
		super(opcode, name);
		if (opcode != Opcodes.INVOKESTATIC)
		{
			args++;
		}
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String)
		{
			if (this.owner == null)
			{
				this.owner = ClassFormat.packageToInternal((String) arg);
				return true;
			}
			else if (this.methodName == null)
			{
				this.methodName = (String) arg;
				return true;
			}
			else if (this.desc == null)
			{
				this.desc = (String) arg;
				ClassFormat.readMethodType(this.desc, this);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		IType type = this.type == Type.VOID ? null : this.type;
		writer.visitMethodInsn(this.opcode, this.owner, this.methodName, this.desc, false, this.args, type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		buffer.append('"').append(this.owner);
		buffer.append("\", \"").append(this.methodName);
		buffer.append("\", \"").append(this.desc).append('"');
	}
	
	// IMethod Interface
	
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
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
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
	public void addVariable(Variable variable)
	{
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
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
	public FieldMatch resolveField(IContext context, String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType[] argumentTypes)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType[] argumentTypes)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void setVarargs()
	{
	}
	
	@Override
	public boolean isVarargs()
	{
		return false;
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
	public void addParameter(Parameter parameter)
	{
		this.args++;
	}
	
	@Override
	public void addParameterType(IType type)
	{
		this.args++;
	}
	
	@Override
	public void checkArguments(List<Marker> markers, IValue instance, List<IValue> arguments)
	{
	}
	
	@Override
	public int getSignatureMatch(String name, IType type, IType... argumentTypes)
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
}
