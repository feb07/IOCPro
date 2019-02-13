package iocpro.feb.com.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lilichun on 2019/2/13.
 * onclick事件三要素
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBase {
    String listenerSetter();//setOnClickListener方法

    Class<?> listenerType();//View.OnClickListener

    String methodName();//onclick回调方法
}
