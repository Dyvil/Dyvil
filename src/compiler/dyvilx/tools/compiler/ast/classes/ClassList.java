package dyvilx.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.Iterators;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class ClassList implements Formattable, Resolvable, Iterable<IClass>
{
	// =============== Constants ===============

	private static final int DEFAULT_CAPACITY = 1;

	// =============== Fields ===============

	protected IClass[] classes;
	protected int      size;

	// =============== Constructors ===============

	public ClassList()
	{
		this(DEFAULT_CAPACITY);
	}

	public ClassList(int capacity)
	{
		this(capacity <= 0 ? null : new IClass[capacity], 0);
	}

	public ClassList(IClass[] classes, int size)
	{
		this.classes = classes;
		this.size = size;
	}

	// =============== Methods ===============

	// --------------- Access ---------------

	public int size()
	{
		return this.size;
	}

	public IClass get(int index)
	{
		return this.classes[index];
	}

	public IClass get(Name name)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IClass theClass = this.classes[i];
			if (theClass.getName() == name)
			{
				return theClass;
			}
		}
		return null;
	}

	// --------------- Modification ---------------

	public void add(IClass iclass)
	{
		if (this.classes == null)
		{
			this.classes = new IClass[DEFAULT_CAPACITY];
		}

		int index = this.size++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.size];
			System.arraycopy(this.classes, 0, temp, 0, this.classes.length);
			this.classes = temp;
		}

		this.classes[index] = iclass;
	}

	// --------------- Iteration ---------------

	@Override
	public Iterator<IClass> iterator()
	{
		return new ArrayIterator<>(this.classes, 0, this.size);
	}

	public Iterable<IClass> objectClasses()
	{
		return () -> Iterators.filtered(this.iterator(), IClass::isObject);
	}

	public Iterable<IField> objectClassInstanceFields()
	{
		return () -> Iterators
			             .mapped(this.objectClasses().iterator(), iclass -> iclass.getMetadata().getInstanceField());
	}

	// --------------- Context Resolution ---------------

	public IField resolveImplicitObjectInstanceField(IType type)
	{
		return ClassBody.resolveImplicitField(type, this.objectClassInstanceFields());
	}

	public void getExtensionMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IClass iclass = this.classes[i];
			if (iclass.hasModifier(Modifiers.EXTENSION) && iclass.getBody() != null)
			{
				// only uses the body to avoid infinite recursion with PrimitiveType
				// (and because extension classes can only define extension methods in the body anyway)
				iclass.getBody().getMethodMatches(list, receiver, name, arguments);
			}
		}
	}

	public void getExtensionImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IClass iclass = this.classes[i];
			if (iclass.hasModifier(Modifiers.EXTENSION) && iclass.getBody() != null)
			{
				// s.a. for body rationale
				iclass.getBody().getImplicitMatches(list, value, targetType);
			}
		}
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].resolve(markers, context);
		}
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].check(markers, context);
		}
	}

	// --------------- Compilation Phases ---------------

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].cleanup(compilableList, classCompilableList);
		}
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull StringBuilder buffer)
	{
		this.toString("", buffer);
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.classes[i].toString(indent, buffer);

			buffer.append("\n\n").append(indent);
		}
	}
}
