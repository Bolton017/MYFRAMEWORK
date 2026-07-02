package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import http.HttpMethode;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String url();
    HttpMethode method();
}
