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

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.convert.Converter;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.MetaRuleset;
import com.sun.facelets.tag.jsf.ConvertHandler;
import com.sun.facelets.tag.jsf.ConverterConfig;

/**
 * Register a named Converter instance on the UIComponent associated with the
 * closest parent UIComponent custom action. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/converter.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id: ConvertDelegateHandler.java,v 1.4 2008-07-13 19:01:44 rlubke Exp $
 */
public final class ConvertDelegateHandler extends ConvertHandler {

    private final TagAttribute converterId;

    /**
     * @param config
     */
    public ConvertDelegateHandler(ConverterConfig config) {
        super(config);
        this.converterId = this.getRequiredAttribute("converterId");
    }

    /**
     * Uses the specified "converterId" to pull an instance from the Application
     * 
     * @see javax.faces.application.Application#createComponent(java.lang.String)
     * @see com.sun.facelets.tag.jsf.ConverterHandler#createConverter(com.sun.facelets.FaceletContext)
     */
    protected Converter createConverter(FaceletContext ctx)
            throws FacesException, ELException, FaceletException {
        return ctx.getFacesContext().getApplication().createConverter(
                this.converterId.getValue(ctx));
    }

    protected MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignoreAll();
    }
}
