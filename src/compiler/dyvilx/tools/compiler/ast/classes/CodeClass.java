package dyvilx.tools.compiler.ast.classes;

import dyvil.collection.mutable.IdentityHashSet;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.ASMConstants;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.metadata.TraitMetadata;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeClass extends AbstractClass
{
	protected ArgumentList  superConstructorArguments;
	protected AttributeList constructorAttributes;

	// Metadata
	protected IHeaderUnit    unit;
	protected SourcePosition position;

	public CodeClass()
	{
	}

	public CodeClass(SourcePosition position, Name name, AttributeList attributes)
	{
		super(attributes);
		this.position = position;
		this.name = name;
	}

	public CodeClass(IHeaderUnit unit, Name name)
	{
		this.unit = unit;
		this.name = name;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this.unit;
	}

	@Override
	public void setHeader(IHeaderUnit unit)
	{
		this.unit = unit;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public ArgumentList getSuperConstructorArguments()
	{
		return this.superConstructorArguments;
	}

	@Override
	public void setSuperConstructorArguments(ArgumentList arguments)
	{
		this.superConstructorArguments = arguments;
	}

	@Override
	public AttributeList getConstructorAttributes()
	{
		if (this.constructorAttributes != null)
		{
			return this.constructorAttributes;
		}
		return this.constructorAttributes = new AttributeList();
	}

	@Override
	public void setConstructorAttributes(AttributeList attributes)
	{
		this.constructorAttributes = attributes;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.metadata == null)
		{
			this.metadata = IClass.getClassMetadata(this, this.attributes.flags());
		}

		this.attributes.resolveTypes(markers, context, this);
		ModifierChecks.checkModifiers(this, markers);

		this.metadata.resolveTypesPre(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolveTypes(markers, context);
		}

		this.parameters.resolveTypes(markers, context);

		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(markers, context);
		}

		if (this.interfaces != null)
		{
			this.interfaces.resolveTypes(markers, context);
		}

		this.metadata.resolveTypesHeader(markers, context);

		// This has to happen here because the metadata might add 'abstract' modifiers in resolveTypesHeader
		this.checkFunctional(markers);

		if (this.body != null)
		{
			this.body.resolveTypes(markers, context);
		}

		this.metadata.resolveTypesBody(markers, context);

		this.metadata.resolveTypesGenerate(markers, context);

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.attributes.resolve(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolve(markers, context);
		}

		this.parameters.resolve(markers, context);

		if (this.superType != null)
		{
			this.superType.resolve(markers, context);
		}

		if (this.interfaces != null)
		{
			this.interfaces.resolve(markers, context);
		}

		this.metadata.resolve(markers, context);

		if (this.body != null)
		{
			this.body.resolve(markers, context);
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.enclosingClass != null && !this.hasModifier(Modifiers.STATIC))
		{
			this.attributes.addFlag(Modifiers.STATIC);
			if ((this.attributes.flags() & Modifiers.CLASS_TYPE_MODIFIERS) == 0)
			{
				// is a plain old class, not an interface, trait, annotation, object or enum
				markers.add(Markers.semantic(this.position, "class.inner.not_static", this.name));
			}
		}

		this.attributes.checkTypes(markers, context);

		this.metadata.checkTypes(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.checkTypes(markers, context);
		}

		this.checkSuperMethods(markers, this, this.getThisType(), new IdentityHashSet<>());

		this.parameters.checkTypes(markers, context);

		if (this.superType != null)
		{
			this.superType.checkType(markers, context, TypePosition.SUPER_TYPE);
		}

		if (this.interfaces != null)
		{
			this.interfaces.checkTypes(markers, context, TypePosition.SUPER_TYPE);
		}

		if (this.body != null)
		{
			this.body.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.attributes.check(markers, context, this.getElementType());

		if (this.typeParameters != null)
		{
			this.typeParameters.check(markers, context);
		}

		this.parameters.check(markers, context);

		if (this.superType != null)
		{
			this.superType.check(markers, context);

			final IClass superClass;
			if (!this.hasModifier(Modifiers.EXTENSION_CLASS) && (superClass = this.superType.getTheClass()) != null)
			{
				final AttributeList superAttributes = superClass.getAttributes();
				if (superAttributes.hasAnyFlag(Modifiers.CLASS_TYPE_MODIFIERS))
				{
					markers.add(Markers.semanticError(this.superType.getPosition(), "class.extend.type",
					                                  ModifierUtil.classTypeToString(superAttributes.flags()),
					                                  superClass.getName()));
				}
				else if (superAttributes.hasFlag(Modifiers.FINAL))
				{
					markers.add(Markers.semanticError(this.superType.getPosition(), "class.extend.final",
					                                  superClass.getName()));
				}
			}
		}

		if (this.interfaces != null)
		{
			for (IType type : this.interfaces)
			{
				type.check(markers, context);

				if (!type.isResolved())
				{
					continue;
				}

				final IClass theClass = type.getTheClass();
				if (theClass == null)
				{
					continue;
				}

				if (!theClass.isInterface())
				{
					final String classType = ModifierUtil.classTypeToString(theClass.getAttributes().flags());
					markers.add(
						Markers.semanticError(this.position, "class.implement.type", classType, theClass.getName()));
				}
			}
		}

		this.metadata.check(markers, context);

		if (this.body != null)
		{
			this.body.check(markers, context);
		}

		context.pop();
	}

	public void checkFunctional(MarkerList markers)
	{
		final boolean hasAnnotation = this.getAttributes().getAnnotation(Types.FUNCTIONALINTERFACE_CLASS) != null;
		if (hasAnnotation && !this.isInterface())
		{
			// FunctionalInterface annotation on class or object
			markers.add(Markers.semanticError(this.position, "interface.functional.class"));
			return;
		}

		if (this.body == null)
		{
			if (hasAnnotation)
			{
				// No abstract method
				markers.add(Markers.semanticError(this.position, "interface.functional.not_found", this.name));
			}
			return;
		}

		// Partial copy in ExternalClass.getFunctionalMethod
		IMethod functionalMethod = null;
		for (IMethod method : this.body.allMethods())
		{
			if (!method.isFunctional())
			{
				continue;
			}

			if (functionalMethod == null)
			{
				functionalMethod = method;
				continue;
			}

			if (!hasAnnotation)
			{
				return;
			}

			// Multiple abstract methods
			markers.add(Markers.semanticError(this.position, "interface.functional.multiple", this.name));
			return;
		}

		if (functionalMethod != null)
		{
			this.metadata.setFunctionalMethod(functionalMethod);
			return;
		}

		if (hasAnnotation)
		{
			// No abstract method
			markers.add(Markers.semanticError(this.position, "interface.functional.not_found", this.name));
		}
	}

	@Override
	public void foldConstants()
	{
		this.attributes.foldConstants();

		if (this.typeParameters != null)
		{
			this.typeParameters.foldConstants();
		}

		this.parameters.foldConstants();

		if (this.superType != null)
		{
			this.superType.foldConstants();
		}

		if (this.interfaces != null)
		{
			this.interfaces.foldConstants();
		}

		this.metadata.foldConstants();

		if (this.body != null)
		{
			this.body.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.attributes.cleanup(compilableList, this);

		if (this.typeParameters != null)
		{
			this.typeParameters.cleanup(compilableList, this);
		}

		this.parameters.cleanup(compilableList, this);

		if (this.superType != null)
		{
			this.superType.cleanup(compilableList, this);
		}

		if (this.interfaces != null)
		{
			this.interfaces.cleanup(compilableList, this);
		}

		this.metadata.cleanup(compilableList, this);

		if (this.body != null)
		{
			this.body.cleanup(compilableList, this);
		}
	}

	@Override
	public String getFullName()
	{
		if (this.fullName != null)
		{
			return this.fullName;
		}
		if (this.enclosingClass != null)
		{
			return this.enclosingClass.getFullName() + '.' + this.name;
		}
		return this.fullName = this.unit.getFullName(this.name);
	}

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}
		if (this.enclosingClass != null)
		{
			return this.enclosingClass.getInternalName() + '$' + this.name;
		}
		return this.internalName = this.unit.getInternalName(this.name);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		// Header

		final boolean isInterface = this.isInterface();
		final String signature = this.getSignature();
		final String superClass = this.superType != null ? this.superType.getInternalName() : null;
		final String[] interfaces = this.getInterfaceArray();

		int javaFlags = ModifierUtil.getJavaFlags(this.attributes);
		final long dyvilFlags = ModifierUtil.getDyvilFlags(this.attributes);

		if (!isInterface)
		{
			javaFlags |= ASMConstants.ACC_SUPER;
		}
		writer.visit(ClassFormat.CLASS_VERSION, javaFlags, this.getInternalName(), signature, superClass, interfaces);

		// Source

		writer.visitSource(this.getHeader().getName() + DyvilFileType.DYVIL_EXTENSION, null);

		// Outer Class

		if (this.enclosingClass != null)
		{
			this.writeInnerClassInfo(writer);
		}

		// Annotations

		this.writeAnnotations(writer, dyvilFlags);

		// Super Types

		if (this.superType != null)
		{
			IClass iclass = this.superType.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}

		if (this.interfaces != null)
		{
			for (IType type : this.interfaces)
			{
				final IClass iclass = type.getTheClass();
				if (iclass != null)
				{
					iclass.writeInnerClassInfo(writer);
				}
			}
		}

		// Class Body

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].write(writer);
		}
		this.metadata.write(writer);

		this.writeClassParameters(writer);

		if (this.body != null)
		{
			this.body.write(writer);
		}

		this.metadata.writePost(writer);

		// Create the static <clinit> method
		MethodWriter initWriter = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.STATIC, "<clinit>", "()V",
		                                                                          null, null));
		initWriter.visitCode();
		this.writeStaticInit(initWriter);
		initWriter.visitEnd(Types.VOID);
	}

	private void writeClassParameters(ClassWriter writer) throws BytecodeException
	{
		final int parameterCount = this.parameters.size();
		if (parameterCount == 0)
		{
			return;
		}

		final AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.CLASS_PARAMETERS, false);
		final AnnotationVisitor arrayVisitor = annotationVisitor.visitArray("names");

		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			parameter.write(writer);
			arrayVisitor.visit("", parameter.getInternalName());
		}

		arrayVisitor.visitEnd();
		annotationVisitor.visitEnd();
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.hasModifier(Modifiers.INTERFACE)) // only generate trait init calls in classes
		{
			// for each inherited trait that is not inherited by our super-class, call the initializer.
			for (IClass traitClass : TraitMetadata.getNewTraits(this))
			{
				final String internal = traitClass.getInternalName();

				// Load 'this'
				writer.visitVarInsn(Opcodes.ALOAD, 0);

				// Invoke the static <traitinit> method of the trait class
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, internal, TraitMetadata.INIT_NAME, "(L" + internal + ";)V",
				                       true);
			}
		}

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeClassInit(writer);
		}

		this.metadata.writeClassInit(writer);

		if (this.body != null)
		{
			this.body.writeClassInit(writer);
		}

		this.metadata.writeClassInitPost(writer);
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeStaticInit(writer);
		}

		this.metadata.writeStaticInit(writer);

		if (this.body != null)
		{
			this.body.writeStaticInit(writer);
		}

		this.metadata.writeStaticInitPost(writer);
	}

	private void writeAnnotations(ClassWriter writer, long flags)
	{
		ModifierUtil.writeDyvilModifiers(writer, flags);

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		this.attributes.write(writer);

		// Type Variable Annotations
		if (this.typeParameters != null)
		{
			this.typeParameters.write(writer);
		}

		// Super Type Variable Annotations
		if (this.superType != null)
		{
			IType.writeAnnotations(this.superType, writer, TypeReference.newSuperTypeReference(-1), "");
		}
		if (this.interfaces != null)
		{
			for (int i = 0, size = this.interfaces.size(); i < size; i++)
			{
				IType.writeAnnotations(this.interfaces.get(i), writer, TypeReference.newSuperTypeReference(i), "");
			}
		}
	}

	private void writeTypes(DataOutput out) throws IOException
	{
		IType.writeType(this.superType, out);

		this.interfaces.write(out);
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		this.writeTypes(out);

		this.parameters.writeSignature(out);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		AttributeList.write(this.attributes, out);

		out.writeUTF(this.name.unqualified);

		this.writeTypes(out);

		this.parameters.write(out);
	}

	private void readTypes(DataInput in) throws IOException
	{
		this.superType = IType.readType(in);

		this.interfaces.read(in);
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.readTypes(in);

		this.parameters.readSignature(in);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.attributes = AttributeList.read(in);

		this.name = Name.read(in);

		this.readTypes(in);

		this.parameters = ParameterList.read(in);
	}
}
