package org.openjump.tools.test;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import org.openjump.OpenJumpConfiguration;
import org.openjump.core.ui.plugin.file.OpenFilePlugIn;

import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.commandline.CommandLine;
import com.vividsolutions.jump.workbench.JUMPConfiguration;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Setup;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SplashPanel;
import com.vividsolutions.jump.workbench.ui.SplashWindow;

class TestTools {
    
    //-----------------------------------------------------------------------------------
    // MAIN METHOD.
    //-----------------------------------------------------------------------------------
    
    public static void main(String[] args) throws Exception {
        final JUMPWorkbench workbench = TestTools.buildWorkbench(args);
        workbench.getFrame().addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                TestTools.openFile(workbench.getContext(), new File("share/dissolve.shp"));
            }
        });
        workbench.getFrame().setVisible(true);
    }
    
    //-----------------------------------------------------------------------------------
    // STATIC METHODS.
    //-----------------------------------------------------------------------------------
    
    // TODO: Don't show the splash window on startup.
    static JUMPWorkbench buildWorkbench(String[] args) throws Exception {
        TaskMonitor progressMonitor = new DummyTaskMonitor();
        SplashPanel splashPanel = new SplashPanel(JUMPWorkbench.splashImage(), "OpenJUMP");
//        Setup setup = new JUMPConfiguration() {
//            public void setup(WorkbenchContext workbenchContext) throws Exception {
//                //super.configureStyles(workbenchContext);
//                super.configureDatastores(workbenchContext);
//                PlugInRunner.callPrivateSuperclassMethodWithoutArgs(this, "initializeRenderingManager");
//                PlugInContext plugInContext = new PlugInContext(workbenchContext, null, null,
//                    null, null);
//                new FirstTaskFramePlugIn().initialize(plugInContext);
//                new UnionByAttributePlugIn().initialize(plugInContext);
//                //OpenJumpConfiguration.loadOpenJumpPlugIns(workbenchContext);
//            }
//        };
        Setup setup = new JUMPConfiguration();
        TestTools.setPrivateStaticField(JUMPWorkbench.class, "commandLine", new CommandLine());
        //JUMPWorkbench.main(args, "OpenJUMP", setup, splashPanel, progressMonitor);
        
        SplashWindow splashWindow = new SplashWindow(splashPanel);
        splashWindow.setVisible(true);
        
        JUMPWorkbench workbench = new JUMPWorkbench("OpenJUMP", args, splashWindow, 
                progressMonitor);
        setup.setup(workbench.getContext());
        OpenJumpConfiguration.postExtensionInitialization(workbench.getContext());
        return workbench;
    }
    
    // TODO: Throw exception if plugin has no field "dialog".
    static void configurePlugIn(PlugIn plugin, Map<String, Object> parameters, 
            boolean retrieveFieldNamesFromPlugIn) throws Exception {
        DialogValues dialogValues = new DialogValues();
        for (String key : parameters.keySet()) {
            Object fieldValue = parameters.get(key);
            String fieldName = key;
            if (retrieveFieldNamesFromPlugIn) {
                fieldName = (String) TestTools.getPrivateStaticField(
                        plugin.getClass(), fieldName);
            }
            dialogValues.putField(fieldName, fieldValue);
        }
        TestTools.setPrivateField(plugin, "dialog", dialogValues);
    }
    
    // TODO: Wait until plugin has finished.
    // TODO: Start UndoableEditReceiver (see AbstractPlugIn.toActionListener).
    static void executePlugIn(PlugIn plugin, WorkbenchContext context) throws Exception {
        TaskMonitor taskMonitor = new DummyTaskMonitor();
        PlugInContext plugInContext = context.createPlugInContext();
        //AbstractPlugIn.toActionListener(plugIn, context, taskMonitor);
        if (plugin instanceof ThreadedPlugIn) {
            ((ThreadedPlugIn) plugin).run(taskMonitor, plugInContext);
        }
        else {
            throw new IllegalArgumentException("ThreadedPlugIn is only supported for now.");
        }
    }
    
    public static void openFile(WorkbenchContext context, File file) {
        OpenFilePlugIn filePlugin = new OpenFilePlugIn(context, file);
        filePlugin.actionPerformed(new ActionEvent(filePlugin, 0, ""));
    }
    
    //-----------------------------------------------------------------------------------
    // PRIVATE STATIC METHODS.
    //-----------------------------------------------------------------------------------
    
    private static void setPrivateField(Object obj, String name, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    private static Object getPrivateStaticField(Class<?> cls, String name) throws Exception {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(cls);
    }
    
    private static void setPrivateStaticField(Class<?> cls, String name, Object value) throws Exception {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        field.set(cls, value);
    }
    
//    private static void callPrivateSuperclassMethodWithoutArgs(Object obj, String name) throws Exception {
//        Method method = obj.getClass().getSuperclass().getDeclaredMethod(name);
//        method.setAccessible(true);
//        method.invoke(obj);
//    }
    
}
