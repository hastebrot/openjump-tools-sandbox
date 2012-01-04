package org.openjump.tools.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import javax.swing.JInternalFrame;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjump.tools.plugin.UnionByAttributePlugIn;

import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;

public class TestToolsTest {
    
    public static JUMPWorkbench workbench;
    
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
        
        // and: "an initialized plugin"
        PlugInContext plugInContext = workbench.getContext().createPlugInContext();
        ThreadedPlugIn plugin = new UnionByAttributePlugIn();
        //plugin.initialize(plugInContext);
        
        // and: "dialog values"
        LayerManager layerManager = workbench.getContext().getLayerManager();
        DialogValues dialogValues = new DialogValues();
        dialogValues.putField(getFieldName(plugin, "LAYER"), layerManager.getLayer("dissolve"));
        dialogValues.putField(getFieldName(plugin, "ATTRIBUTE"), "LABEL");
        dialogValues.putField(getFieldName(plugin, "IGNORE_EMPTY"), false);
        dialogValues.putField(getFieldName(plugin, "MERGE_LINES"), false);
        dialogValues.putField(getFieldName(plugin, "TOTAL_NUMERIC_FIELDS"), false);
        TestTools.setPrivateField(plugin, "dialog", dialogValues);
        
        // when: "union by attribute is called"
        // TODO: Wait until plugin has finished.
        // TODO: Start UndoableEditReceiver (see AbstractPlugIn.toActionListener).
        //AbstractPlugIn.toActionListener(plugIn, workbench.getContext(), new DummyTaskMonitor());
        plugin.run(new DummyTaskMonitor(), plugInContext);
        
        // then: "layer manager contains the source and result layer" 
        assertEquals(2, layerManager.getLayers().size());
        //Thread.sleep(Integer.MAX_VALUE);
    }
    
    private String getFieldName(PlugIn plugin, String field) throws Exception {
        return (String) TestTools.getPrivateStaticField(plugin.getClass(), field);
    }
    
}
