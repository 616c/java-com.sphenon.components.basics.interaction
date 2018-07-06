package com.sphenon.basics.interaction;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.expression.*;
import com.sphenon.basics.debug.*;
import com.sphenon.basics.system.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class AnchorInterceptor implements Interceptor {
    static final public Class _class = AnchorInterceptor.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

    protected AnchorInterceptor(CallContext context, Object target, Workspace workspace, Transaction transaction) {
        this.target = target;
        this.workspace = workspace;
        this.transaction = transaction;
    }

    protected Object target;
    protected Workspace workspace;
    protected Transaction transaction;

    static protected boolean initialised;

    static public<TargetType> TargetType wrap(CallContext context, TargetType instance, Workspace workspace, Transaction transaction) {

        if (initialised == false) {
            try {
                METHOD_Anchor_getWorkspace   = Anchor.class.getMethod("_getWorkspace", CallContext.class);
                METHOD_Anchor_setWorkspace   = Anchor.class.getMethod("_setWorkspace", CallContext.class, Workspace.class);
                METHOD_Anchor_getTransaction = Anchor.class.getMethod("_getTransaction", CallContext.class);
                METHOD_Anchor_setTransaction = Anchor.class.getMethod("_setTransaction", CallContext.class, Transaction.class);
                METHOD_Anchor_getDelegate    = Anchor.class.getMethod("_getDelegate", CallContext.class);

                METHOD_Object_hashCode       = Object.class.getMethod("hashCode");;
                METHOD_Object_equals         = Object.class.getMethod("equals", Object.class);;
            } catch (java.lang.NoSuchMethodException nsme) {
                // stimmt nicht
            }
            initialised = true;
        }

        transaction = (instance instanceof Transaction ? (Transaction) instance : transaction);

        // For workspaces, this could be done analogously; this would require
        // to remove the explicit anchoring in workspaces (EditorSpace etc. - the code
        // can be found in UMLAttribute.ocp, search: AfterGetBody/Achorable as well
        // as in EditorSpace.model, search: Operation/addToSpace/OperationBody)
        //
        // Then, in addition during VUI setup the root-workspace needs to be put
        // into an anchor, to let the whole stuff begin somewhere (currently it
        // does so in the UMLAttribute.ocp-Code in EditorSpace)
        //
        // workspace = (instance instanceof Workspace ? instance : workspace);

        AnchorInterceptor ai = new AnchorInterceptor(context, instance, workspace, transaction);
        TargetType delegate = Delegate.create(instance, Anchor.class, ai);

        if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "New anchor '%(delegate)' for '%(target)', interceptor '%(interceptor)'", "delegate", System.identityHashCode(delegate), "target", instance, "interceptor", ai); }

        return delegate;
    }

    public boolean matches(Object target, Method method, Object[] arguments){
        return true;
    }

    private static Method METHOD_Anchor_getWorkspace;
    private static Method METHOD_Anchor_setWorkspace;
    private static Method METHOD_Anchor_getTransaction;
    private static Method METHOD_Anchor_setTransaction;
    private static Method METHOD_Anchor_getDelegate;

    private static Method METHOD_Object_hashCode;
    private static Method METHOD_Object_equals;

    public Object handleInvocation(Object proxy, Delegate delegate, Object target, Method method, Object[] arguments) throws Throwable {
        CallContext context = (arguments != null && arguments.length > 0 && arguments[0] instanceof CallContext ? ((CallContext) (arguments[0])) : RootContext.getFallbackCallContext());
        Class return_type = method.getReturnType();

        if (method.equals(METHOD_Anchor_getWorkspace)) {
            return this.workspace;
        }
        if (method.equals(METHOD_Anchor_setWorkspace)) {
            this.workspace = (Workspace) arguments[1];
        }
        if (method.equals(METHOD_Anchor_getTransaction)) {
            return this.transaction;
        }
        if (method.equals(METHOD_Anchor_setTransaction)) {
            this.transaction = (Transaction) arguments[1];
        }
        if (method.equals(METHOD_Anchor_getDelegate)) {
            return target;
        }

        if (method.equals(METHOD_Object_hashCode)) {
            return this.hashCode(target);
        }
        if (method.equals(METHOD_Object_equals)) {
            return this.equals(target, arguments[0]);
        }

        Object result = method.invoke(target, arguments);

        if (result instanceof Anchor) {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Delegate invocation '%(delegate)/%(target)'.'%(method)', result '%(result)' is already an anchor", "delegate", System.identityHashCode(proxy), "target", target, "method", method.getName(), "result", result); }
            return result;
        } else if (result instanceof Anchorable) {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Delegate invocation '%(delegate)/%(target)'.'%(method)', result '%(result)' is anchorable and will be anchored", "delegate", System.identityHashCode(proxy), "target", target, "method", method.getName(), "result", result); }
            return ((Anchorable) result).createAnchor(context, this.workspace, this.transaction);
        } else {
            if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendDiagnostics(context, "Delegate invocation '%(delegate)/%(target)'.'%(method)', result '%(result)' is not anchorable", "delegate", System.identityHashCode(proxy), "target", target, "method", method.getName(), "result", result); }
            return result;
        }
    }

    public boolean equals(Object target, Object other_proxy) {
        // in general, other_proxy could be a normal instance, but here with anchors we know it's a proxy
        // in general, one could check, but we know here:  static boolean isProxyClass(Class cl)
        Delegate d = (Delegate) Proxy.getInvocationHandler(other_proxy);

        Object            other    = d.getTarget();
        AnchorInterceptor other_ai = (AnchorInterceptor) d.getInterceptors()[0];

        CallContext context = RootContext.getFallbackCallContext();
        return (    (    (    target == null
                            && other == null
                         )
                      || (    this.target != null
                           && this.target.equals(other)
                         )
                    )
                 && (    (    this.workspace == null
                           && other_ai.workspace == null
                         )
                      || (    this.workspace != null
                           && this.workspace.equals(other_ai.workspace)
                         )
                    )
                 && (    (    this.transaction == null
                           && other_ai.transaction == null
                         )
                      || (    this.transaction != null
                           && this.transaction.equals(other_ai.transaction)
                         )
                    )
               );
    }

    public int hashCode(Object target) {
        return    (target == null           ? 0 : target.hashCode())
                ^ (this.workspace == null   ? 0 : this.workspace.hashCode())
                ^ (this.transaction == null ? 0 : this.transaction.hashCode());
    }
}
