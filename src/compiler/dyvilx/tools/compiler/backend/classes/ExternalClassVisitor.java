package dyvilx.tools.compiler.backend.classes;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.asm.*;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.external.*;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.annotation.*;
import dyvilx.tools.compiler.backend.field.ExternalFieldVisitor;
import dyvilx.tools.compiler.backend.method.ExternalMethodVisitor;

import java.io.InputStream;

import static dyvilx.tools.compiler.backend.ClassFormat.*;

public class ExternalClassVisitor implements ClassVisitor
{
	public final ExternalClass theClass;
	public final List<String>  classParameters = new ArrayList<>();

	public ExternalClassVisitor(ExternalClass theClass)
	{
		this.theClass = theClass;
	}

	public static ExternalClass loadClass(DyvilCompiler compiler, ExternalClass externalClass, InputStream inputStream)
	{
		try
		{
			final dyvilx.tools.asm.ClassReader reader = new dyvilx.tools.asm.ClassReader(inputStream);
			final ExternalClassVisitor visitor = new ExternalClassVisitor(externalClass);

			reader.accept(visitor, dyvilx.tools.asm.ClassReader.SKIP_FRAMES);

			return externalClass;
		}
		catch (Throwable ex)
		{
			compiler.error("ExternalClassVisitor", "loadClass", ex);
		}

		return null;
	}

	// ------------------------------ ClassVisitor Implementation ------------------------------

	// --------------- Class ---------------

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.theClass.setJavaFlags(access);
		this.theClass.setInternalName(name);

		this.theClass.setBody(new ClassBody(this.theClass));
		if (interfaces != null)
		{
			this.theClass.setInterfaces(new TypeList(interfaces.length));
		}

		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.theClass.setName(Name.fromQualified(name));
			this.theClass.setFullName(name);
			this.theClass.setPackage(Package.rootPackage);
		}
		else
		{
			this.theClass.setName(Name.fromQualified(name.substring(index + 1)));
			// Do not set 'fullName' here
			this.theClass.setPackage(Package.rootPackage.resolveGlobalPackage(name.substring(0, index)));
		}

		if (signature != null)
		{
			ClassFormat.readClassSignature(signature, this.theClass);
		}
		else
		{
			this.theClass.setSuperType(superName != null ? ClassFormat.internalToType(superName) : null);

			if (interfaces != null)
			{
				for (String internal : interfaces)
				{
					this.theClass.getInterfaces().add(ClassFormat.internalToType(internal));
				}
			}
		}
	}

	@Override
	public void visitSource(String source, String debug)
	{
	}

	// --------------- Outer and Inner Classes ---------------

	@Override
	public void visitOuterClass(String owner, String name, String desc)
	{
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		if (innerName == null || !this.theClass.getInternalName().equals(outerName))
		{
			return;
		}

		this.theClass.getInnerTypeNames().put(innerName, name);
	}

	// --------------- Attributes and Annotations ---------------

	@Override
	public void visitAttribute(Attribute attr)
	{
	}

	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		switch (type)
		{
		case AnnotationUtil.DYVIL_NAME:
			return new DyvilNameVisitor(this.theClass);
		case ModifierUtil.DYVIL_MODIFIERS:
			return new DyvilModifiersVisitor(this.theClass);
		case AnnotationUtil.CLASS_PARAMETERS:
			return new ClassParameterAnnotationVisitor(this);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (!this.theClass.skipAnnotation(internal, null))
		{
			Annotation annotation = new ExternalAnnotation(ClassFormat.internalToType(internal));
			return new AnnotationReader(annotation, this.theClass.annotationConsumer());
		}
		return null;
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.CLASS_EXTENDS:
		{
			final int index = TypeReference.getSuperTypeIndex(typeRef);
			if (index < 0)
			{
				this.theClass.setSuperType(IType.withAnnotation(this.theClass.getSuperType(), annotation, typePath));
			}
			else
			{
				final TypeList interfaces = this.theClass.getInterfaces();
				interfaces.set(index, IType.withAnnotation(interfaces.get(index), annotation, typePath));
			}
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.theClass.getTypeParameters()
			                                      .get(TypeReference.getTypeParameterIndex(typeRef));
			if (!typeVar.skipAnnotation(annotation.getTypeDescriptor(), annotation))
			{
				return null;
			}

			typeVar.getAttributes().add(annotation);
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.theClass.getTypeParameters()
			                                      .get(TypeReference.getTypeParameterIndex(typeRef));
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		}
		return new AnnotationReader(annotation);
	}

	// --------------- Members ---------------

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		IType type = ClassFormat.readFieldType(signature == null ? desc : signature);

		if (this.classParameters.contains(name))
		{
			final ClassParameter param = new ExternalClassParameter(this.theClass, Name.fromQualified(name), desc, type);
			param.setJavaFlags(access);
			this.theClass.getParameters().add(param);
			return new ExternalFieldVisitor(param);
		}

		final ExternalField field = new ExternalField(this.theClass, Name.fromQualified(name), desc, type);
		field.setJavaFlags(access);

		if (value != null)
		{
			field.setConstantValue(value);
		}

		this.theClass.getBody().addDataMember(field);

		return new ExternalFieldVisitor(field);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			return null;
		}

		switch (name)
		{
		case "<clinit>":
			return null;
		case "<init>":
			if (this.theClass.hasModifier(Modifiers.ENUM))
			{
				return null;
			}

			final ExternalConstructor ctor = new ExternalConstructor(this.theClass);
			ctor.setJavaFlags(access);

			if (signature != null)
			{
				readConstructorType(signature, ctor);
			}
			else
			{
				readConstructorType(desc, ctor);

				if (exceptions != null)
				{
					readExceptions(exceptions, ctor.getExceptions());
				}
			}

			if ((access & Modifiers.ACC_VARARGS) != 0)
			{
				final ParameterList parameterList = ctor.getExternalParameterList();
				parameterList.get(parameterList.size() - 1).setVarargs();
			}

			this.theClass.getBody().addConstructor(ctor);

			return new ExternalMethodVisitor(ctor);
		}

		if (this.theClass.isAnnotation() && (access & Modifiers.STATIC) == 0)
		{
			final ClassParameter param = new ExternalClassParameter(this.theClass, Name.fromQualified(name),
			                                                        desc.substring(2), readReturnType(desc));
			param.setJavaFlags(access);
			this.theClass.getParameters().add(param);
			return new AnnotationClassVisitor(param);
		}

		final ExternalMethod method = new ExternalMethod(this.theClass, name, desc, signature);
		method.setJavaFlags(access);

		if (signature != null)
		{
			readMethodType(signature, method);
		}
		else
		{
			readMethodType(desc, method);

			if (exceptions != null)
			{
				readExceptions(exceptions, method.getExceptions());
			}
		}

		if ((access & Modifiers.ACC_VARARGS) != 0)
		{
			final ParameterList parameterList = method.getExternalParameterList();
			parameterList.get(parameterList.size() - 1).setVarargs();
		}

		this.theClass.getBody().addMethod(method);
		return new ExternalMethodVisitor(method);
	}

	// --------------- End ---------------

	@Override
	public void visitEnd()
	{
	}
}
