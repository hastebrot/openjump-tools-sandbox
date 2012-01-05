package org.openjump.tools.test;

import java.util.HashMap;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

@SuppressWarnings("serial")
public class DialogValues extends MultiInputDialog {
    
    //-----------------------------------------------------------------------------------
    // FIELDS.
    //-----------------------------------------------------------------------------------
    
    private HashMap<String, Object> fields = new HashMap<String, Object>();

    //-----------------------------------------------------------------------------------
    // METHODS.
    //-----------------------------------------------------------------------------------
    
    public void putField(String fieldName, Object value) {
        fields.put(fieldName, value);
    }
    
    @Override
    public String getText(String fieldName) {
        return (String) fields.get(fieldName);
    }

    @Override
    public boolean getBoolean(String fieldName) {
        return (Boolean) fields.get(fieldName);
    }
    
    @Override
    public double getDouble(String fieldName) {
        return (Double) fields.get(fieldName);
    }
    
    @Override
    public int getInteger(String fieldName) {
        return (Integer) fields.get(fieldName);
    }
    
    @Override
    public Layer getLayer(String fieldName) {
        return (Layer) fields.get(fieldName);
    }
    
    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException("Shouldn't be called in execute().");
    }

}
