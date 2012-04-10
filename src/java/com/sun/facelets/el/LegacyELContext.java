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

package com.sun.facelets.el;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.PropertyNotWritableException;
import javax.el.VariableMapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;

/**
 * 
 * 
 * @author Jacob Hookom
 * @version $Id: LegacyELContext.java,v 1.8 2008-07-13 19:01:42 rlubke Exp $
 * @deprecated
 */
public final class LegacyELContext extends ELContext {

    private static final String[] IMPLICIT_OBJECTS = new String[] {
            "application", "applicationScope", "cookie", "facesContext",
            "header", "headerValues", "initParam", "param", "paramValues",
            "request", "requestScope", "session", "sessionScope", "view" };

    private final static FunctionMapper functions = new EmptyFunctionMapper();

    private final FacesContext faces;

    private final ELResolver resolver;

    private final VariableMapper variables;

    public LegacyELContext(FacesContext faces) {
        this.faces = faces;
        this.resolver = new LegacyELResolver();
        this.variables = new DefaultVariableMapper();
    }

    public ELResolver getELResolver() {
        return this.resolver;
    }

    public FunctionMapper getFunctionMapper() {
        return functions;
    }

    public VariableMapper getVariableMapper() {
        return this.variables;
    }
    
    public FacesContext getFacesContext() {
        return this.faces;
    }

    private final class LegacyELResolver extends ELResolver {

        public Class getCommonPropertyType(ELContext context, Object base) {
            return Object.class;
        }

        public Iterator getFeatureDescriptors(ELContext context, Object base) {
            return Collections.EMPTY_LIST.iterator();
        }

        private VariableResolver getVariableResolver() {
            return faces.getApplication().getVariableResolver();
        }

        private PropertyResolver getPropertyResolver() {
            return faces.getApplication().getPropertyResolver();
        }

        public Class getType(ELContext context, Object base, Object property) {
            if (property == null) {
                return null;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    Object obj = this.getVariableResolver().resolveVariable(
                            faces, property.toString());
                    return (obj != null) ? obj.getClass() : null;
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().getType(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().getType(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new javax.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        public Object getValue(ELContext context, Object base, Object property) {
            if (property == null) {
                return null;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    return this.getVariableResolver().resolveVariable(faces,
                            property.toString());
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().getValue(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().getValue(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new javax.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        public boolean isReadOnly(ELContext context, Object base,
                Object property) {
            if (property == null) {
                return true;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    return false; // what can I do?
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().isReadOnly(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().isReadOnly(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new javax.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        public void setValue(ELContext context, Object base, Object property,
                Object value) {
            if (property == null) {
                throw new PropertyNotWritableException("Null Property");
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    if (Arrays.binarySearch(IMPLICIT_OBJECTS, property
                            .toString()) >= 0) {
                        throw new PropertyNotWritableException(
                                "Implicit Variable Not Setable: " + property);
                    } else {
                        Map scope = this.resolveScope(property.toString());
                        this.getPropertyResolver().setValue(scope, property,
                                value);
                    }
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        this.getPropertyResolver().setValue(base,
                                Integer.parseInt(property.toString()), value);
                    } else {
                        this.getPropertyResolver().setValue(base, property,
                                value);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new javax.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }

        }

        private final Map resolveScope(String var) {
            ExternalContext ext = faces.getExternalContext();

            // cycle through the scopes to find a match, if no
            // match is found, then return the requestScope
            Map map = ext.getRequestMap();
            if (!map.containsKey(var)) {
                map = ext.getSessionMap();
                if (!map.containsKey(var)) {
                    map = ext.getApplicationMap();
                    if (!map.containsKey(var)) {
                        map = ext.getRequestMap();
                    }
                }
            }
            return map;
        }
    }

    private final static class EmptyFunctionMapper extends FunctionMapper {

        public Method resolveFunction(String prefix, String localName) {
            return null;
        }

    }

}
