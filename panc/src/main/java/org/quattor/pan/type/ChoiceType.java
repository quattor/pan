package org.quattor.pan.type;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.utils.Range;

import java.util.ArrayList;
import java.util.List;

import static org.quattor.pan.utils.MessageUtils.MSG_NONEXISTANT_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_INVALID_CHOICE_TYPE;
import static org.quattor.pan.utils.MessageUtils.MSG_ALL_STRING_ARGS_REQ;

/**
 * Created by iliclaey.
 */
public class ChoiceType extends AliasType {

    private List<Element> choices;

    /**
     * Constructor for AliasType takes the name of the type identifier.
     *
     * @param source      String describing the source containing this definition
     * @param sourceRange
     * @param identifier  name of the referenced type
     * @param range will alway be null in this case
     * @param choices list of choices
     */
    public ChoiceType(String source, SourceRange sourceRange, String identifier, Range range, List<Element> choices) {
        super(source, sourceRange, identifier, range);
        this.choices = choices;
    }

    public static ChoiceType getInstance(String source, SourceRange sourceRange, String identifier,
                                         Range range, List<Operation> choices) throws SyntaxException {

        List<Element> els = new ArrayList<Element>();
        for (Operation o : choices) {
            Element e = null;

            try {
                e = (Element) o;
            } catch(ClassCastException cce) {
                throw SyntaxException.create(sourceRange, MSG_ALL_STRING_ARGS_REQ,
                        "choice", e.getTypeAsString());
            }

            els.add(e);
        }

        return new ChoiceType(source, sourceRange, identifier, range, els);
    }

    public Object validate(final Context context, final Element self) {

        FullType type = context.getFullType(identifier);

        type.validate(context, self);

        boolean found = false;
        for (Element e : choices) {
            if (e.equals(self)) found = true;
        }

        if (!found) {
            throw ValidationException.create(MSG_INVALID_CHOICE_TYPE, self.toString());
        }

        return null;
    }

    public String toString() {
        String s = "Choice(";
        if (choices != null) {
            for (Element e: choices) {
                s += e.toString() + " ";
            }
        }
        s += ")";

        return s;
    }
}
