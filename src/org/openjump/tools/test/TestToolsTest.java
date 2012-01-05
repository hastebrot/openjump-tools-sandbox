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
        workbench = TestTools.buildWorkbench(new String[] { "-i18n", "en" });
        // TODO: Wait until frame is visible.
        // TODO: Refactor PlugIns so that a visible frame isn't needed.
        workbench.getFrame().setVisible(true);
    }
    
    @Before
    public void before() {
        //workbench.getFrame().addTaskFrame();
    }
    
    @After
    public void after() throws Exception {
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
    }
    
    @Test
    public void testOpenFileAgain() {
        // when: "a shapefile is opened again"
        TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
    }
    
    // TODO: check for I18N fields.
    // TODO: test execute() and run(), with or without ThreadedPlugIn.
    
    @Test
    public void testConfigurePlugInWithFields() throws Exception {
        // given: "an example plugin with fields"
        PlugIn plugin = new ExamplePlugInWithFields();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("field", "value");
        
        // when: "configure plugin with fields"
        TestTools.configurePlugIn(plugin, parameters);
        
        // then: "contains the field"
        assertEquals("value", privateField(plugin, "field"));
    }
    
    @Test(expected=NoSuchFieldException.class)
    public void testConfigurePlugInWithoutFields() throws Exception {
        // given: "an example plugin without dialog"
        PlugIn plugin = new ExampleEmptyPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("field", "value");
        
        // when: "configure plugin without dialog"
        TestTools.configurePlugIn(plugin, parameters);
        
        // then: "complain gracefully that no field for parameters exists"
    }
    
    @Test
    public void testConfigurePlugInWithDialog() throws Exception {
        // given: "an example plugin with dialog"
        PlugIn plugin = new ExamplePlugInWithDialog();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("field", "value");
        
        // when: "configure plugin with dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "contains the dialog with parameters"
        assertNotNull(privateField(plugin, "dialog"));
    }
    
    @Test(expected=NoSuchFieldException.class)
    public void testConfigurePlugInWithoutDialog() throws Exception {
        // given: "an example plugin without dialog"
        PlugIn plugin = new ExampleEmptyPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("field", "value");
        
        // when: "configure plugin without dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "complain gracefully that no field for parameters exists"
    }
    
    @Test
    public void testExecutePlugin() throws Exception {
        // given: "a threaded plugin with parameters"
        PlugIn plugin = new ExampleThreadedPlugInWithDialog();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", "execute plugin");
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // when: "the plugin is executed"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "a property was added to the blackboard"
        Blackboard blackboard = workbench.getContext().getBlackboard();
        assertEquals("execute plugin", blackboard.get("key"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testExecutePluginWithoutThreadedPlugIn() throws Exception {
        // given: "an non-threaded plugin"
        PlugIn plugin = new ExamplePlugInWithDialog();
        
        // when: "the plugin is executed"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "an exception was thrown"
    }
    
    //-----------------------------------------------------------------------------------
    // TEST FIXTURES.
    //-----------------------------------------------------------------------------------
    
    public class ExampleEmptyPlugIn extends AbstractPlugIn {}
    
    public class ExamplePlugInWithFields extends AbstractPlugIn {
        @SuppressWarnings("unused")
        private String field = null;
    }
    
    public class ExamplePlugInWithDialog extends AbstractPlugIn {
        @SuppressWarnings("unused")
        private MultiInputDialog dialog;
    }
    
    public class ExampleThreadedPlugInWithFields extends AbstractPlugIn 
            implements ThreadedPlugIn {
        private MultiInputDialog dialog;
        private String key = "";
        
        public boolean execute(PlugInContext context) throws Exception {
            dialog = new MultiInputDialog();
            dialog.addTextField("key", "", 10, null, "");
            dialog.setVisible(true);
            key = dialog.getText("key");
            return dialog.wasOKPressed();
        }
        
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            String value = key;
            context.getWorkbenchContext().getBlackboard().put("key", value);
        }
    }
    
    public class ExampleThreadedPlugInWithDialog extends AbstractPlugIn 
            implements ThreadedPlugIn {
        private MultiInputDialog dialog;
        
        public boolean execute(PlugInContext context) throws Exception {
            dialog = new MultiInputDialog();
            dialog.addTextField("key", "", 10, null, "");
            dialog.setVisible(true);
            return dialog.wasOKPressed();
        }
        
        public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
            String value = dialog.getText("key");
            context.getWorkbenchContext().getBlackboard().put("key", value);
        }
    }
    
}
