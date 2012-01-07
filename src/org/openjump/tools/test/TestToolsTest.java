/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI for 
 * visualizing and manipulating spatial features with geometry and attributes.
 * Copyright (C) 2011  The JUMP/OpenJUMP contributors
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 2 of the License, or (at your option) 
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openjump.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openjump.tools.test.ReflectionUtils.privateField;

import java.io.File;
import java.util.HashMap;
import javax.swing.JInternalFrame;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

/**
 * @author Benjamin Gudehus
 */
public class TestToolsTest {
    
    //-----------------------------------------------------------------------------------
    // FIELDS.
    //-----------------------------------------------------------------------------------
    
    public static JUMPWorkbench workbench;
    
    //-----------------------------------------------------------------------------------
    // SETUP AND CLEANUP.
    //-----------------------------------------------------------------------------------
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        workbench = TestTools.buildWorkbench(new String[] {"-i18n", "en"});
        // TODO: Wait until frame is visible.
        // TODO: Refactor PlugIns so that a visible frame is not needed.
        workbench.getFrame().setVisible(true);
    }
    
    @Before
    public void before() {
        //workbench.getFrame().addTaskFrame();
    }
    
    @After
    public void after() throws Exception {
        workbench.getBlackboard().getProperties().remove("parameter1");
        workbench.getBlackboard().getProperties().remove("parameter2");
        for (JInternalFrame frame : workbench.getFrame().getInternalFrames()) {
            workbench.getFrame().removeInternalFrame(frame);
        }
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        workbench.getFrame().setVisible(false);
        workbench.getFrame().dispose();
    }
    
    //-----------------------------------------------------------------------------------
    // TEST CASES.
    //-----------------------------------------------------------------------------------
    
    @Test
    public void testBuildWorkbench() {
        // expect: "Workbench contains WorkbenchFrame and WorkbenchContext"
        assertNotNull(workbench.getFrame());
        assertNotNull(workbench.getContext());
    }
    
    @Test
    public void testOpenFile() {
        // when: "a shapefile is opened"
        TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
        assertNotNull(layerManager.getLayer("dissolve"));
    }
    
    @Test
    public void testOpenFileAgain() {
        // when: "a shapefile is opened again"
        TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
        assertNotNull(layerManager.getLayer("dissolve"));
    }
    
    @Test
    public void testConfigurePlugInWithFields() throws Exception {
        // given: "an example plugin with fields"
        PlugIn plugin = new ExamplePlugInWithFields();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        
        // when: "configure plugin with fields"
        TestTools.configurePlugIn(plugin, parameters);
        
        // then: "contains the field"
        assertEquals("foo", privateField(plugin, "parameter1"));
    }
    
    // TODO: Tests for I18N field usage: configurePlugIn(plugin, parameters, true).
    
    @Test
    public void testConfigurePlugInWithDialog() throws Exception {
        // given: "an example plugin with dialog"
        PlugIn plugin = new ExamplePlugInWithDialog();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        
        // when: "configure plugin with dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "contains the dialog with parameters"
        MultiInputDialog dialog = (MultiInputDialog) privateField(plugin, "dialog");
        assertNotNull(dialog);
        assertEquals("foo", dialog.getText("parameter1"));
    }
    
    @Test(expected=NoSuchFieldException.class)
    public void testConfigurePlugInWithoutFields() throws Exception {
        // given: "an example plugin without dialog"
        PlugIn plugin = new ExampleAbstractPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        
        // when: "configure plugin without dialog"
        TestTools.configurePlugIn(plugin, parameters);
        
        // then: "complain gracefully that no field for parameters exists"
    }
    
    @Test(expected=NoSuchFieldException.class)
    public void testConfigurePlugInWithoutDialog() throws Exception {
        // given: "an example plugin without dialog"
        PlugIn plugin = new ExampleAbstractPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        
        // when: "configure plugin without dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "complain gracefully that no field for parameters exists"
    }
    
    @Test
    public void testExecutePluginWithFields() throws Exception {
        // given: "a threaded plugin with parameters"
        PlugIn plugin = new ExamplePlugInWithFields();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        parameters.put("parameter2", 42);
        TestTools.configurePlugIn(plugin, parameters);
        
        // when: "the plugin is executed"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "a property was added to the blackboard"
        Blackboard blackboard = workbench.getContext().getBlackboard();
        assertEquals("foo", blackboard.get("parameter1"));
        assertEquals(42, blackboard.get("parameter2"));
    }
    
    @Test
    public void testExecutePluginWithDialog() throws Exception {
        // given: "a threaded plugin with parameters"
        PlugIn plugin = new ExamplePlugInWithDialog();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parameter1", "foo");
        parameters.put("parameter2", 42);
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // when: "the plugin is executed"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "a property was added to the blackboard"
        Blackboard blackboard = workbench.getContext().getBlackboard();
        assertEquals("foo", blackboard.get("parameter1"));
        assertEquals(42, blackboard.get("parameter2"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testExecutePluginWithoutThreadedPlugIn() throws Exception {
        // given: "an non-threaded plugin"
        PlugIn plugin = new ExampleAbstractPlugIn();
        
        // when: "the plugin is executed"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "an exception was thrown"
    }
    
    //-----------------------------------------------------------------------------------
    // TEST FIXTURES.
    //-----------------------------------------------------------------------------------
    
    /**
     * Example fixture that outlines a plugin with parameters using instance fields.
     * 
     * <p>One possible structure of a testable plugin.
     * 
     * <p><b>Configure:</b> Has a field dialog: MultiInputDialog. PlugIn with fields 
     * for the parameters. 
     * 
     * <p><b>Execute:</b> Implements ThreadedPlugIn. Does not need to call execute() 
     * in order to run. I.e. only shows a dialog.
     */
    public static class ExamplePlugInWithFields extends AbstractPlugIn 
            implements ThreadedPlugIn {
        private MultiInputDialog dialog;
        private String parameter1 = "";
        private int parameter2 = 0;
        
        public boolean execute(PlugInContext context) throws Exception {
            dialog = new MultiInputDialog();
            dialog.addTextField("parameter1", "", 10, null, "");
            dialog.addIntegerField("parameter2", 0, 10, "");
            dialog.setVisible(true);
            parameter1 = dialog.getText("parameter1");
            parameter2 = dialog.getInteger("parameter2");
            return dialog.wasOKPressed();
        }
        
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            Blackboard blackboard = context.getWorkbenchContext().getBlackboard();
            blackboard.put("parameter1", parameter1);
            blackboard.put("parameter2", parameter2);
        }
    }
    
    /**
     * Example fixture that outlines a plugin with parameters using the dialog.
     * 
     * Only allow dialog getText(), getBoolean(), getDouble(), getInteger(), getLayer()
     * in execute.
     * 
     * I18N field names for parameters.
     */
    public static class ExamplePlugInWithDialog extends AbstractPlugIn 
            implements ThreadedPlugIn {
        private MultiInputDialog dialog;
        
        public boolean execute(PlugInContext context) throws Exception {
            dialog = new MultiInputDialog();
            dialog.addTextField("parameter1", "", 10, null, "");
            dialog.addIntegerField("parameter2", 0, 10, "");
            dialog.setVisible(true);
            return dialog.wasOKPressed();
        }
        
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            Blackboard blackboard = context.getWorkbenchContext().getBlackboard();
            blackboard.put("parameter1", dialog.getText("parameter1"));
            blackboard.put("parameter2", dialog.getInteger("parameter2"));
        }
    }
    
    /**
     * 
     * Also possible with parameters using field setter or a constructor.
     */
    public static class ExampleAbstractPlugIn extends AbstractPlugIn {
        public boolean execute(PlugInContext context) throws Exception {
            Blackboard blackboard = context.getWorkbenchContext().getBlackboard();
            blackboard.put("parameter1", "");
            blackboard.put("parameter2", 0);
            return true;
        }
    }
    
}
