package dyvil.tools.compiler.ast.method;

import dyvil.collection.Set;
import dyvil.collection.mutable.HashSet;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeMethod extends AbstractMethod
{
	protected IValue value;

	// Metadata
	protected Set<IMethod> overrideMethods;

	public CodeMethod(IClass iclass)
	{
		super(iclass);
	}

	public CodeMethod(IClass iclass, Name name)
	{
		super(iclass, name);
	}

	public CodeMethod(IClass iclass, Name name, IType type)
	{
		super(iclass, name, type);
	}

	public CodeMethod(IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(iclass, name, type, modifiers);
	}

	public CodeMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolveTypes(markers, context);

		this.unmangleName();

		if (this.receiverType != null)
		{
			this.receiverType = this.receiverType.resolveType(markers, context);

			// Check the self type for compatibility
			final IClass selfTypeClass = this.receiverType.getTheClass();
			if (selfTypeClass != null && selfTypeClass != this.enclosingClass)
			{
				final Marker marker = Markers.semanticError(this.receiverType.getPosition(),
				                                            "method.receivertype.incompatible", this.getName());
				marker.addInfo(Markers.getSemantic("method.receivertype", this.receiverType));
				marker.addInfo(Markers.getSemantic("method.classtype", this.enclosingClass.getFullName()));
				markers.add(marker);
			}
		}
		else if (!this.isStatic())
		{
			this.receiverType = this.enclosingClass.getType();
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(markers, context);
		}

		this.parameters.resolveTypes(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT | Modifiers.PUBLIC);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT);
		}

		context.pop();
	}

	private void unmangleName()
	{
		final Name name = this.name;
		final String unqualified = name.unqualified;
		final int index = unqualified.indexOf(NAME_SEPARATOR);

		if (index < 0)
		{
			return;
		}

		final String qualified = name.qualified;
		final String newUnqualified = unqualified.substring(0, index);
		final String newQualified = qualified.substring(0, qualified.indexOf(NAME_SEPARATOR));
		this.mangledName = qualified;
		this.name = Name.from(newUnqualified, newQualified);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolve(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, context);
		}

		if (this.receiverType != null)
		{
			this.receiverType.resolve(markers, context);
		}

		this.parameters.resolve(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, context);
		}

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			boolean inferType = false;
			if (this.type == Types.UNKNOWN || this.type == null)
			{
				inferType = true;
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN && this.value.isResolved())
				{
					markers.add(Markers.semantic(this.position, "method.type.infer", this.name.unqualified));
				}
			}

			final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("method.type.incompatible",
			                                                                             "method.type", "value.type",
			                                                                             this.name);
			this.value = TypeChecker.convertValue(this.value, this.type, this.type, markers, context, markerSupplier);

			if (inferType)
			{
				this.type = this.value.getType();
			}
		}
		else if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		if (this.receiverType != null)
		{
			this.receiverType.checkType(markers, context, TypePosition.PARAMETER_TYPE);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, context);
		}

		this.parameters.checkTypes(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, context, TypePosition.RETURN_TYPE);
		}

		if (this.value != null)
		{
			this.value.resolveStatement(this, markers);
			this.value.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.check(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, this);
		}

		if (this.receiverType != null)
		{
			this.receiverType.check(markers, context);
		}

		this.parameters.check(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, this);

			if (!Types.isSuperType(Types.THROWABLE, exceptionType))
			{
				Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
			}
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}

		ModifierList.checkMethodModifiers(markers, this, this.modifiers.toFlags());

		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			this.checkOverrideMethods(markers);
		}

		// Check for duplicate methods
		this.checkDuplicates(markers);

		context.pop();
	}

	private void checkDuplicates(MarkerList markers)
	{
		final IClassBody body = this.enclosingClass.getBody();
		if (body == null)
		{
			return;
		}

		final String descriptor = this.getDescriptor();
		final String signature = this.getSignature();
		final int parameterCount = this.parameters.size();

		String mangledName = this.getMangledName();
		boolean thisMangled = mangledName.contains(NAME_SEPARATOR);

		for (int i = body.methodCount() - 1; i >= 0; i--)
		{
			final IMethod method = body.getMethod(i);
			if (method == this || method.getName() != this.name // common cases
				    || method.getParameterList().size() != parameterCount // optimization
				    || !method.getDescriptor().equals(descriptor))
			{
				continue;
			}

			final String otherMangledName = method.getMangledName();
			if (!mangledName.equals(otherMangledName))
			{
				continue;
			}

			// Name mangling required

			if (!thisMangled)
			{
				// ensure this method gets name-mangled
				this.mangledName = mangledName = createMangledName(this);
				thisMangled = true;

			}

			if (mangledName.equals(otherMangledName))
			{
				// also true if this.getSignature equals method.getSignature
				markers.add(Markers.semanticError(this.position, "method.duplicate", this.name, signature));
			}
		}
	}

	private static String createMangledName(IMethod method)
	{
		final String qualifiedName = method.getName().qualified;
		final String signature = method.getSignature();

		final StringBuilder builder = new StringBuilder(qualifiedName.length() + NAME_SEPARATOR.length() + signature
			                                                                                                   .length());
		builder.append(qualifiedName).append(NAME_SEPARATOR);

		for (int i = 0, length = signature.length(); i < length; i++)
		{
			// Replace special chars with dollar signs
			final char c = signature.charAt(i);
			switch (c)
			{
			case '(':
			case ')':
				// strip opening and closing paren
				continue;
			case '<':
				if (i == 0)
				{
					// strip opening angle bracket if at first position
					continue;
				}
				builder.append("$_");
				continue;
			case '>':
				if (signature.charAt(i + 1) == ';')
				{
					// the next token is a semicolon, so '_$' will be appended
					builder.append('_');
					continue;
				}
				// double separator between type and value parameter lists
				builder.append("__");
				continue;
			case '+':
			case ';':
			case ':':
			case '-':
			case '*':
				builder.append('$');
				continue;
			case '/':
				builder.append('_');
				continue;
			default:
				builder.append(c);
			}
		}
		return builder.toString();
	}

	@Override
	public void addOverride(IMethod candidate)
	{
		if (!this.enclosingClass.isSubClassOf(candidate.getEnclosingClass().getClassType()))
		{
			return;
		}

		if (this.overrideMethods == null)
		{
			this.overrideMethods = new IdentityHashSet<>();
		}
		this.overrideMethods.add(candidate);
	}

	@Override
	protected boolean checkOverride0(IMethod candidate)
	{
		return this.overrideMethods != null && this.overrideMethods.contains(candidate);
	}

	private void checkOverrideMethods(MarkerList markers)
	{
		if (this.overrideMethods == null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
			{
				markers.add(Markers.semantic(this.position, "method.override.notfound", this.name));
			}
			return;
		}

		if (!this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(Markers.semantic(this.position, "method.overrides", this.name));
		}

		final boolean thisTypeResolved = this.type.isResolved();
		final int accessLevel = this.getAccessLevel() & ~Modifiers.INTERNAL;
		final ITypeContext typeContext = this.enclosingClass.getType();

		for (IMethod overrideMethod : this.overrideMethods)
		{
			final ModifierSet modifiers = overrideMethod.getModifiers();

			// Final Modifier Check
			if (modifiers.hasIntModifier(Modifiers.FINAL))
			{
				markers.add(Markers.semanticError(this.position, "method.override.final", this.name));
			}

			// Visibility Check

			switch (modifiers.toFlags() & Modifiers.VISIBILITY_MODIFIERS)
			{
			case Modifiers.PRIVATE:
				markers.add(Markers.semanticError(this.position, "method.override.private", this.name));
				break;
			case Modifiers.PROTECTED:
				if (accessLevel != Modifiers.PUBLIC && accessLevel != Modifiers.PROTECTED)
				{
					markers.add(Markers.semanticError(this.position, "method.override.protected", this.name));
				}
				break;
			case Modifiers.PUBLIC:
				if (modifiers.hasIntModifier(Modifiers.PUBLIC) && accessLevel != Modifiers.PUBLIC)
				{
					markers.add(Markers.semanticError(this.position, "method.override.public", this.name));
				}
				break;
			}

			// Type Compatibility Check

			if (!thisTypeResolved)
			{
				continue;
			}

			final IType superReturnType = overrideMethod.getType().getConcreteType(typeContext);
			if (superReturnType != this.type && superReturnType.isResolved() // avoid extra error
				    && !Types.isSuperType(superReturnType.asParameterType(), this.type))
			{
				final Marker marker = Markers
					                      .semanticError(this.position, "method.override.type.incompatible", this.name);
				marker.addInfo(Markers.getSemantic("method.type", this.type));
				marker.addInfo(Markers.getSemantic("method.override.type", superReturnType));

				marker.addInfo(Markers.getSemantic("method.override",
				                                   Util.methodSignatureToString(overrideMethod, typeContext),
				                                   overrideMethod.getEnclosingClass().getFullName()));
				markers.add(marker);
			}
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].foldConstants();
		}

		if (this.receiverType != null)
		{
			this.receiverType.foldConstants();
		}

		this.parameters.foldConstants();

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].foldConstants();
		}

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		context = context.push(this);

		super.cleanup(context, compilableList);

		if (this.annotations != null)
		{
			IAnnotation intrinsic = this.annotations.getAnnotation(Types.INTRINSIC_CLASS);
			if (intrinsic != null)
			{
				this.intrinsicData = Intrinsics.readAnnotation(this, intrinsic);
			}
		}

		if (this.receiverType != null)
		{
			this.receiverType.cleanup(context, compilableList);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(context, compilableList);
		}

		this.parameters.cleanup(context, compilableList);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(context, compilableList);
		}

		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}

		context.pop();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final boolean interfaceClass = this.enclosingClass.isInterface();

		int modifiers = this.modifiers.toFlags();
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		if (interfaceClass)
		{
			modifiers = modifiers & ~3 | Modifiers.PUBLIC;
		}

		final String ownerClassName = this.enclosingClass.getInternalName();
		final String mangledName = this.getMangledName();
		final String descriptor = this.getDescriptor();
		final String signature = this.needsSignature() ? this.getSignature() : null;
		final String[] exceptionTypes = this.getInternalExceptions();

		MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
			modifiers & ModifierUtil.JAVA_MODIFIER_MASK, mangledName, descriptor, signature, exceptionTypes));

		if ((modifiers & Modifiers.STATIC) == 0)
		{
			methodWriter.setThisType(ownerClassName);
		}

		this.writeAnnotations(methodWriter, modifiers);

		this.parameters.writeInit(methodWriter);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].writeParameter(methodWriter);
		}

		final Label start = new Label();
		final Label end = new Label();

		if (this.value != null)
		{
			methodWriter.visitCode();
			methodWriter.visitLabel(start);
			this.value.writeExpression(methodWriter, this.type);
			methodWriter.visitLabel(end);
			methodWriter.visitEnd(this.type);
		}

		this.parameters.writeLocals(methodWriter, start, end);

		if ((modifiers & Modifiers.STATIC) != 0)
		{
			return;
		}

		methodWriter.visitLocalVariable("this", 'L' + ownerClassName + ';', null, start, end, 0);

		if (this.overrideMethods == null)
		{
			return;
		}

		final int lineNumber = this.getLineNumber();
		final int opcode =
			(modifiers & Modifiers.ABSTRACT) != 0 && interfaceClass ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

		/**
		 * Contains entries in the format 'mangledName(paramTypes)returnType'
		 * Used to ensure unique bridge methods
		 */
		final Set<String> descriptors = new HashSet<>(1 + this.overrideMethods.size());
		descriptors.add(mangledName + descriptor);

		for (IMethod overrideMethod : this.overrideMethods)
		{
			final String overrideDescriptor = overrideMethod.getDescriptor();
			final String overrideMangledName = overrideMethod.getMangledName();
			final String overrideEntry = overrideMangledName + overrideDescriptor;

			// Check if a bridge method for the descriptor has not yet been
			// generated
			if (descriptors.contains(overrideEntry))
			{
				continue;
			}
			descriptors.add(overrideEntry);

			// Generate a bridge method
			methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
				Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, overrideMangledName, overrideDescriptor,
				null, exceptionTypes));

			methodWriter.visitCode();
			methodWriter.setThisType(ownerClassName);

			methodWriter.visitVarInsn(Opcodes.ALOAD, 0);

			final IParameterList overrideParameterList = overrideMethod.getParameterList();

			for (int p = 0, count = this.parameters.size(); p < count; p++)
			{
				final IParameter overrideParameter = overrideParameterList.get(p);
				final IType parameterType = this.parameters.get(p).getInternalType();
				final IType overrideParameterType = overrideParameter.getInternalType();

				overrideParameter.writeInit(methodWriter);
				methodWriter.visitVarInsn(overrideParameterType.getLoadOpcode(), overrideParameter.getLocalIndex());
				overrideParameterType.writeCast(methodWriter, parameterType, lineNumber);
			}

			IType overrideReturnType = overrideMethod.getType();

			methodWriter.visitLineNumber(lineNumber);
			methodWriter.visitMethodInsn(opcode, ownerClassName, mangledName, descriptor, interfaceClass);
			this.type.writeCast(methodWriter, overrideReturnType, lineNumber);
			methodWriter.visitInsn(overrideReturnType.getReturnOpcode());
			methodWriter.visitEnd();
		}
	}

	private boolean needsSignature()
	{
		return this.typeParameterCount != 0 || this.type.isGenericType() || this.type.hasTypeVariables()
			       || this.parameters.needsSignature();
	}

	protected void writeAnnotations(MethodWriter writer, int modifiers)
	{
		if (this.annotations != null)
		{
			this.annotations.write(writer);
		}

		if (this.receiverType != null && this.receiverType != this.enclosingClass.getType())
		{
			final String signature = this.receiverType.getSignature();
			if (signature != null)
			{
				AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.RECEIVER_TYPE, false);
				annotationVisitor.visit("value", signature);
				annotationVisitor.visitEnd();
			}
		}

		ModifierUtil.writeModifiers(writer, this.modifiers);

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		// Type Variable Annotations
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].write(writer);
		}

		this.type.writeAnnotations(writer, TypeReference.newTypeReference(TypeReference.METHOD_RETURN), "");
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].writeAnnotations(writer, TypeReference.newExceptionReference(i), "");
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		this.parameters.writeSignature(out);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.writeAnnotations(out);

		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		this.parameters.write(out);
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.type = IType.readType(in);

		this.parameters.readSignature(in);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);

		this.name = Name.from(in.readUTF());
		this.type = IType.readType(in);
		this.parameters = ParameterList.read(in);
	}
}
