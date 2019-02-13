package iocpro.feb.com.library;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * Created by lilichun on 2019/2/13.
 * 点击事件代理
 */
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
