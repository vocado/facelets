/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag.jstl.fn;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Implementations of JSTL Functions
 * 
 * @author Jacob Hookom
 * @version $Id: JstlFunction.java,v 1.6 2009-01-16 20:51:11 rlubke Exp $
 */
public final class JstlFunction {

    private JstlFunction() {
    }

    public static boolean contains(String name, String searchString) {
        if (name == null || searchString == null) {
            return false;
        }

        return -1 != name.indexOf(searchString);
    }

    public static boolean containsIgnoreCase(String name, String searchString) {
        if (name == null || searchString == null) {
            return false;
        }
        return -1 != name.toUpperCase().indexOf(searchString.toUpperCase());
    }

    public static boolean endsWith(String name, String searchString) {
        if (name == null || searchString == null) {
            return false;
        }
        return name.endsWith(searchString);
    }

    public static String escapeXml(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("<", "&lt;");
    }

    public static int indexOf(String name, String searchString) {
        if (name == null || searchString == null) {
            return -1;
        }
        return name.indexOf(searchString);
    }

    public static String join(String[] a, String delim) {
        if (a == null || delim == null) {
            return null;
        }
        if (a.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer(a.length
                * (a[0].length() + delim.length()));
        for (int i = 0; i < a.length; i++) {
            sb.append(a[i]);
            if (i < (a.length - 1)) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public static int length(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).size();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        if (obj instanceof String) {
            return ((String) obj).length();
        }
        if (obj instanceof Map) {
            return ((Map) obj).size();
        }
        throw new IllegalArgumentException("Object type not supported: "
                + obj.getClass().getName());
    }
    
    public static String replace(String value, String a, String b) {
        if (value == null || a == null || b == null) {
            return null;
        }
        return value.replaceAll(a, b);
    }
    
    public static String[] split(String value, String d) {
        if (value == null || d == null) {
            return null;
        }
        return value.split(d);
    }
    
    public static boolean startsWith(String value, String p) {
        if (value == null || p == null) {
            return false;
        }
        return value.startsWith(p);
    }
    
    public static String substring(String v, int s, int e) {
        if (v == null) {
            return null;
        }
        return v.substring(s, e);
    }
    
    public static String substringAfter(String v, String p) {
        if (v == null) {
            return null;
        }
        int i = v.indexOf(p);
        if (i >= 0) {
            return v.substring(i+p.length());
        }
        return null;
    }
    
    public static String substringBefore(String v, String s) {
        if (v == null) {
            return null;
        }
        int i = v.indexOf(s);
        if (i > 0) {
            return v.substring(0, i);
        }
        return null;
    }
    
    public static String toLowerCase(String v) {
        if (v == null) {
            return null;
        }
        return v.toLowerCase();
    }
    
    public static String toUpperCase(String v) {
        if (v == null) {
            return null;
        }
        return v.toUpperCase();
    }
    
    public static String trim(String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }

}
