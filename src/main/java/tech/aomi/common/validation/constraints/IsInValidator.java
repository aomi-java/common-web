
package tech.aomi.common.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;


/**
 * is
 */
public class IsInValidator implements ConstraintValidator<IsIn, Object> {

    private List<String> value;

    @Override
    public void initialize(IsIn isIn) {
        value = Arrays.asList(isIn.value());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return this.value.contains(value.toString());
    }
}
