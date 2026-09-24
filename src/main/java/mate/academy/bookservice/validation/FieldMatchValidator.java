package mate.academy.bookservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String first;
    private String second;

    @Override
    public void initialize(FieldMatch annotation) {
        first = annotation.first();
        second = annotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Method firstAccessor = value.getClass().getMethod(first);
            Method secondAccessor = value.getClass().getMethod(second);
            if (Objects.equals(firstAccessor.invoke(value), secondAccessor.invoke(value))) {
                return true;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Invalid @FieldMatch property", e);
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(second)
                .addConstraintViolation();
        return false;
    }
}
