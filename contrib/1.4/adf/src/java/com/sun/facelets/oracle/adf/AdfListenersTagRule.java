package com.sun.facelets.oracle.adf;

import java.beans.PropertyDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.el.MethodExpression;
import javax.faces.el.MethodBinding;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.el.LegacyMethodBinding;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.MetadataTarget;
import com.sun.facelets.tag.MetaRule;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;

/**
 * 
 * @author Adam Winer
 * @version $Id: AdfListenersTagRule.java,v 1.1 2005-08-23 05:54:54 adamwiner Exp $
 */
final class AdfListenersTagRule extends MetaRule
{
  public static final MetaRule Instance = new AdfListenersTagRule();

  private static class ListenerPropertyMetadata extends Metadata
  {
    public ListenerPropertyMetadata(Method method, TagAttribute attribute, Class[] paramList)
    {
      _method = method;
      _attribute = attribute;
      _paramList = paramList;
    }
    
    public void applyMetadata(FaceletContext ctx, Object instance)
    {
      MethodExpression expr =
        _attribute.getMethodExpression(ctx, null, _paramList);
      
      try
      {
        _method.invoke(instance,
                       new Object[]{new LegacyMethodBinding(expr)});
      }
      catch (InvocationTargetException e)
      {
        throw new TagAttributeException(_attribute, e.getCause());
      }
      catch (Exception e)
      {
        throw new TagAttributeException(_attribute, e);
      }
    }

    private final Method       _method;
    private final TagAttribute _attribute;
    private       Class[]      _paramList;
  }
   

  public Metadata applyRule(
     String name,
     TagAttribute attribute,
     MetadataTarget meta)
  {
    if ((meta.getPropertyType(name) == MethodBinding.class) &&
        name.endsWith("Listener"))
    {
      // OK, we're trying to call setFooListener()
      Method m = meta.getWriteMethod(name);
      if (m != null)
      {
        // First, look for the getFooListeners() property
        PropertyDescriptor listeners = meta.getProperty(name + "s");
        if (listeners == null)
          return null;

        // It should return an array of FooListener objects
        Class arrayType = listeners.getPropertyType();
        if (!arrayType.isArray())
          return null;
        
        // Ignore non-ADF types
        Class listenerClass = arrayType.getComponentType();
        if (!listenerClass.getName().startsWith("oracle.adf."))
          return null;

        // Turn that into an Event to get the signature
        Class eventClass = _getEventClass(listenerClass);
        if (eventClass == null)
          return null;

        // And go
        return new ListenerPropertyMetadata(m, attribute,
                                            new Class[]{eventClass});
      }
    }
    return null;
  }

  static private Class _getEventClass(Class listenerClass)
  {
    String listenerName = listenerClass.getName();
    if (!listenerName.endsWith("Listener"))
      return null;
    
    String eventName = (listenerName.substring(0,
                          listenerName.length() - "Listener".length()) +
                        "Event");

    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    try
    {
      return Class.forName(eventName, true, loader);
    }
    catch (ClassNotFoundException cnfe)
    {
      return null;
    }
  }
}
