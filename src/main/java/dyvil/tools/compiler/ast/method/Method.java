package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.*;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IMethod;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	private String			openBracket			= "(";
	private List<Parameter>	parameters			= new ArrayList(3);
	private String			closeBracket		= ")";
	
	private List<Type>		throwsDeclarations	= new ArrayList(1);
	
	private IValue			statement;
	
	private List<Variable>	variables			= new ArrayList(5);
	
	protected boolean		isConstructor;
	
	public Method(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void setName(String name)
	{
		if (name.equals("new"))
		{
			this.qualifiedName = "<init>";
			this.name = "new";
			this.isConstructor = true;
		}
		else
		{
			this.qualifiedName = Symbols.expand(name);
			this.name = name;
		}
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		if (name.equals("<init>"))
		{
			this.qualifiedName = "<init>";
			this.name = "new";
			this.isConstructor = true;
		}
		else
		{
			this.qualifiedName = name;
			this.name = Symbols.contract(name);
		}
	}
	
	@Override
	public void setParametersOpenBracket(String bracket)
	{
		this.openBracket = bracket;
	}
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public void addParameter(Parameter parameter)
	{
		this.parameters.add(parameter);
	}
	
	@Override
	public void addVariable(Variable variable)
	{
		this.variables.add(variable);
	}
	
	@Override
	public void setParametersCloseBracket(String bracket)
	{
		this.closeBracket = bracket;
	}
	
	@Override
	public int getSignatureMatch(String name, Type type, Type... argumentTypes)
	{
		if (name.equals(this.qualifiedName))
		{
			if (argumentTypes == null)
			{
				return 1;
			}
			
			int pOff = 0;
			int match = 1;
			int len = argumentTypes.length;
			List<Parameter> parameters = this.parameters;
			
			if (type != null && (this.modifiers & Modifiers.IMPLICIT) != 0)
			{
				if (len != parameters.size() - 1)
				{
					return 0;
				}
				
				Type t2 = parameters.get(0).type;
				if (type.equals(t2))
				{
					match += 2;
				}
				else if (Type.isSuperType(type, t2))
				{
					match += 1;
				}
				else
				{
					return 0;
				}
				
				pOff = 1;
			}
			else if (len != this.parameters.size())
			{
				return 0;
			}
			
			for (int i = 0; i < len; i++)
			{
				Type t1 = parameters.get(i + pOff).type;
				Type t2 = argumentTypes[i];
				
				if (t1.equals(t2))
				{
					match += 2;
				}
				else if (Type.isSuperType(t1, t2))
				{
					match += 1;
				}
				else
				{
					return 0;
				}
			}
			return match;
		}
		return 0;
	}
	
	@Override
	public void setThrows(List<Type> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public List<Type> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public void addThrows(Type throwsDecl)
	{
		this.throwsDeclarations.add(throwsDecl);
	}
	
	@Override
	public void setValue(IValue statement)
	{
		this.statement = statement;
	}
	
	@Override
	public IValue getValue()
	{
		return this.statement;
	}
	
	@Override
	public String getDescriptor()
	{
		StringBuilder buf = new StringBuilder();
		buf.append('(');
		for (Parameter par : this.parameters)
		{
			buf.append(par.type.getExtendedName());
		}
		buf.append(')');
		buf.append(this.type.getExtendedName());
		return buf.toString();
	}
	
	@Override
	public String getSignature()
	{
		// TODO Generic Signature
		return null;
	}
	
	@Override
	public String[] getExceptions()
	{
		int len = this.throwsDeclarations.size();
		if (len == 0)
		{
			return null;
		}
		
		String[] array = new String[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = this.throwsDeclarations.get(i).getInternalName();
		}
		return array;
	}
	
	@Override
	public Method applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.applyState(state, context);
			this.throwsDeclarations.replaceAll(t -> t.applyState(state, context));
		}
		
		for (Parameter p : this.parameters)
		{
			p.applyState(state, context);
		}
		
		if (this.statement != null)
		{
			this.statement = this.statement.applyState(state, this);
		}
		
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.hasModifier(Modifiers.STATIC);
	}
	
	@Override
	public Type getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		// TODO Variables
		
		for (Parameter param : this.parameters)
		{
			if (param.name.equals(name))
			{
				return new FieldMatch(param, 1);
			}
		}
		
		return this.theClass.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, Type... argumentTypes)
	{
		return this.theClass.resolveMethod(returnType, name, argumentTypes);
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		MethodVisitor visitor = writer.visitMethod(this.modifiers & 0xFFFF, this.qualifiedName, this.getDescriptor(), this.getSignature(), this.getExceptions());
		MethodWriter mw = new MethodWriter(Opcodes.ASM5, visitor);
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.IMPLICIT) == Modifiers.IMPLICIT)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/implicit;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.visitAnnotation("Ldyvil/lang/annotation/prefix;", false);
		}
		
		int index = this.hasModifier(Modifiers.STATIC) ? 0 : 1;
		for (Parameter param : this.parameters)
		{
			param.index = index++;
			mw.visitParameter(param.name, param.type, index);
			
			if (param.hasModifier(Modifiers.BYREF))
			{
				mw.visitParameterAnnotation(index, "Ldyvil/lang/annotation/ByRef;", true);
			}
		}
		
		if (this.statement != null)
		{
			mw.visitCode();
			
			if (this.statement != null)
			{
				this.statement.writeExpression(mw);
			}
			
			for (Variable var : this.variables)
			{
				String name = var.qualifiedName;
				mw.visitLocalVariable(name, var.type, var.start, var.end, var.index);
			}
			
			mw.visitEnd(this.isConstructor ? Type.VOID : this.type);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Modifiers.METHOD.toString(this.modifiers));
		if (this.type != null)
		{
			this.type.toString("", buffer);
			buffer.append(' ');
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		Util.parametersToString(this.parameters, buffer, true, this.openBracket + this.closeBracket, this.openBracket, Formatting.Method.parameterSeperator, this.closeBracket);
		
		if (!this.throwsDeclarations.isEmpty())
		{
			buffer.append(Formatting.Method.signatureThrowsSeperator);
			Util.astToString(this.throwsDeclarations, Formatting.Method.throwsSeperator, buffer);
		}
		
		IValue statement = this.getValue();
		if (statement != null)
		{
			buffer.append(Formatting.Method.signatureBodySeperator);
			statement.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
