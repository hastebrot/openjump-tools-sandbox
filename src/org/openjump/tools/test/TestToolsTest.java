package org.openjump.tools.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import javax.swing.JInternalFrame;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjump.tools.plugin.UnionByAttributePlugIn;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugIn;

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
        workbench = TestTools.buildWorkbench(new String[] {});
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
    
    //-----------------------------------------------------------------------------------
    // TEST CASES.
    //-----------------------------------------------------------------------------------
    
    @Test
    public void testOpenFile() {
        // when: "a shapefile is opened"
        TestTools.openFile(workbench.getContext(), new File("share/dissolve.shp"));
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
    }
    
    @Test
    public void testOpenFile2nd() {
        // when: "a shapefile is opened a second time"
        TestTools.openFile(workbench.getContext(), new File("share/dissolve.shp"));
        
        // then: "layer manager contains one layer"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        assertEquals(1, layerManager.getLayers().size());
    }
    
    @Test
    public void testExamplePlugin() throws Exception {
        // given: "a loaded shapefile fixture"
        TestTools.openFile(workbench.getContext(), new File("share/dissolve.shp"));
        
        // and: "an initialized plugin with dialog values"
        PlugIn plugin = new UnionByAttributePlugIn();
        //plugin.initialize(plugInContext);
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
    
}
