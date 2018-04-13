package org.quattor.pan.type;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.parser.ParseException;
import org.quattor.pan.ttemplate.BuildContext;
import org.quattor.pan.ttemplate.Context;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by iliclaey.
 */
public class ChoiceTypeTest {

    @Test(expected = ValidationException.class)
    public void testInvalidSelfValidation() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = new ChoiceType(null, null, args);

        type.validate(context, LongProperty.getInstance(1));
    }

    @Test(expected = ValidationException.class)
    public void testInvalidChoice() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = new ChoiceType(null, null, args);

        type.validate(context, StringProperty.getInstance("C"));
    }

    @Test
    public void testValidChoice() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = new ChoiceType(null, null, args);

        Object o = type.validate(context, StringProperty.getInstance("A"));

        assertEquals(o, null);
    }


}
