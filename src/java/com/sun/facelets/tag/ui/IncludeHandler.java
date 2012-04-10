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

package com.sun.facelets.tag.ui;

import java.io.IOException;

import javax.el.ELException;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.el.VariableMapperWrapper;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.TagAttributeException;

/**
 * @author Jacob Hookom
 * @version $Id: IncludeHandler.java,v 1.6 2009-02-02 22:58:59 driscoll Exp $
 */
public final class IncludeHandler extends TagHandler {

    

    private final TagAttribute src;

    /**
     * @param config
     */
    public IncludeHandler(TagConfig config) {
        super(config);
        this.src = this.getRequiredAttribute("src");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException {
        String path = this.src.getValue(ctx);
        if (path == null || path.length() == 0) {
            return;
        }
        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try {
            this.nextHandler.apply(ctx, null);
            ctx.includeFacelet(parent, path);
        } catch (IOException e) {
            throw new TagAttributeException(this.tag, this.src, "Invalid path : " + path);            
        } finally {
            ctx.setVariableMapper(orig);
        }
    }
}
