package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class Property extends Member implements IProperty
{
	protected IMethod getter;

	protected IMethod setter;
	protected IValue  initializer;

	// Metadata

	protected IClass enclosingClass;

	protected CodeParameter setterParameter;
	protected SourcePosition initializerPosition;

	public Property(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;

		if (this.getter != null)
		{
			this.getter.setEnclosingClass(enclosingClass);
		}
		if (this.setter != null)
		{
			this.setter.setEnclosingClass(enclosingClass);
		}
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.METHOD;
	}

	@Override
	public IMethod getGetter()
	{
		return this.getter;
	}

	@Override
	public IMethod initGetter()
	{
		if (this.getter != null)
		{
			return this.getter;
		}
		final CodeMethod getter = new CodeMethod(this.enclosingClass, this.name, this.type, this.modifiers);
		getter.setPosition(this.position);
		return this.getter = getter;
	}

	@Override
	public IMethod getSetter()
	{
		return this.setter;
	}

	@Override
	public void setSetterParameterName(Name name)
	{
		this.initSetter();
		this.setterParameter.setName(name);
	}

	@Override
	public IMethod initSetter()
	{
		if (this.setter != null)
		{
			return this.setter;
		}

		final Name name = Name.from(this.name.unqualified + "_=", this.name.qualified + "_$eq");
		this.setter = new CodeMethod(this.enclosingClass, name, Types.VOID, this.modifiers);
		this.setter.setPosition(this.position);
		this.setterParameter = new CodeParameter(this.setter, this.position, Names.newValue, this.type,
		                                         new FlagModifierSet(), null);
		this.setter.getParameters().add(this.setterParameter);

		return this.setter;
	}

	@Override
	public IValue getInitializer()
	{
		return this.initializer;
	}

	@Override
	public void setInitializer(IValue value)
	{
		this.initializer = value;
	}

	@Override
	public SourcePosition getInitializerPosition()
	{
		return this.initializerPosition;
	}

	@Override
	public void setInitializerPosition(SourcePosition position)
	{
		this.initializerPosition = position;
	}

	@Override
	public void checkMatch(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (this.getter != null)
		{
			this.getter.checkMatch(list, receiver, name, arguments);
		}

		if (this.setter != null)
		{
			this.setter.checkMatch(list, receiver, name, arguments);
		}
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.getter != null)
		{
			final ModifierSet getterModifiers = this.getter.getModifiers();

			// Add <generated> Modifier and copy Property Modifiers
			getterModifiers.addIntModifier(Modifiers.GENERATED);
			Field.copyModifiers(this.modifiers, getterModifiers);

			// Copy Annotations
			if (this.annotations != null)
			{
				this.getter.getAnnotations().addAll(this.annotations);
			}

			this.getter.setType(this.type);
			this.getter.resolveTypes(markers, context);
		}
		if (this.setter != null)
		{
			final ModifierSet setterModifiers = this.setter.getModifiers();

			// Add <generated> Modifier and copy Property Modifiers
			setterModifiers.addIntModifier(Modifiers.GENERATED);
			Field.copyModifiers(this.modifiers, setterModifiers);

			// Copy Annotations
			if (this.annotations != null)
			{
				this.setter.getAnnotations().addAll(this.annotations);
			}

			this.setterParameter.setPosition(this.setter.getPosition());
			this.setterParameter.setType(this.type);
			this.setter.resolveTypes(markers, context);
		}
		if (this.initializer != null)
		{
			this.initializer.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.getter != null)
		{
			this.getter.resolve(markers, context);

			// Infer Type if necessary
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.getter.getType();

				if (this.setterParameter != null)
				{
					this.setterParameter.setType(this.type);
				}
			}
		}

		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semanticError(this.position, "property.type.infer", this.name));
		}

		if (this.setter != null)
		{
			this.setter.resolve(markers, context);
		}
		if (this.initializer != null)
		{
			final IValue resolved = this.initializer.resolve(markers, context);

			this.initializer = TypeChecker.convertValue(resolved, Types.VOID, Types.VOID, markers, context,
			                                            TypeChecker.markerSupplier("property.initializer.type"));
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.getter != null)
		{
			this.getter.checkTypes(markers, context);
		}
		if (this.setter != null)
		{
			this.setter.checkTypes(markers, context);
		}
		if (this.initializer != null)
		{
			this.initializer.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semanticError(this.position, "property.type.void"));
		}

		if (this.getter != null)
		{
			this.getter.check(markers, context);
		}

		if (this.setter != null)
		{
			this.setter.check(markers, context);
		}

		// No setter and no getter
		if (this.getter == null && this.setter == null)
		{
			markers.add(Markers.semantic(this.position, "property.empty", this.name));
		}

		if (this.initializer != null)
		{
			this.initializer.check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.getter != null)
		{
			this.getter.foldConstants();
		}
		if (this.setter != null)
		{
			this.setter.foldConstants();
		}
		if (this.initializer != null)
		{
			this.initializer = this.initializer.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.getter != null)
		{
			this.getter.cleanup(compilableList, classCompilableList);
		}
		if (this.setter != null)
		{
			this.setter.cleanup(compilableList, classCompilableList);
		}
		if (this.initializer != null)
		{
			this.initializer = this.initializer.cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.getter != null)
		{
			this.getter.write(writer);
		}
		if (this.setter != null)
		{
			this.setter.write(writer);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (this.initializer != null && !this.hasModifier(Modifiers.STATIC))
		{
			this.initializer.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (this.initializer != null && this.hasModifier(Modifiers.STATIC))
		{
			this.initializer.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		IDataMember.toString(prefix, buffer, this, "property.type_ascription");
		formatBody(this, prefix, buffer);
	}

	public static void formatBody(IProperty property, String prefix, StringBuilder buffer)
	{
		// Block Start
		if (Formatting.getBoolean("property.block.newline"))
		{
			buffer.append('\n').append(prefix);
		}
		else
		{
			buffer.append(' ');
		}
		buffer.append('{');

		// Initializer

		final IValue initializer = property.getInitializer();
		final IMethod getter = property.getGetter();
		final IMethod setter = property.getSetter();

		if (initializer != null)
		{
			formatInitializer(initializer, prefix, buffer);

			if (getter != null || setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Getter
		if (getter != null)
		{
			formatGetter(getter, prefix, buffer);

			if (setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Setter
		if (setter != null)
		{
			formatSetter(setter, prefix, buffer);
		}

		// Block End
		buffer.append('\n').append(prefix).append('}');
	}

	private static void formatInitializer(IValue initializer, String prefix, StringBuilder buffer)
	{
		final String initializerPrefix = Formatting.getIndent("property.initializer.indent", prefix);

		buffer.append('\n').append(initializerPrefix).append("init");

		if (Util.formatStatementList(initializerPrefix, buffer, initializer))
		{
			return;
		}

		// Separator
		if (Formatting.getBoolean("property.initializer.separator.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(':');
		if (Formatting.getBoolean("property.initializer.separator.newline_after"))
		{
			buffer.append('\n').append(initializerPrefix);
		}
		else if (Formatting.getBoolean("property.initializer.separator.space_after"))
		{
			buffer.append(' ');
		}

		initializer.toString(prefix, buffer);

		if (Formatting.getBoolean("property.initializer.semicolon"))
		{
			buffer.append(';');
		}
	}

	private static void formatGetter(IMethod getter, String prefix, StringBuilder buffer)
	{
		final String indent = Formatting.getIndent("property.getter.indent", prefix);

		final IValue value = getter.getValue();
		final ModifierSet modifiers = getter.getModifiers();
		final AnnotationList annotations = getter.getAnnotations();

		buffer.append('\n').append(indent);
		if (annotations != null)
		{
			annotations.toInlineString(indent, buffer);
		}
		if (modifiers != null)
		{
			modifiers.toString(getter.getKind(), buffer);
		}
		buffer.append("get");

		if (value != null)
		{
			if (Util.formatStatementList(indent, buffer, value))
			{
				return;
			}

			// Separator
			if (Formatting.getBoolean("property.getter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.getter.separator.newline_after"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("property.getter.separator.space_after"))
			{
				buffer.append(' ');
			}

			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("property.getter.semicolon"))
		{
			buffer.append(';');
		}
	}

	private static void formatSetter(IMethod setter, String prefix, StringBuilder buffer)
	{
		final String indent = Formatting.getIndent("property.setter.indent", prefix);

		final IValue value = setter.getValue();
		final ModifierSet setterModifiers = setter.getModifiers();
		final AnnotationList annotations = setter.getAnnotations();
		final Name setterParameterName = setter.getParameters().get(0).getName();

		buffer.append('\n').append(indent);
		if (annotations != null)
		{
			annotations.toInlineString(indent, buffer);
		}
		if (setterModifiers != null)
		{
			setterModifiers.toString(setter.getKind(), buffer);
		}
		buffer.append("set");

		if (setterParameterName != Names.newValue)
		{
			Formatting.appendSeparator(buffer, "property.setter.parameter.open_paren", '(');
			buffer.append(setterParameterName);
			Formatting.appendSeparator(buffer, "property.setter.parameter.close_paren", ')');
		}

		if (value != null)
		{
			if (Util.formatStatementList(indent, buffer, value))
			{
				return;
			}

			// Separator
			if (Formatting.getBoolean("property.setter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.setter.separator.newline_after"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("property.setter.separator.space_after"))
			{
				buffer.append(' ');
			}

			value.toString(indent, buffer);
		}

		if (Formatting.getBoolean("property.setter.semicolon"))
		{
			buffer.append(';');
		}
	}
}
