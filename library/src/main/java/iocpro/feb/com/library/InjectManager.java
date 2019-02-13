package iocpro.feb.com.library;

import android.app.Activity;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by lilichun on 2019/2/13.
 */
public class InjectManager {
    public static void inject(Activity activity) {
        injectLayout(activity);
        injectViews(activity);
        injectEvents(activity);
    }

    public static void injectLayout(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        //获取InjectContentView注解
        InjectContentView contentview = clazz.getAnnotation(InjectContentView.class);
        if (contentview != null) {
            int layout = contentview.value();
            try {
                //获取setContentView方法
                Method method = clazz.getMethod("setContentView", int.class);
                method.setAccessible(true);
                method.invoke(activity, layout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void injectViews(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        //获取所有属性
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            InjectView injectview = field.getAnnotation(InjectView.class);
            if (injectview != null) {
                try {
                    int viewId = injectview.value();
                    Method method = clazz.getMethod("findViewById", int.class);
                    Object view = method.invoke(activity, viewId);
                    field.setAccessible(true);
                    field.set(activity, view);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void injectEvents(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            //拿到方法的所有注解
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                //拿到注解的注解
                EventBase eventBaseAnnotation = annotationType.getAnnotation(EventBase.class);
                if (eventBaseAnnotation != null) {
                    String listenerSetter = eventBaseAnnotation.listenerSetter();
                    Class<?> listenerType = eventBaseAnnotation.listenerType();
                    String methodName = eventBaseAnnotation.methodName();

                    try {
                        ClickInvocationHandler handler = new ClickInvocationHandler(activity);
                        handler.addMethod(methodName, method);
                        Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, handler);
                        Method value = annotationType.getDeclaredMethod("value");
                        int[] viewIds = (int[]) value.invoke(annotation, null);
                        for (int viewid : viewIds) {
                            View view = activity.findViewById(viewid);
                            Method setEventListenerMethod = view.getClass().getMethod(listenerSetter, listenerType);
                            setEventListenerMethod.invoke(view, listener);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
