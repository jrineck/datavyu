package org.openshapa.views.discrete.datavalues.vocabelements;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import org.apache.log4j.Logger;
import org.openshapa.models.db.FormalArgument;
import org.openshapa.models.db.FormalArgument.FArgType;
import org.openshapa.models.db.SystemErrorException;
import org.openshapa.models.db.VocabElement;
import org.openshapa.views.discrete.EditorComponent;

/**
 * This class is the character editor of a NominalDataValue.
 */
public final class FormalArgTypeEditor extends EditorComponent {

    /** Parent Vocab Element. */
    private VocabElement vocabElement;

    /** Index of the formal arg. */
    private int argIndex;

    /** The logger for this class. */
    private static Logger logger = Logger.getLogger(FormalArgTypeEditor.class);

    public FormalArgTypeEditor(final JTextComponent ta,
                               final VocabElement ve,
                               final int index,
                               final VocabElementV pv) {
        super(ta);
        argIndex = index;
        resetValue(ve);
    }

    public final void resetValue(final VocabElement ve) {
        vocabElement = ve;

        String fargType = "";
        try {
            FormalArgument model = vocabElement.getFormalArgCopy(argIndex);
            FArgType ft = model.getFargType();
            fargType = ft.toString().substring(0, 1);
        } catch (SystemErrorException e) {
            logger.error("Unable to reset value", e);
        }
        setText(fargType);
    }

    /**
     * The action to invoke when focus is gained. NOTE: This is an uneditable
     * editor - so this method should never be called.
     *
     * @param fe The event that triggered this action.
     */
    @Override
    public void focusGained(final FocusEvent fe) {
    }

    /**
     * The action to invoke when a key is typed. NOTE: this is an uneditable
     * editor - so this method should never be called.
     *
     * @param e The KeyEvent that triggered this action.
     */
    @Override
    public void keyTyped(final KeyEvent e) {
    }

    /**
     * Action to take by this editor when a key is pressed. NOTE: This is an
     * uneditable editor - so this method should never be called.
     *
     * @param e The KeyEvent that triggered this action.
     */
    @Override
    public void keyPressed(final KeyEvent e) {
    }

    /**
     * Action to take by this editor when a key is released. NOTE: This is an
     * uneditable editor - so this method should never be called.
     *
     * @param e The KeyEvent that triggered this action.
     */
    @Override
    public void keyReleased(final KeyEvent e) {
    }
}
