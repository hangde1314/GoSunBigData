package com.hzgc.datasyncer.esearch;

import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface CustomField {
    CustomFieldType type() default CustomFieldType.Auto;

    boolean index() default true;

    DateFormat format() default DateFormat.none;

    String pattern() default "";

    boolean store() default false;

    boolean fielddata() default false;

    String searchAnalyzer() default "";

    String analyzer() default "";

    String normalizer() default "";

    String[] ignoreFields() default {};

    boolean includeInParent() default false;

    String[] copyTo() default {};
}
