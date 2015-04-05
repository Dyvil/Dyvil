package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ClassBody extends ASTNode implements IClassBody
{
	public IClass			theClass;
	
	public IClass[]			classes;
	public int				classCount;
	
	private IField[]		fields			= new IField[3];
	private int				fieldCount;
	private IConstructor[]	constructors	= new IConstructor[1];
	private int				constructorCount;
	private IMethod[]		methods			= new IMethod[3];
	private int				methodCount;
	private IProperty[]		properties		= new IProperty[3];
	private int				propertyCount;
	
	protected IMethod		functionalMethod;
	
	public ClassBody(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public void setTheClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	// Nested Classes
	
	@Override
	public int classCount()
	{
		return this.classCount;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		if (this.classes == null)
		{
			this.classes = new IClass[2];
			this.classes[0] = iclass;
			this.classCount = 1;
			return;
		}
		
		int index = this.classCount++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.classCount];
			System.arraycopy(this.classes, 0, temp, 0, index);
			this.classes = temp;
		}
		this.classes[index] = iclass;
	}
	
	@Override
	public IClass getClass(int index)
	{
		return this.classes[index];
	}
	
	@Override
	public IClass getClass(Name name)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			IClass c = this.classes[i];
			if (c.getName() == name)
			{
				return c;
			}
		}
		return null;
	}
	
	// Fields
	
	@Override
	public int fieldCount()
	{
		return this.fieldCount;
	}
	
	@Override
	public void addField(IField field)
	{
		int index = this.fieldCount++;
		if (index >= this.fields.length)
		{
			IField[] temp = new IField[this.fieldCount];
			System.arraycopy(this.fields, 0, temp, 0, index);
			this.fields = temp;
		}
		this.fields[index] = field;
	}
	
	@Override
	public IField getField(int index)
	{
		return this.fields[index];
	}
	
	@Override
	public IField getField(Name name)
	{
		for (int i = 0; i < this.fieldCount; i++)
		{
			IField f = this.fields[i];
			if (f.getName() == name)
			{
				return f;
			}
		}
		return null;
	}
	
	// Properties
	
	@Override
	public int propertyCount()
	{
		return this.propertyCount;
	}
	
	@Override
	public void addProperty(IProperty property)
	{
		int index = this.propertyCount++;
		if (index >= this.properties.length)
		{
			IProperty[] temp = new IProperty[this.propertyCount];
			System.arraycopy(this.properties, 0, temp, 0, index);
			this.properties = temp;
		}
		this.properties[index] = property;
	}
	
	@Override
	public IProperty getProperty(int index)
	{
		return this.properties[index];
	}
	
	@Override
	public IProperty getProperty(Name name)
	{
		for (int i = 0; i < this.propertyCount; i++)
		{
			IProperty p = this.properties[i];
			if (p.getName() == name)
			{
				return p;
			}
		}
		return null;
	}
	
	// Constructors
	
	@Override
	public int constructorCount()
	{
		return this.constructorCount;
	}
	
	@Override
	public void addConstructor(IConstructor constructor)
	{
		int index = this.constructorCount++;
		if (index >= this.constructors.length)
		{
			IConstructor[] temp = new IConstructor[this.constructorCount];
			System.arraycopy(this.constructors, 0, temp, 0, index);
			this.constructors = temp;
		}
		this.constructors[index] = constructor;
	}
	
	@Override
	public IConstructor getConstructor(int index)
	{
		return this.constructors[index];
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		for (int i = 0; i < this.constructorCount; i++)
		{
			IConstructor c = this.constructors[i];
			int m = c.getSignatureMatch(arguments);
			if (m > 0)
			{
				list.add(new ConstructorMatch(c, m));
			}
		}
	}
	
	// Methods
	
	@Override
	public int methodCount()
	{
		return this.methodCount;
	}
	
	@Override
	public void addMethod(IMethod method)
	{
		int index = this.methodCount++;
		if (index >= this.methods.length)
		{
			IMethod[] temp = new IMethod[this.methodCount];
			System.arraycopy(this.methods, 0, temp, 0, index);
			this.methods = temp;
		}
		this.methods[index] = method;
	}
	
	@Override
	public IMethod getMethod(int index)
	{
		return this.methods[index];
	}
	
	@Override
	public IMethod getMethod(Name name)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			IMethod m = this.methods[i];
			if (m.getName() == name)
			{
				return m;
			}
		}
		return null;
	}
	
	@Override
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount)
	{
		outer:
		for (int i = 0; i < this.methodCount; i++)
		{
			IMethod m = this.methods[i];
			if (m.getName() != name)
			{
				continue;
			}
			
			if (parameterCount != m.parameterCount())
			{
				continue;
			}
			
			for (int p = 0; p < parameterCount; p++)
			{
				IType t1 = parameters[p].getType();
				IType t2 = m.getParameter(p).getType();
				if (!t1.classEquals(t2) && t1.getArrayDimensions() != t2.getArrayDimensions())
				{
					continue outer;
				}
			}
			return m;
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			IMethod m = this.methods[i];
			int match = m.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(m, match));
			}
		}
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		if (this.functionalMethod != null)
		{
			return this.functionalMethod;
		}
		
		for (int i = 0; i < this.methodCount; i++)
		{
			IMethod m = this.methods[i];
			if (m.hasModifier(Modifiers.ABSTRACT))
			{
				this.functionalMethod = m;
				return m;
			}
		}
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].resolve(markers, context);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].resolve(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].resolve(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolve(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].check(markers, context);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].check(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].check(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].check(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].foldConstants();
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].foldConstants();
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].foldConstants();
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].foldConstants();
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].foldConstants();
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Class.bodyStart).append('\n');
		String prefix1 = prefix + Formatting.Class.bodyIndent;
		
		if (this.classCount > 0)
		{
			for (int i = 0; i < this.classCount; i++)
			{
				IClass c = this.classes[i];
				if (c != this.theClass)
				{
					c.toString(prefix1, buffer);
					buffer.append('\n');
				}
			}
			buffer.append('\n');
		}
		
		if (this.fieldCount > 0)
		{
			for (int i = 0; i < this.fieldCount; i++)
			{
				this.fields[i].toString(prefix1, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
		if (this.constructorCount > 0)
		{
			for (int i = 0; i < this.constructorCount; i++)
			{
				this.constructors[i].toString(prefix1, buffer);
				if (i + 1 < this.constructorCount)
				{
					buffer.append('\n');
				}
			}
			buffer.append('\n');
		}
		
		if (this.propertyCount > 0)
		{
			for (int i = 0; i < this.propertyCount; i++)
			{
				this.properties[i].toString(prefix1, buffer);
				if (i + 1 < this.propertyCount)
				{
					buffer.append('\n');
				}
			}
			buffer.append('\n');
		}
		
		if (this.methodCount > 0)
		{
			for (int i = 0; i < this.methodCount; i++)
			{
				IMethod method = this.methods[i];
				method.toString(prefix1, buffer);
				buffer.append('\n');
				if (i + 1 < this.methodCount)
				{
					buffer.append('\n');
				}
			}
		}
		
		buffer.append(prefix).append(Formatting.Class.bodyEnd);
	}
}
