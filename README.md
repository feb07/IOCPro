# IOCPro
IOC依赖注入
github地址：https://github.com/feb07/IOCPro

###1、概述
什么叫IOC，控制反转（Inversion of Control，英文缩写为IOC）。IOC框架也叫控制反转框架，依赖注入框架。IOC的核心是解耦，解耦的目的是修改耦合对象时不影响另外的对象，降低关联性，从Spring来看，在Spring中IOC更多的依赖的是xml配置，而Android的IOC不使用xml配置，使用注解+反射。一个类里面有很多个成员变量，传统的写法，要用这些成员变量，就new 出来用。IOC的原则是：不要new，这样耦合度太高；配置个xml文件，里面标明哪个类，里面用了哪些成员变量，等待加载这个类的时候，注入（new）进去；

###2、实现
这里的Android IOC框架，主要是帮大家注入所有的控件，布局文件，点击事件。

举个例子，一个activity有十几个view，传统做法是设置布局文件，然后一个个findViewById。现在的做法，Activity类上添加个注解，帮我们自动注入布局文件；声明View的时候，添加一行注解，然后自动帮我们findViewById。

###3、编码
######1)注入布局文件

定义注解
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectContentView {
    int value();
}
```
InjectContentView用于在类上使用，主要用于标明该Activity需要使用的布局文件。
```
@InjectContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
}
```
简单说下注解，关键字@interface @Target @Retention。
@Target表示该注解可以用于什么地方，可能的类型TYPE（类）,FIELD（成员变量）,可能的类型：
```
public enum ElementType {
    /** Class, interface (including annotation type), or enum declaration */
    TYPE,

    /** Field declaration (includes enum constants) */
    FIELD,

    /** Method declaration */
    METHOD,

    /** Formal parameter declaration */
    PARAMETER,

    /** Constructor declaration */
    CONSTRUCTOR,

    /** Local variable declaration */
    LOCAL_VARIABLE,

    /** Annotation type declaration */
    ANNOTATION_TYPE,

    /** Package declaration */
    PACKAGE,

    /**
     * Type parameter declaration
     *
     * @since 1.8
     */
    TYPE_PARAMETER,

    /**
     * Use of a type
     *
     * @since 1.8
     */
    TYPE_USE
}
```
@Retention表示：表示需要在什么级别保存该注解信息；我们这里设置为运行时。

可能的类型：
```
public enum RetentionPolicy {
    /**
     * Annotations are to be discarded by the compiler.
     */
    SOURCE,

    /**
     * Annotations are to be recorded in the class file by the compiler
     * but need not be retained by the VM at run time.  This is the default
     * behavior.
     */
    CLASS,

    /**
     * Annotations are to be recorded in the class file by the compiler and
     * retained by the VM at run time, so they may be read reflectively.
     *
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME
}
```
注入布局文件代码，通过获取注解的值，反射setContentView方法
```
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
```
######2）注入view
定义注解
```
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView {
    int value();
}
```
```
@InjectContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.btn)
    private Button btn;

    @InjectView(R.id.tv)
    private TextView tv;
}
```
注入view，获取所有的类的属性变量，通过获取注解的值，反射findViewById方法，设置给属性
```
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
```
######3）点击事件注入
正常写法:有三个要素，listener绑定方法setOnClickListener，事件类型View.OnClickListener，事件回调方法onClick
```
btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "today is 2019-2-13", Toast.LENGTH_LONG).show();
            }
        });
```
定义注解
```
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBase {
    String listenerSetter();//setOnClickListener方法

    Class<?> listenerType();//View.OnClickListener

    String methodName();//onclick回调方法
}
```
```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(listenerSetter = "setOnClickListener", listenerType = View.OnClickListener.class, methodName = "onClick")
public @interface InjectClick {
    int[] value();
}
```
如果是onclick事件，可以按以上的写法，如果是onitemclick，只需要修改@EventBase三要素的value的值。同理可IOC注入Recyclerview、listview的onitemclick事件。
注入代码：涉及到动态方法代理，即用自定义方法clickMethod(View v)，代理了原本的onclick(View v)方法， 需要注意的是代理方法的入参需要与被代理方法的入参一致。
```
@InjectClick(R.id.btn)
    public void clickMethod(View view) {
        Toast.makeText(MainActivity.this, "today is 2019-2-13", Toast.LENGTH_LONG).show();
    }
```
```
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
```
java动态代理相关：Proxy与InvocationHandler
```
public class ClickInvocationHandler implements InvocationHandler {
    private Object target;
    private HashMap<String, Method> methodHashMap = new HashMap<>();

    public ClickInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (target != null) {
            method = methodHashMap.get(methodName);
            if (method != null) {
                return method.invoke(target, args);
            }
        }
        return null;
    }

    public void addMethod(String methodName, Method method) {
        methodHashMap.put(methodName, method);
    }
}
```
附上完整的InjectManager代码，以及BaseActivity代码
```
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
```
```
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectManager.inject(this);
    }
}
```
MainActivity代码：可以看出在activity中，布局文件，view，点击事件的注入，使代码更简洁
```
@InjectContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.btn)
    private Button btn;

    @InjectView(R.id.tv)
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @InjectClick(R.id.btn)
    public void clickMethod(View view) {
        Toast.makeText(MainActivity.this, "today is 2019-2-13", Toast.LENGTH_LONG).show();
    }
}
```
####4、github地址
[https://github.com/feb07/IOCPro](https://github.com/feb07/IOCPro)


