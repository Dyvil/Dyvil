package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
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
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class Property extends Member implements IProperty
{
	protected IClass theClass;
	
	protected IMethod getter;
	protected IMethod setter;

	private MethodParameter setterParameter;
	
	public Property(ICodePosition position, IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.position = position;
		this.theClass = iclass;
	}
	
	@Override
	public void setTheClass(IClass iclass)
	{
		this.theClass = iclass;

		if (this.getter != null)
		{
			this.getter.setTheClass(iclass);
		}
		if (this.setter != null)
		{
			this.setter.setTheClass(iclass);
		}
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
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
		return this.getter = new CodeMethod(this.theClass, this.name, this.type, this.modifiers);
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
		this.setter = new CodeMethod(this.theClass, name, Types.VOID, this.modifiers);
		this.setterParameter = new MethodParameter(this.position, this.name, this.type, EmptyModifiers.INSTANCE);
		this.setter.addParameter(this.setterParameter);

		return this.setter;
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
			this.getter.resolveTypes(markers, context);
		}
		if (this.setter != null)
		{
			this.setter.resolveTypes(markers, context);
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
				mw.setThisType(this.theClass.getInternalName());
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
				mw.setThisType(this.theClass.getInternalName());
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

		// Getters
		if (this.getter != null)
		{
			String getterPrefix = Formatting.getIndent("property.getter.indent", prefix);

			final IValue getterValue = this.getter.getValue();
			final ModifierSet getterModifiers = this.getter.getModifiers();

			buffer.append('\n').append(prefix);
			if (getterModifiers != null)
			{
				getterModifiers.toString(buffer);
			}
			buffer.append("get");

			// Separator
			if (Formatting.getBoolean("property.getter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.getter.newline"))
			{
				buffer.append('\n').append(getterPrefix);
			}
			else if (Formatting.getBoolean("property.getter.separator.space_after"))
			{
				buffer.append(' ');
			}

			if (getterValue != null)
			{
				getterValue.toString(getterPrefix, buffer);
			}

			if (Formatting.getBoolean("property.getter.semicolon"))
			{
				buffer.append(';');
			}
		}

		// Setters
		if (this.setter != null)
		{
			String setterPrefix = Formatting.getIndent("property.setter.indent", prefix);
			final IValue setterValue = this.setter.getValue();
			final ModifierSet setterModifiers = this.setter.getModifiers();

			buffer.append('\n').append(prefix);
			if (setterModifiers != null)
			{
				setterModifiers.toString(buffer);
			}
			buffer.append("set");

			// Separator
			if (Formatting.getBoolean("property.setter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.setter.newline"))
			{
				buffer.append('\n').append(setterPrefix);
			}
			else if (Formatting.getBoolean("property.setter.separator.space_after"))
			{
				buffer.append(' ');
			}

			if (setterValue != null)
			{
				setterValue.toString(setterPrefix, buffer);
			}

			if (Formatting.getBoolean("property.setter.semicolon"))
			{
				buffer.append(';');
			}
		}

		// Block End
		buffer.append('\n').append(prefix).append('}');
	}
}
