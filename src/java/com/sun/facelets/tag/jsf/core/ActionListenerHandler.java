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
import java.io.Serializable;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.jsf.ComponentSupport;
import com.sun.facelets.util.ReflectionUtil;

/**
 * Register an ActionListener instance on the UIComponent associated with the
 * closest parent UIComponent custom action. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/actionListener.html">tag
 * documentation</a>.
 * 
 * @see javax.faces.event.ActionListener
 * @see javax.faces.component.ActionSource
 * @author Jacob Hookom
 * @version $Id: ActionListenerHandler.java,v 1.7 2008-07-13 19:01:44 rlubke Exp $
 */
public final class ActionListenerHandler extends TagHandler {
	
	private final static class LazyActionListener implements ActionListener, Serializable {

        private static final long serialVersionUID = -9202120013153262119L;
        
        private final String type;
		private final ValueExpression binding;


        public LazyActionListener(String type, ValueExpression binding) {
			this.type = type;
			this.binding = binding;
		}

        public void processAction(ActionEvent event)
              throws AbortProcessingException {
            ActionListener instance = null;
            FacesContext faces = FacesContext.getCurrentInstance();
            if (faces == null) {
                return;
            }
            if (this.binding != null) {
                instance = (ActionListener) binding
                      .getValue(faces.getELContext());
            }
            if (instance == null && this.type != null) {
                try {
                    instance = (ActionListener) ReflectionUtil
                          .forName(this.type).newInstance();
                } catch (Exception e) {
                    throw new AbortProcessingException(
                          "Couldn't Lazily instantiate ValueChangeListener",
                          e);
                }
                if (this.binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            if (instance != null) {
                instance.processAction(event);
            }
        }
    }

    private final TagAttribute binding;

	private final String listenerType;

    /**
     * @param config
     */
    public ActionListenerHandler(TagConfig config) {
        super(config);
        this.binding = this.getAttribute("binding");
        TagAttribute type = this.getAttribute("type");
        if (type != null) {
			if (!type.isLiteral()) {
				throw new TagAttributeException(type,
						"Must be a literal class name of type ActionListener");
			} else {
				// test it out
				try {
					ReflectionUtil.forName(type.getValue());
				} catch (ClassNotFoundException e) {
					throw new TagAttributeException(type,
							"Couldn't qualify ActionListener", e);
				}
			}
			this.listenerType = type.getValue();
		} else {
			this.listenerType = null;
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        if (parent instanceof ActionSource) {
        	if (ComponentSupport.isNew(parent)) {
				ActionSource as = (ActionSource) parent;
				ValueExpression b = null;
				if (this.binding != null) {
					b = this.binding.getValueExpression(ctx, ActionListener.class);
				}
				ActionListener listener = new LazyActionListener(this.listenerType, b);
				as.addActionListener(listener);
			}
        } else {
            throw new TagException(this.tag,
                    "Parent is not of type ActionSource, type is: " + parent);
        }
    }
}
