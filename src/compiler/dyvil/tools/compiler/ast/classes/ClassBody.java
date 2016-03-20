package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ClassBody implements IClassBody
{
	public IClass theClass;
	
	public IClass[] classes;
	public int      classCount;
	
	private IField[] fields = new IField[3];
	private int fieldCount;
	private IProperty[] properties = new IProperty[3];
	private int propertyCount;
	private IMethod[] methods = new IMethod[3];
	private int methodCount;
	private IConstructor[] constructors = new IConstructor[1];
	private int constructorCount;
	private IInitializer[] initializers = new IInitializer[1];
	private int initializerCount;

	protected IMethod functionalMethod;
	
	public ClassBody(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
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
		iclass.setEnclosingClass(this.theClass);

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
		field.setEnclosingClass(this.theClass);

		final int index = this.fieldCount++;
		if (index >= this.fields.length)
		{
			IField[] temp = new IField[index * 2];
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
		property.setEnclosingClass(this.theClass);

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

	// Methods

	@Override
	public int methodCount()
	{
		return this.methodCount;
	}

	@Override
	public void addMethod(IMethod method)
	{
		method.setEnclosingClass(this.theClass);

		final int index = this.methodCount++;
		if (index >= this.methods.length)
		{
			final IMethod[] temp = new IMethod[index * 2];
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
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			IContext.getMethodMatch(list, receiver, name, arguments, this.methods[i]);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		if (this.functionalMethod != null)
		{
			return this.functionalMethod;
		}

		boolean found = false;
		IMethod match = null;
		for (int i = 0; i < this.methodCount; i++)
		{
			IMethod m = this.methods[i];
			if (m.isAbstract())
			{
				if (found)
				{
					return null;
				}

				found = true;
				match = m;
			}
		}
		return this.functionalMethod = match;
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
		constructor.setEnclosingClass(this.theClass);

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
	public IConstructor getConstructor(IParameter[] parameters, int parameterCount)
	{
		outer:
		for (int i = 0; i < this.constructorCount; i++)
		{
			IConstructor c = this.constructors[i];
			if (c.parameterCount() != parameterCount)
			{
				continue;
			}
			
			for (int p = 0; p < parameterCount; p++)
			{
				IType classParamType = parameters[p].getType();
				IType constructorParamType = c.getParameter(p).getType();
				if (!Types.isSameType(classParamType, constructorParamType))
				{
					continue outer;
				}
			}
			
			return c;
		}
		
		return null;
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		for (int i = 0; i < this.constructorCount; i++)
		{
			IConstructor ctor = this.constructors[i];
			float match = ctor.getSignatureMatch(arguments);
			if (match > 0)
			{
				list.add(ctor, match);
			}
		}
	}

	// Initializers

	@Override
	public int initializerCount()
	{
		return this.initializerCount;
	}

	@Override
	public void addInitializer(IInitializer initializer)
	{
		initializer.setEnclosingClass(this.theClass);

		int index = this.initializerCount++;
		if (index >= this.initializers.length)
		{
			IInitializer[] temp = new IInitializer[this.initializerCount];
			System.arraycopy(this.initializers, 0, temp, 0, index);
			this.initializers = temp;
		}
		this.initializers[index] = initializer;
	}

	@Override
	public IInitializer getInitializer(int index)
	{
		return this.initializers[index];
	}

	// Phases
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		final IDyvilHeader header = this.theClass.getHeader();

		for (int i = 0; i < this.classCount; i++)
		{
			final IClass innerClass = this.classes[i];
			innerClass.setHeader(header);
			innerClass.resolveTypes(markers, context);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].resolveTypes(markers, context);
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
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolve(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolve(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].resolve(markers, context);
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
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].checkTypes(markers, context);
		}
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			if (checkOverride(this.methods[i], candidate, typeContext))
			{
				return true;
			}
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			final IProperty property = this.properties[i];

			final IMethod getter = property.getGetter();
			if (getter != null && checkOverride(getter, candidate, typeContext))
			{
				return true;
			}

			final IMethod setter = property.getSetter();
			if (setter != null && checkOverride(setter, candidate, typeContext))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean checkOverride(IMethod method, IMethod candidate, ITypeContext typeContext)
	{
		if (method.checkOverride(candidate, typeContext))
		{
			method.addOverride(candidate);
			return !method.hasModifier(Modifiers.ABSTRACT);
		}
		return false;
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			final IMethod candidate = this.methods[i];
			this.checkMethod(markers, checkedClass, typeContext, candidate);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			final IProperty property = this.properties[i];

			final IMethod getter = property.getGetter();
			if (getter != null)
			{
				this.checkMethod(markers, checkedClass, typeContext, getter);
			}

			final IMethod setter = property.getSetter();
			if (setter != null)
			{
				this.checkMethod(markers, checkedClass, typeContext, setter);
			}
		}
	}

	private void checkMethod(MarkerList markers, IClass checkedClass, ITypeContext typeContext, IMethod candidate)
	{
		if (candidate.hasModifier(Modifiers.STATIC))
		{
			// Don't check static methods
			return;
		}

		// Check if the super class implements the method
		// We cannot do the modifier checks beforehand, because methods need to know which methods they implement to
		// generate bridges.
		if (checkedClass.checkImplements(candidate, typeContext))
		{
			return;
		}

		// If it doesn't, check for abstract modifiers
		if (candidate.hasModifier(Modifiers.ABSTRACT) && !checkedClass.hasModifier(Modifiers.ABSTRACT))
		{
			// Create an abstract method error
			markers.add(Markers.semantic(checkedClass.getPosition(), "class.method.abstract", checkedClass.getName(),
			                             candidate.getName(), this.theClass.getName()));
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
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].check(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].check(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].check(markers, context);
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
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].foldConstants();
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].foldConstants();
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup( IContext context)
	{
		final IClassCompilableList compilableList = this.theClass;

		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].cleanup(context, compilableList);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].cleanup(context, compilableList);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].cleanup(context, compilableList);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].cleanup(context, compilableList);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].cleanup(context, compilableList);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].cleanup(context, compilableList);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		String bodyPrefix = Formatting.getIndent("class.body.indent", prefix);
		if (Formatting.getBoolean("class.body.open_bracket.newline"))
		{
			buffer.append('\n').append(prefix);
		}
		else
		{
			buffer.append(' ');
		}

		buffer.append('{').append('\n');
		this.bodyToString(bodyPrefix, buffer);
		buffer.append(prefix).append('}');

		if (Formatting.getBoolean("class.body.close_bracket.newline_after"))
		{
			buffer.append('\n');
		}
	}

	private void bodyToString(String prefix, StringBuilder buffer)
	{
		if (this.classCount > 0)
		{
			this.membersToString(prefix, this.classes, this.classCount, buffer);
		}

		if (this.fieldCount > 0)
		{
			for (int i = 0; i < this.fieldCount; i++)
			{
				this.fields[i].toString(prefix, buffer);

				if (Formatting.getBoolean("field.declaration.semicolon"))
				{
					buffer.append(';');
				}

				buffer.append('\n');
			}
			buffer.append('\n');
		}

		if (this.constructorCount > 0)
		{
			this.membersToString(prefix, this.constructors, this.constructorCount, buffer);
		}

		if (this.initializerCount > 0)
		{
			this.membersToString(prefix, this.initializers, this.initializerCount, buffer);
		}

		if (this.propertyCount > 0)
		{
			this.membersToString(prefix, this.properties, this.propertyCount, buffer);
		}

		if (this.methodCount > 0)
		{
			for (int i = 0; i < this.methodCount; i++)
			{
				this.methods[i].toString(prefix, buffer);
				buffer.append('\n');
				if (i + 1 < this.methodCount)
				{
					buffer.append('\n');
				}
			}
		}
	}

	private void membersToString(String prefix, IASTNode[] constructors, int count, StringBuilder buffer)
	{
		for (int i = 0; i < count; i++)
		{
			constructors[i].toString(prefix, buffer);
			buffer.append('\n');
		}
	}
}
