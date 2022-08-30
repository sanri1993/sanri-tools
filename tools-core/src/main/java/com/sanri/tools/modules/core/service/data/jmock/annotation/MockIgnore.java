package com.sanri.tools.modules.core.service.data.jmock.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MockIgnore {

}