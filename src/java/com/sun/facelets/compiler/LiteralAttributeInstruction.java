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

package com.sun.facelets.compiler;

import java.io.IOException;
import java.util.List;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


final class LiteralAttributeInstruction implements Instruction {
    private final String attr;

    private final String text;

    public LiteralAttributeInstruction(String attr, String text) {
        this.attr = attr;
        this.text = text;
    }

    public void write(FacesContext context) throws IOException {
      context.getResponseWriter().writeAttribute(this.attr, this.text, null);
    }

    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    public boolean isLiteral() {
        return true;
    }
}
