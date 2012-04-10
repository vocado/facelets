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

package com.sun.facelets.tag.jsf;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.MethodExpressionValidator;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.el.LegacyMethodBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.MetaRule;
import com.sun.facelets.tag.MetadataTarget;
import com.sun.facelets.util.FacesAPI;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: EditableValueHolderRule.java,v 1.3 2008-07-13 19:01:46 rlubke Exp $
 */
public final class EditableValueHolderRule extends MetaRule {

    final static class LiteralValidatorMetadata extends Metadata {

        private final String validatorId;

        public LiteralValidatorMetadata(String validatorId) {
            this.validatorId = validatorId;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance).addValidator(ctx.getFacesContext()
                    .getApplication().createValidator(this.validatorId));
        }
    }

    final static class ValueChangedExpressionMetadata extends Metadata {
        private final TagAttribute attr;

        public ValueChangedExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance)
                    .addValueChangeListener(new MethodExpressionValueChangeListener(
                            this.attr.getMethodExpression(ctx, null,
                                    VALUECHANGE_SIG)));
        }
    }

    final static class ValueChangedBindingMetadata extends Metadata {
        private final TagAttribute attr;

        public ValueChangedBindingMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance)
                    .setValueChangeListener(new LegacyMethodBinding(this.attr
                            .getMethodExpression(ctx, null, VALUECHANGE_SIG)));
        }
    }

    final static class ValidatorExpressionMetadata extends Metadata {
        private final TagAttribute attr;

        public ValidatorExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance)
                    .addValidator(new MethodExpressionValidator(this.attr
                            .getMethodExpression(ctx, null, VALIDATOR_SIG)));
        }
    }

    final static class ValidatorBindingMetadata extends Metadata {
        private final TagAttribute attr;

        public ValidatorBindingMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance)
                    .setValidator(new LegacyMethodBinding(this.attr
                            .getMethodExpression(ctx, null, VALIDATOR_SIG)));
        }
    }

    private final static Class[] VALIDATOR_SIG = new Class[] {
            FacesContext.class, UIComponent.class, Object.class };

    private final static Class[] VALUECHANGE_SIG = new Class[] { ValueChangeEvent.class };

    public final static EditableValueHolderRule Instance = new EditableValueHolderRule();

    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {

        if (meta.isTargetInstanceOf(EditableValueHolder.class)) {

            boolean elSupport = FacesAPI.getComponentVersion(meta
                    .getTargetClass()) >= 12;

            if ("validator".equals(name)) {
                if (attribute.isLiteral()) {
                    return new LiteralValidatorMetadata(attribute.getValue());
                } else if (elSupport) {
                    return new ValidatorExpressionMetadata(attribute);
                } else {
                    return new ValidatorBindingMetadata(attribute);
                }
            }

            if ("valueChangeListener".equals(name)) {
                if (elSupport) {
                    return new ValueChangedExpressionMetadata(attribute);
                } else {
                    return new ValueChangedBindingMetadata(attribute);
                }
            }

        }
        return null;
    }

}
