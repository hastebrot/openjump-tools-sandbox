package org.openjump.tools.test;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.openjump.OpenJumpConfiguration;
import org.openjump.core.ui.plugin.file.OpenFilePlugIn;
import org.openjump.tools.plugin.UnionByAttributePlugIn;

import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.commandline.CommandLine;
import com.vividsolutions.jump.workbench.JUMPConfiguration;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Setup;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SplashPanel;
import com.vividsolutions.jump.workbench.ui.SplashWindow;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FirstTaskFramePlugIn;

class TestTools {
    
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
        
        JUMPWorkbench workbench = new JUMPWorkbench("", args, splashWindow, progressMonitor);
        setup.setup(workbench.getContext());
        
        PlugInContext plugInContext = new PlugInContext(workbench.getContext(), null, null, null, null);
        new UnionByAttributePlugIn().initialize(plugInContext);
        OpenJumpConfiguration.postExtensionInitialization(workbench.getContext());
        return workbench;
    }
    
    public static void main(String[] args) throws Exception {
        final JUMPWorkbench workbench = TestTools.buildWorkbench(args);
        workbench.getFrame().addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                TestTools.openFile(workbench.getContext(), new File("share/dissolve.shp"));
            }
        });
        workbench.getFrame().setVisible(true);
    }
    
    public static void openFile(WorkbenchContext context, File file) {
        OpenFilePlugIn filePlugin = new OpenFilePlugIn(context, file);
        filePlugin.actionPerformed(new ActionEvent(filePlugin, 0, ""));
    }
    
    public static void setPrivateField(Object obj, String name, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    public static Object getPrivateStaticField(Class<?> cls, String name) throws Exception {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(cls);
    }
    
    public static void setPrivateStaticField(Class<?> cls, String name, Object value) throws Exception {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        field.set(cls, value);
    }
    
    public static void callPrivateSuperclassMethodWithoutArgs(Object obj, String name) throws Exception {
        Method method = obj.getClass().getSuperclass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(obj);
    }
    
}
