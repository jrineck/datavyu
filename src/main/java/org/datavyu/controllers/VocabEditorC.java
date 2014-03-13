/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.controllers;

import org.datavyu.Datavyu;
import org.datavyu.views.VocabEditorV;

import javax.swing.*;

/**
 * A controller for invoking the vocab editor.
 */
public class VocabEditorC {

    private static final VocabEditorC c = new VocabEditorC(); //singleton
    private VocabEditorV view;
    
    /**
     * Constructor.
     */
    private VocabEditorC() {}     
        
    public static VocabEditorC getController()
    {
        return c;
    }
    
    public void showView()
    {
        if(view == null) //If we don't already have a view open...
        {
            // ...create the view with mainFrame as parent
            JFrame mainFrame = Datavyu.getApplication().getMainFrame();
            view = new VocabEditorV(mainFrame, false);           
        }
        Datavyu.getApplication().show(view); //Display view
    }
    
    public void killView()
    {
        if (view != null)
        {
            //view.closeWindow();
            view = null;
        }
    }
}
