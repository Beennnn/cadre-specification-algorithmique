package method.spec;

import java.lang.annotation.*;

/**
 * Links a piece of code to the specification items it implements.
 *
 * <pre>
 *     &#64;ImplementsSpec("RG-030")
 *     BigDecimal averageSpeed(...) { ... }
 * </pre>
 *
 * <p>This is not documentation: it is the only machine-readable link between the
 * specification and the code. The coverage tool reads it to answer a question no green
 * test suite answers on its own — <em>is every item of the specification implemented, and
 * does every piece of code trace back to something the business asked for?</em>
 *
 * <p>Retention is {@code SOURCE}: the tool reads the source files, so nothing is carried
 * into the bytecode and no runtime dependency is created.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ImplementsSpec {

    /** Identifiers of the covered items: rules RG-xxx, invariants INV-xx, errors E-XXX-xxx. */
    String[] value();

    /** The specification that owns them. Empty means the one of the example it lives in. */
    String spec() default "";
}
