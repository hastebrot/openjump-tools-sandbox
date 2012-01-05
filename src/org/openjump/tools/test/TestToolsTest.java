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
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjump.tools.plugin.UnionByAttributePlugIn;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
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
    
//    @Before
//    public void before() {
//        workbench.getFrame().addTaskFrame();
//    }
    
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
    
    // TODO: check for I18N fields.
    // TODO: test execute() and run(), with or without ThreadedPlugIn.
    
    //@NoWorkbenchRequired
    @Test
    public void testConfigurePlugInWithDialog() throws Exception {
        // given: "an example plugin with dialog"
        PlugIn plugin = new ExampleWithDialogPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        
        // when: "configure plugin with dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "contains the dialog with parameters"
        assertNotNull(privateField(plugin, "dialog"));
    }

    //@NoWorkbenchRequired
    @Test(expected=NoSuchFieldException.class)
    public void testConfigurePlugInWithoutDialog() throws Exception {
        // given: "an example plugin without dialog"
        PlugIn plugin = new ExampleWithoutDialogPlugIn();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        
        // when: "configure plugin without dialog"
        TestTools.configurePlugIn(plugin, parameters, false);
        
        // then: "complain gracefully that no field for parameters exists"
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
    public void testOpenFile2nd() {
        // when: "a shapefile is opened a second time"
        TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
    }
    
    @Test
    public void testExamplePlugin() throws Exception {
        // given: "a loaded shapefile fixture"
        TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
        
        // and: "an initialized plugin with dialog values"
        PlugIn plugin = new UnionByAttributePlugIn();
        LayerManager layerManager = workbench.getContext().getLayerManager();
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("LAYER", layerManager.getLayer("dissolve"));
        parameters.put("ATTRIBUTE", "LABEL");
        parameters.put("IGNORE_EMPTY", false);
        parameters.put("MERGE_LINES", false);
        parameters.put("TOTAL_NUMERIC_FIELDS", false);
        TestTools.configurePlugIn(plugin, parameters, true);
        
        // when: "union by attribute is called"
        TestTools.executePlugIn(plugin, workbench.getContext());
        
        // then: "layer manager contains the source and result layer" 
        assertEquals(2, layerManager.getLayers().size());
        //Thread.sleep(Integer.MAX_VALUE);
    }
    
    //-----------------------------------------------------------------------------------
    // TEST FIXTURES.
    //-----------------------------------------------------------------------------------
    
    public class ExampleWithDialogPlugIn extends AbstractPlugIn {
        @SuppressWarnings("unused")
        private MultiInputDialog dialog;
    }
    
    public class ExampleWithoutDialogPlugIn extends AbstractPlugIn {}
    
}
