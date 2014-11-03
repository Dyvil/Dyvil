package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class CodeClass extends ASTObject implements IClass
{
	protected CompilationUnit	unit;
	
	protected int				type;
	
	protected int				modifiers;
	
	protected String			name;
	protected String			internalName;
	
	protected List<Annotation>	annotations		= new ArrayList(1);
	
	protected List<Type>		superClasses	= new ArrayList(1);
	
	protected ClassBody			body;
	
	public CodeClass()
	{}
	
	public CodeClass(ICodePosition position, CompilationUnit unit, int type, ClassBody body)
	{
		this.position = position;
		this.unit = unit;
		this.type = type;
		this.body = body;
	}
	
	public int getClassType()
	{
		return this.type;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		this.internalName = this.unit.getInternalName(this.name);
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public List<Type> getSuperClasses()
	{
		return this.superClasses;
	}
	
	@Override
	public void setTypes(List<Type> types)
	{
		this.superClasses = types;
	}
	
	@Override
	public List<Type> getTypes()
	{
		return this.superClasses;
	}
	
	@Override
	public void addType(Type type)
	{
		this.superClasses.add(type);
	}
	
	@Override
	public void setBody(ClassBody body)
	{
		this.body = body;
	}
	
	@Override
	public ClassBody getBody()
	{
		return this.body;
	}
	
	@Override
	public String getInternalName()
	{
		return this.internalName;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		// TODO Actual super classes / interfaces
		writer.visit(Opcodes.V1_8, this.modifiers, this.internalName, null, "java/lang/Object", null);
		
		List<IField> fields = this.body.fields;
		for (IField f : fields)
		{
			f.write(writer);
		}
		
		List<IMethod> methods = this.body.methods;
		for (IMethod m : methods)
		{
			m.write(writer);
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Type getThisType()
	{
		return new Type(this.name, this);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.name.equals(name))
		{
			return this;
		}
		
		return this.unit.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		// Own fields
		IField field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}
		
		// Inherited Fields
		for (Type type : this.superClasses)
		{
			field = type.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		
		return null;
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		// Own methods
		IMethod method = this.body.getMethod(name);
		if (method != null)
		{
			return method;
		}
		
		for (Type type : this.superClasses)
		{
			method = type.resolveMethodName(name);
			if (method != null)
			{
				return method;
			}
		}
		
		return null;
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		// Own methods
		List<IMethod> list = new ArrayList();
		this.body.getMethods(list, name, args);
		
		if (!list.isEmpty())
		{
			return list.get(0);
		}
		
		for (Type type : this.superClasses)
		{
			IMethod method = type.resolveMethod(name, args);
			if (method != null)
			{
				list.add(method);
			}
		}
		
		// TODO Static, Accessibility, Ambiguity
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	@Override
	public CodeClass applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.superClasses.replaceAll(t -> t.applyState(state, context));
		}
		
		this.body = this.body.applyState(state, this);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix);
		buffer.append(Modifiers.CLASS.toString(this.modifiers));
		buffer.append(Modifiers.CLASS_TYPE.toString(this.type));
		buffer.append(this.name);
		
		if (!this.superClasses.isEmpty())
		{
			buffer.append(" extends ");
			Util.astToString(this.superClasses, Formatting.Class.superClassesSeperator, buffer);
		}
		
		buffer.append(Formatting.Class.bodyStart);
		this.body.toString(Formatting.Class.bodyIndent, buffer);
		buffer.append(Formatting.Class.bodyEnd);
	}
}
