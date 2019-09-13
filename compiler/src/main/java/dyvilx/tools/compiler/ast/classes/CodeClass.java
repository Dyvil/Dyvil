package dyvilx.tools.compiler.ast.classes;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.classes.metadata.TraitMetadata;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.external.ExternalHeader;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.TypeList;
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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class CodeClass extends AbstractClass
{
	protected ArgumentList  superConstructorArguments;
	protected AttributeList constructorAttributes;

	// Metadata
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
		this.setHeader(unit);
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
			this.metadata = IClassMetadata.getClassMetadata(this, this.attributes.flags());
		}

		this.attributes.resolveTypes(markers, context, this);
		ModifierChecks.checkModifiers(this, markers);

		this.metadata.resolveTypesAfterAttributes(markers, context);

		this.getTypeParameters().resolveTypes(markers, context);

		this.parameters.resolveTypes(markers, context);

		final IType unresolvedSuperType = this.getSuperType();
		if (unresolvedSuperType != null)
		{
			this.setSuperType(unresolvedSuperType.resolveType(markers, context));
		}

		this.getInterfaces().resolveTypes(markers, context);

		this.metadata.resolveTypesBeforeBody(markers, context);

		// This has to happen here because the metadata might add 'abstract' modifiers in resolveTypesHeader
		this.checkFunctional(markers);

		if (this.body != null)
		{
			this.body.resolveTypes(markers, context);
		}

		this.metadata.resolveTypesAfterBody(markers, context);

		this.metadata.resolveTypesGenerate(markers, context);

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.attributes.resolve(markers, context);

		this.getTypeParameters().resolve(markers, context);

		this.parameters.resolve(markers, context);

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.resolve(markers, context);
		}

		this.getInterfaces().resolve(markers, context);

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

		this.getTypeParameters().checkTypes(markers, context);

		final Set<IClass> checkedClasses = Collections.newSetFromMap(new IdentityHashMap<>());
		checkedClasses.add(this);
		this.checkSuperMethods(markers, this, this.getThisType(), checkedClasses);

		this.parameters.checkTypes(markers, context);

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.checkType(markers, context, TypePosition.SUPER_TYPE);
		}

		this.getInterfaces().checkTypes(markers, context, TypePosition.SUPER_TYPE);

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

		this.getTypeParameters().check(markers, context);

		this.parameters.check(markers, context);

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.check(markers, context);

			final IClass superClass;
			if (!this.hasModifier(Modifiers.EXTENSION) && (superClass = superType.getTheClass()) != null)
			{
				final AttributeList superAttributes = superClass.getAttributes();
				if (superAttributes.hasAnyFlag(Modifiers.CLASS_TYPE_MODIFIERS))
				{
					markers.add(Markers.semanticError(superType.getPosition(), "class.extend.type",
					                                  ModifierUtil.classTypeToString(superAttributes.flags()),
					                                  superClass.getName()));
				}
				else if (superAttributes.hasFlag(Modifiers.FINAL))
				{
					markers.add(Markers.semanticError(superType.getPosition(), "class.extend.final",
					                                  superClass.getName()));
				}
			}
		}

		for (IType type : this.getInterfaces())
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

		this.checkDuplicate(markers);

		this.metadata.check(markers, context);

		if (this.body != null)
		{
			this.body.check(markers, context);
		}

		context.pop();
	}

	protected void checkDuplicate(MarkerList markers)
	{
		final String internalName = this.getInternalName();

		if (this.enclosingClass != null)
		{
			for (IClass other : this.enclosingClass.getBody().getClasses())
			{
				if (this != other && internalName.equalsIgnoreCase(other.getInternalName()))
				{
					markers.add(Markers.semanticError(this.position, "class.descriptor.duplicate.nested", this.name,
					                                  this.enclosingClass.getName(), internalName));
					return;
				}
			}
		}
		if (this.enclosingHeader != null)
		{
			for (IClass other : this.enclosingHeader.getClasses())
			{
				if (this != other // don't match with itself
				    && internalName.equalsIgnoreCase(other.getInternalName())) // same descriptor
				{
					markers.add(Markers.semanticError(this.position, "class.descriptor.duplicate.header", this.name,
					                                  this.enclosingHeader.getName(), internalName));
					return;
				}
			}
		}
		if (this.enclosingPackage != null)
		{
			for (IHeaderUnit header : this.enclosingPackage.getHeaders())
			{
				if (header == this.enclosingHeader || header instanceof ExternalHeader)
				{
					// skip enclosing header and external headers
					continue;
				}

				for (IClass other : header.getClasses())
				{
					if (this == other // don't match with itself
					    || !internalName.equalsIgnoreCase(other.getInternalName())) // same descriptor
					{
						continue;
					}

					markers.add(Markers.semanticError(this.position, "class.descriptor.duplicate.package", this.name,
					                                  this.enclosingPackage.getFullName(), internalName));
					return;
				}
			}

			if (this.enclosingPackage.listExternalClassDescriptors().anyMatch(internalName::equalsIgnoreCase))
			{
				markers.add(Markers.semanticWarning(this.position, "class.descriptor.duplicate.external", this.name,
				                                    this.enclosingPackage.getFullName(), internalName));
				return;
			}
		}
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

		this.getTypeParameters().foldConstants();

		this.parameters.foldConstants();

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.foldConstants();
		}

		this.getInterfaces().foldConstants();

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

		this.getTypeParameters().cleanup(compilableList, this);

		this.parameters.cleanup(compilableList, this);

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.cleanup(compilableList, this);
		}

		this.getInterfaces().cleanup(compilableList, this);

		this.metadata.cleanup(compilableList, this);

		if (this.body != null)
		{
			this.body.cleanup(compilableList, this);
		}
	}

	// --------------- Compilation ---------------

	private String getSignature()
	{
		StringBuilder buffer = new StringBuilder();

		this.getTypeParameters().appendSignature(buffer);

		final IType superType = this.getSuperType();
		if (superType != null)
		{
			superType.appendSignature(buffer, false);
		}

		this.getInterfaces().appendDescriptors(buffer, IType.NAME_SIGNATURE);
		return buffer.toString();
	}

	// - - - - - - - - Interfaces - - - - - - - -

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		// Header

		final String signature = this.getSignature();
		final IType superType = this.getSuperType();
		final TypeList interfaces = this.getInterfaces();

		int javaFlags = this.getJavaFlags();
		final long dyvilFlags = this.getDyvilFlags();

		writer.visit(ClassFormat.CLASS_VERSION, javaFlags, this.getInternalName(), signature,
		             superType != null ? superType.getInternalName() : null,
		             interfaces.isEmpty() ? null : interfaces.getInternalTypeNames());

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

		if (superType != null)
		{
			IClass iclass = superType.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}

		for (IType type : interfaces)
		{
			final IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
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
		// Write DyvilName annotation if it differs from the mangled name
		if (!this.getInternalSimpleName().equals(this.name.qualified))
		{
			AnnotationUtil.writeDyvilName(writer, this.name.qualified);
		}

		ModifierUtil.writeDyvilModifiers(writer, flags);

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		this.attributes.write(writer);

		// Type Variable Annotations
		this.getTypeParameters().write(writer);

		// Super Type Variable Annotations
		final IType superType = this.getSuperType();
		if (superType != null)
		{
			IType.writeAnnotations(superType, writer, TypeReference.newSuperTypeReference(-1), "");
		}

		final TypeList interfaces = this.getInterfaces();
		for (int i = 0, size = interfaces.size(); i < size; i++)
		{
			IType.writeAnnotations(interfaces.get(i), writer, TypeReference.newSuperTypeReference(i), "");
		}
	}

	private void writeTypes(DataOutput out) throws IOException
	{
		IType.writeType(this.getSuperType(), out);

		this.getInterfaces().write(out);
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
		this.setSuperType(IType.readType(in));
		this.getInterfaces().read(in);
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
