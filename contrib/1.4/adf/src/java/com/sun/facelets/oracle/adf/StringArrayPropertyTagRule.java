package com.sun.facelets.oracle.adf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.MetadataTarget;
import com.sun.facelets.tag.MetaRule;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;

/**
 * 
 * @author Adam Winer
 * @version $Id: StringArrayPropertyTagRule.java,v 1.1 2005-08-23 05:54:54 adamwiner Exp $
 */
final class StringArrayPropertyTagRule extends MetaRule
{
  public static final MetaRule Instance = new StringArrayPropertyTagRule();

  private static class LiteralPropertyMetadata extends Metadata
  {
    public LiteralPropertyMetadata(Method method, TagAttribute attribute)
    {
      _method = method;
      _attribute = attribute;
    }
    
    public void applyMetadata(FaceletContext ctx, Object instance)
    {
      if (_params == null)
      {
        String[] strArray = _coerceToStringArray(_attribute.getValue());
        _params = new Object[]{strArray};
      }
      
      try
      {
        _method.invoke(instance, _params);
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
    private       Object[]     _params;
  }
   

  public Metadata applyRule(
     String name,
     TagAttribute attribute,
     MetadataTarget meta)
  {
    // Leave expressions to the underlying code
    if ((meta.getPropertyType(name) == _STRING_ARRAY_TYPE) &&
        attribute.isLiteral())
    {
      Method m = meta.getWriteMethod(name);
      
      // if the property is writable
      if (m != null)
      {
        return new LiteralPropertyMetadata(m, attribute);
      }
    }
    return null;
  }

  static private String[] _coerceToStringArray(String str)
  {
    if (str == null)
      return null;

    ArrayList list = new ArrayList();
    StringTokenizer tokens = new StringTokenizer(str);
    while (tokens.hasMoreTokens())
      list.add(tokens.nextToken());
    String[] strArray = new String[list.size()];
    return (String[]) list.toArray(strArray);
  }

  static private final Class _STRING_ARRAY_TYPE = (new String[0]).getClass();
}
