package org.quattor.pan.type;

import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.template.TypeMap;
import org.quattor.pan.utils.MessageUtils;

import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_REFERENCED_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_TYPE;

/**
 * Created by iliclaey.
 *
 * Class used to create more advanced user-defined types.
 */
public abstract class AdvancedType extends BaseType {

    protected String identifier;

    public AdvancedType(String source, SourceRange sourceRange, String identifier) {
        super(source, sourceRange);
        this.identifier = identifier;
    }

    @Override
    public Element findDefault(Context context) {

        try {

            FullType type = context.getFullType(identifier);
            return type.findDefault(context);

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw CompilerError.create(MSG_NONEXISTANT_TYPE, identifier);
        }
    }

    @Override
    public void verifySubtypesDefined(TypeMap types) {
        if (types.get(identifier) == null) {
            throw new EvaluationException(MessageUtils.format(
                    MSG_NONEXISTANT_REFERENCED_TYPE, identifier));
        }
    }
}
