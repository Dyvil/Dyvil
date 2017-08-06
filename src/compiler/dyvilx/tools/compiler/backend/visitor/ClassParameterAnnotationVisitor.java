package dyvilx.tools.compiler.backend.visitor;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.external.ExternalClass;

public class ClassParameterAnnotationVisitor implements AnnotationVisitor
{
	private ExternalClass externalClass;

	public ClassParameterAnnotationVisitor(ExternalClass externalClass)
	{
		this.externalClass = externalClass;
	}

	@Override
	public void visit(String name, Object value)
	{
	}

	@Override
	public void visitEnum(String name, String desc, String value)
	{

	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		return null;
	}

	@Override
	public AnnotationVisitor visitArray(String name)
	{
		if (!"names".equals(name))
		{
			return null;
		}

		return new AnnotationVisitor()
		{
			private String[] classParameters = new String[4];
			private int classParameterCount;

			@Override
			public void visit(String name, Object value)
			{
				if (value instanceof String)
				{
					int index = this.classParameterCount++;
					if (index >= this.classParameters.length)
					{
						String[] temp = new String[this.classParameters.length << 1];
						System.arraycopy(this.classParameters, 0, temp, 0, index);
						this.classParameters = temp;
					}
					this.classParameters[index] = (String) value;
				}
			}

			@Override
			public void visitEnum(String name, String desc, String value)
			{

			}

			@Override
			public AnnotationVisitor visitAnnotation(String name, String desc)
			{
				return null;
			}

			@Override
			public AnnotationVisitor visitArray(String name)
			{
				return null;
			}

			@Override
			public void visitEnd()
			{
				// Create a trimmed copy
				final String[] classParameters = new String[this.classParameterCount];
				System.arraycopy(this.classParameters, 0, classParameters, 0, this.classParameterCount);
				ClassParameterAnnotationVisitor.this.externalClass.setClassParameters(classParameters);
			}
		};
	}

	@Override
	public void visitEnd()
	{
	}
}
