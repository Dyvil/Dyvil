package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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

	protected MethodParameter setterParameter;
	protected ICodePosition   initializerPosition;

	public Property(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
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
		return this.getter = new CodeMethod(this.enclosingClass, this.name, this.type, this.modifiers);
	}

	@Override
	public IMethod getSetter()
	{
		return this.setter;
	}

	@Override
	public void setSetterParameterName(Name setterParameterName)
	{
		this.initSetter();
		this.setterParameter.setName(setterParameterName);
	}

	@Override
	public IMethod initSetter()
	{
		if (this.setter != null)
		{
			return this.setter;
		}

		final Name name = Name.get(this.name.unqualified + "_=", this.name.qualified + "_$eq");
		this.setter = new CodeMethod(this.enclosingClass, name, Types.VOID, this.modifiers);
		this.setterParameter = new MethodParameter(this.position, this.name, this.type, EmptyModifiers.INSTANCE, null);
		this.setter.addParameter(this.setterParameter);

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
	public ICodePosition getInitializerPosition()
	{
		return this.initializerPosition;
	}

	@Override
	public void setInitializerPosition(ICodePosition position)
	{
		this.initializerPosition = position;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.getter != null)
		{
			IContext.getMethodMatch(list, receiver, name, arguments, this.getter);
		}

		if (this.setter != null)
		{
			IContext.getMethodMatch(list, receiver, name, arguments, this.setter);
		}
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.getter != null)
		{
			this.getter.getModifiers().addIntModifier(this.modifiers.toFlags());
			this.getter.resolveTypes(markers, context);
		}
		if (this.setter != null)
		{
			this.setter.getModifiers().addIntModifier(this.modifiers.toFlags());
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

		if (this.setter != null)
		{
			this.setter.resolve(markers, context);
		}
		if (this.getter != null)
		{
			this.getter.resolve(markers, context);
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
		
		if (this.getter != null)
		{
			this.getter.check(markers, context);
		}

		if (this.setter != null)
		{
			this.setter.check(markers, context);

			if (this.type == Types.VOID)
			{
				markers.add(Markers.semantic(this.position, "property.type.void"));
			}
		}
		
		// No setter and no getter
		if (this.getter == null && this.setter == null)
		{
			markers.add(Markers.semantic(this.position, "property.empty", this.name));
		}

		if (this.initializer != null)
		{
			this.initializer.check(markers, context);

			if (this.enclosingClass.hasModifier(Modifiers.INTERFACE_CLASS))
			{
				markers.add(Markers.semantic(this.initializerPosition, "property.initializer.interface"));
			}
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.getter != null)
		{
			this.getter.cleanup(context, compilableList);
		}
		if (this.setter != null)
		{
			this.setter.cleanup(context, compilableList);
		}
		if (this.initializer != null)
		{
			this.initializer = this.initializer.cleanup(context, compilableList);
		}
	}
	
	// Compilation
	
	protected void writeAnnotations(MethodWriter mw, int modifiers)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}
		
		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String extended = this.type.getExtendedName();
		String signature = this.type.getSignature();
		if (this.getter != null)
		{
			final IValue getterValue = this.getter.getValue();
			final ModifierSet getterModifiers = this.getter.getModifiers();

			int modifiers = this.modifiers.toFlags();

			if (getterModifiers != null)
			{
				modifiers |= getterModifiers.toFlags();
			}

			MethodWriter mw = new MethodWriterImpl(writer,
			                                       writer.visitMethod(modifiers, this.name.qualified, "()" + extended,
			                                                          signature == null ? null : "()" + signature,
			                                                          null));

			if ((modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.enclosingClass.getInternalName());
			}

			this.writeAnnotations(mw, modifiers);

			if (getterValue != null)
			{
				mw.begin();
				getterValue.writeExpression(mw, this.type);
				mw.end(this.type);
			}
		}
		if (this.setter != null)
		{
			final IValue setterValue = this.setter.getValue();
			final ModifierSet setterModifiers = this.setter.getModifiers();

			int modifiers = this.modifiers.toFlags();
			if (setterModifiers != null)
			{
				modifiers |= setterModifiers.toFlags();
			}
			
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified + "_$eq",
			                                                                  "(" + extended + ")V", signature == null ?
					                                                                  null :
					                                                                  "(" + signature + ")V", null));

			if ((modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.enclosingClass.getInternalName());
			}
			
			this.writeAnnotations(mw, modifiers);
			this.setter.getParameter(0).write(mw);
			
			if (setterValue != null)
			{
				mw.begin();
				setterValue.writeExpression(mw, Types.VOID);
				mw.end(Types.VOID);
			}
		}
	}

	@Override
	public void writeInit(MethodWriter writer) throws BytecodeException
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

		this.modifiers.toString(buffer);
		this.type.toString("", buffer);
		buffer.append(' ');
		buffer.append(this.name);

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

		if (this.initializer != null)
		{
			this.formatInitializer(prefix, buffer);

			if (this.getter != null || this.setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Getter
		if (this.getter != null)
		{
			this.formatGetter(prefix, buffer);

			if (this.setter != null)
			{
				buffer.append('\n').append(prefix);
			}
		}

		// Setter
		if (this.setter != null)
		{
			this.formatSetter(prefix, buffer);
		}

		// Block End
		buffer.append('\n').append(prefix).append('}');
	}

	private void formatInitializer(String prefix, StringBuilder buffer)
	{
		final String initializerPrefix = Formatting.getIndent("property.initializer.indent", prefix);

		buffer.append('\n').append(initializerPrefix).append("init");

		if (Util.formatStatementList(initializerPrefix, buffer, this.initializer))
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

		this.initializer.toString(prefix, buffer);

		if (Formatting.getBoolean("property.initializer.semicolon"))
		{
			buffer.append(';');
		}
	}

	private void formatGetter(String prefix, StringBuilder buffer)
	{
		final String getterPrefix = Formatting.getIndent("property.getter.indent", prefix);

		final IValue getterValue = this.getter.getValue();
		final ModifierSet getterModifiers = this.getter.getModifiers();

		buffer.append('\n').append(getterPrefix);
		if (getterModifiers != null && getterModifiers != this.modifiers)
		{
			getterModifiers.toString(buffer);
		}
		buffer.append("get");

		if (getterValue != null)
		{
			if (Util.formatStatementList(getterPrefix, buffer, getterValue))
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
				buffer.append('\n').append(getterPrefix);
			}
			else if (Formatting.getBoolean("property.getter.separator.space_after"))
			{
				buffer.append(' ');
			}

			getterValue.toString(getterPrefix, buffer);
		}

		if (Formatting.getBoolean("property.getter.semicolon"))
		{
			buffer.append(';');
		}
	}

	private void formatSetter(String prefix, StringBuilder buffer)
	{
		final String setterPrefix = Formatting.getIndent("property.setter.indent", prefix);
		final IValue setterValue = this.setter.getValue();
		final ModifierSet setterModifiers = this.setter.getModifiers();
		final Name setterParameterName = this.setterParameter.getName();

		buffer.append('\n').append(setterPrefix);
		if (setterModifiers != null && setterModifiers != this.modifiers)
		{
			setterModifiers.toString(buffer);
		}
		buffer.append("set");

		if (setterParameterName != this.name)
		{
			Formatting.appendSeparator(buffer, "property.setter.parameter.open_paren", '(');
			buffer.append(setterParameterName);
			Formatting.appendSeparator(buffer, "property.setter.parameter.close_paren", ')');
		}

		if (setterValue != null)
		{
			if (Util.formatStatementList(setterPrefix, buffer, setterValue))
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
				buffer.append('\n').append(setterPrefix);
			}
			else if (Formatting.getBoolean("property.setter.separator.space_after"))
			{
				buffer.append(' ');
			}

			setterValue.toString(setterPrefix, buffer);
		}

		if (Formatting.getBoolean("property.setter.semicolon"))
		{
			buffer.append(';');
		}
	}
}
