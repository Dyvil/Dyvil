package dyvilx.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.Iterators;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.phase.ResolvableList;

import java.util.ArrayList;

public class ClassList extends ArrayList<IClass> implements Formattable, ResolvableList<IClass>
{
	// =============== Constructors ===============

	public ClassList()
	{
	}

	public ClassList(int capacity)
	{
		super(capacity);
	}

	// =============== Methods ===============

	// --------------- Access ---------------

	public IClass get(Name name)
	{
		for (final IClass iclass : this)
		{
			if (iclass.getName() == name)
			{
				return iclass;
			}
		}
		return null;
	}

	// --------------- Iteration ---------------

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
		for (final IClass iclass : this)
		{
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
		for (final IClass iclass : this)
		{
			if (iclass.hasModifier(Modifiers.EXTENSION) && iclass.getBody() != null)
			{
				// s.a. for body rationale
				iclass.getBody().getImplicitMatches(list, value, targetType);
			}
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
		for (final IClass iclass : this)
		{
			iclass.toString(indent, buffer);

			buffer.append("\n\n").append(indent);
		}
	}
}
