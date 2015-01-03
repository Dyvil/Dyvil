package dyvil.tools.compiler.ast.method;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.api.IMethod;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	private List<Parameter>	parameters			= new ArrayList(3);
	private List<IType>		throwsDeclarations	= new ArrayList(1);
	
	private IValue			statement;
	
	private List<Variable>	variables			= new ArrayList(5);
	
	protected boolean		isConstructor;
	
	protected IMethod		overrideMethod;
	
	public Method(IClass iclass)
	{
		super(iclass);
	}
	
	public Method(IClass iclass, String name)
	{
		super(iclass, name);
	}
	
	public Method(IClass iclass, String name, IType type)
	{
		super(iclass, name, type);
	}
	
	public Method(IClass iclass, String name, IType type, int modifiers, List<Annotation> annotations)
	{
		super(iclass, name, type, modifiers, annotations);
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
	public void addAnnotation(Annotation annotation)
	{
		if (!this.processAnnotation(annotation))
		{
			annotation.target = ElementType.METHOD;
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.qualifiedName;
		if ("dyvil.lang.annotation.inline".equals(name))
		{
			this.modifiers |= Modifiers.INLINE;
			return true;
		}
		if ("dyvil.lang.annotation.implicit".equals(name))
		{
			this.modifiers |= Modifiers.IMPLICIT;
			return true;
		}
		if ("dyvil.lang.annotation.prefix".equals(name))
		{
			this.modifiers |= Modifiers.PREFIX;
			return true;
		}
		if ("java.lang.Override".equals(name))
		{
			this.modifiers |= Modifiers.OVERRIDE;
			return true;
		}
		return false;
	}
	
	@Override
	public void checkArguments(CompilerState state, IValue instance, List<IValue> arguments)
	{
		int pOff = 0;
		int len = arguments.size();
		List<Parameter> params = this.parameters;
		Parameter par;
		IType parType;
		
		if (instance != null && (this.modifiers & Modifiers.IMPLICIT) == Modifiers.IMPLICIT)
		{
			par = params.get(0);
			parType = par.type;
			if (!instance.requireType(parType))
			{
				state.addMarker(new SemanticError(instance.getPosition(), "The implicit method argument for '" + par.name + "' is incompatible with the required type " + parType));
			}
			pOff = 1;
		}
		
		for (int i = 0; i < len; i++)
		{
			par = params.get(i + pOff);
			parType = par.type;
			IValue value = arguments.get(i);
			if (!value.requireType(parType))
			{
				state.addMarker(new SemanticError(value.getPosition(), "The method argument for '" + par.name + "' is incompatible with the required type " + parType));
			}
		}
	}
	
	@Override
	public int getSignatureMatch(String name, IType type, IType... argumentTypes)
	{
		if (!name.equals(this.qualifiedName))
		{
			return 0;
		}
		// Only matching the name
		if (argumentTypes == null)
		{
			return 1;
		}
		
		int pOff = 0;
		int match = 1;
		int len = argumentTypes.length;
		List<Parameter> params = this.parameters;
		
		// implicit modifier implementation
		if (type != null && (this.modifiers & Modifiers.IMPLICIT) == Modifiers.IMPLICIT)
		{
			if (len != params.size() - 1)
			{
				return 0;
			}
			
			IType t2 = params.get(0).type;
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
			IType t1 = params.get(i + pOff).type;
			IType t2 = argumentTypes[i];
			
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
	
	@Override
	public void setThrows(List<IType> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public List<IType> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public void addThrows(IType throwsDecl)
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
			
			int len = this.throwsDeclarations.size();
			for (int i = 0; i < len; i++)
			{
				IType t1 = this.throwsDeclarations.get(i);
				IType t2 = t1.applyState(state, context);
				if (t1 != t2)
				{
					this.throwsDeclarations.set(i, t2);
				}
			}
			
			for (Variable v : this.variables)
			{
				v.applyState(state, context);
			}
		}
		else if (state == CompilerState.RESOLVE)
		{
			Iterator<Annotation> iterator = this.annotations.iterator();
			while (iterator.hasNext())
			{
				Annotation a = iterator.next();
				if (this.processAnnotation(a))
				{
					iterator.remove();
				}
			}
			
			for (Variable v : this.variables)
			{
				v.applyState(state, context);
			}
			
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				IType t = this.theClass.getSuperType();
				if (t != null)
				{
					IClass iclass = t.getTheClass();
					if (iclass != null)
					{
						this.overrideMethod = iclass.getBody().getMethod(this.name, this.parameters);
					}
				}
			}
		}
		else if (state == CompilerState.CHECK)
		{
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				if (this.overrideMethod == null)
				{
					if ((this.modifiers & Modifiers.OVERRIDE) != 0)
					{
						state.addMarker(new SemanticError(this.position, "The method '" + this.name + "' must override or implement a supertype method"));
					}
				}
				else
				{
					if ((this.modifiers & Modifiers.OVERRIDE) == 0)
					{
						state.addMarker(new SemanticError(this.position, "The method '" + this.name + "' overrides a method, but does not have an 'override' modifier"));
					}
					
					IType type = this.overrideMethod.getType();
					if (!Type.isSuperType(type, this.type))
					{
						state.addMarker(new SemanticError(this.position, "The return type of '" + this.name + "' is incompatible with the overriden method type " + type));
					}
				}
			}
			
			if (this.statement != null && !this.statement.requireType(this.type))
			{
				state.addMarker(new SemanticError(this.statement.getPosition(), "The method '" + this.name + "' must return a result of type " + this.type));
			}
		}
		
		for (Annotation a : this.annotations)
		{
			a.applyState(state, context);
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
		return (this.modifiers & Modifiers.STATIC) != 0;
	}
	
	@Override
	public IType getThisType()
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
	public MethodMatch resolveMethod(IContext returnType, String name, IType... argumentTypes)
	{
		return this.theClass.resolveMethod(returnType, name, argumentTypes);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null)
		{
			return READ_WRITE_ACCESS;
		}
		if ((this.modifiers & Modifiers.STATIC) != 0 && iclass == this.theClass && !member.hasModifier(Modifiers.STATIC))
		{
			return STATIC;
		}
		return this.theClass.getAccessibility(member);
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		MethodVisitor visitor = writer.visitMethod(this.modifiers & 0xFFFF, this.qualifiedName, this.getDescriptor(), this.getSignature(), this.getExceptions());
		MethodWriter mw = new MethodWriter(Opcodes.ASM5, visitor);
		
		for (Annotation annotation : this.annotations)
		{
			annotation.write(mw);
		}
		
		if (this.isConstructor)
		{
			mw.setConstructor(this.type);
		}
		
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
			param.write(mw);
		}
		
		if (this.statement != null)
		{
			mw.visitCode();
			
			for (Variable var : this.variables)
			{
				var.index = index++;
			}
			
			if (this.statement != null)
			{
				this.statement.writeStatement(mw);
			}
			
			for (Variable var : this.variables)
			{
				mw.visitLocalVariable(var.qualifiedName, var.type, var.start, var.end, var.index);
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
		
		Util.parametersToString(this.parameters, buffer, true);
		
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
