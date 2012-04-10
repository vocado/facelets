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

package com.sun.facelets.tag.jsf.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;

/**
 * Load a resource bundle localized for the Locale of the current view, and
 * expose it (as a Map) in the request attributes of the current request. <p/>
 * See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/loadBundle.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: LoadBundleHandler.java,v 1.4 2008-07-13 19:01:44 rlubke Exp $
 */
public final class LoadBundleHandler extends TagHandler {

    private final static class ResourceBundleMap implements Map {
        private final static class ResourceEntry implements Map.Entry {

            protected final String key;

            protected final String value;

            public ResourceEntry(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public Object getKey() {
                return this.key;
            }

            public Object getValue() {
                return this.value;
            }

            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }

            public int hashCode() {
                return this.key.hashCode();
            }

            public boolean equals(Object obj) {
                return (obj instanceof ResourceEntry && this.hashCode() == obj
                        .hashCode());
            }
        }

        protected final ResourceBundle bundle;

        public ResourceBundleMap(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            try {
                bundle.getString(key.toString());
                return true;
            } catch (MissingResourceException e) {
                return false;
            }
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Set entrySet() {
            Enumeration e = this.bundle.getKeys();
            Set s = new HashSet();
            String k;
            while (e.hasMoreElements()) {
                k = (String) e.nextElement();
                s.add(new ResourceEntry(k, this.bundle.getString(k)));
            }
            return s;
        }

        public Object get(Object key) {
        	try {
        		return this.bundle.getObject((String) key);
        	} catch( java.util.MissingResourceException mre ) {
        		return "???"+key+"???";
        	}
        }

        public boolean isEmpty() {
            return false;
        }

        public Set keySet() {
            Enumeration e = this.bundle.getKeys();
            Set s = new HashSet();
            while (e.hasMoreElements()) {
                s.add(e.nextElement());
            }
            return s;
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return this.keySet().size();
        }

        public Collection values() {
            Enumeration e = this.bundle.getKeys();
            Set s = new HashSet();
            while (e.hasMoreElements()) {
                s.add(this.bundle.getObject((String) e.nextElement()));
            }
            return s;
        }
    }

    private final TagAttribute basename;

    private final TagAttribute var;

    /**
     * @param config
     */
    public LoadBundleHandler(TagConfig config) {
        super(config);
        this.basename = this.getRequiredAttribute("basename");
        this.var = this.getRequiredAttribute("var");
    }

    /**
     * See taglib documentation.
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        ResourceBundle bundle = null;
        try {
            String name = this.basename.getValue(ctx);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (root != null && root.getLocale() != null) {
                bundle = ResourceBundle.getBundle(name, root.getLocale(), cl);
            } else {
                bundle = ResourceBundle
                        .getBundle(name, Locale.getDefault(), cl);
            }
        } catch (Exception e) {
            throw new TagAttributeException(this.tag, this.basename, e);
        }
        ResourceBundleMap map = new ResourceBundleMap(bundle);
        FacesContext faces = ctx.getFacesContext();
        faces.getExternalContext().getRequestMap().put(this.var.getValue(ctx),
                map);
    }
}
