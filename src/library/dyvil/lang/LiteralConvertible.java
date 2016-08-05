package dyvil.lang;

public interface LiteralConvertible
{
	// @formatter:off
	@interface FromArray { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromBoolean { String methodName() default "apply"; }
	// @formatter:on

	// @formatter:off
	@interface FromChar { String methodName() default "apply"; }
	// @formatter:on

	// @formatter:off
	@interface FromClass { String methodName() default "apply"; }
	// @formatter:on

	// @formatter:off
	@interface FromColonOperator { String methodName() default "apply";  }
	//@formatter:on

	// @formatter:off
	@interface FromDouble { String methodName() default "apply";  }
	//@formatter:on

	// @formatter:off
	@interface FromFloat { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromInt { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromLong { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromMap { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromNil { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromString { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromStringInterpolation { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromTuple { String methodName() default "apply"; }
	//@formatter:on

	// @formatter:off
	@interface FromType { String methodName() default "apply"; }
	//@formatter:on
}
