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

import static org.openjump.tools.test.ReflectionUtils.privateField;
import static org.openjump.tools.test.ReflectionUtils.privateStaticField;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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

/**
 * @author Benjamin Gudehus
 * @since 2012-01-04
 */
public class TestTools {
    
    //-----------------------------------------------------------------------------------
    // MAIN METHOD.
    //-----------------------------------------------------------------------------------
    
    public static void main(String[] args) throws Exception {
        final JUMPWorkbench workbench = TestTools.buildWorkbench(args);
        workbench.getFrame().addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                TestTools.openFile(new File("share/dissolve.shp"), workbench.getContext());
            }
        });
        workbench.getFrame().setVisible(true);
    }
    
    //-----------------------------------------------------------------------------------
    // STATIC METHODS.
    //-----------------------------------------------------------------------------------
    
    /**
     * Builds a new Workbench with WorkbenchFrame and WorkbenchContext.
     * 
     * @see JUMPWorkbench.main()
     * @return JUMPWorkbench
     */
    public static JUMPWorkbench buildWorkbench(String[] args) throws Exception {
        // Configure a SplashPanel.
        // TODO: (DONE) Don't show the splash window on startup.
        String title = "OpenJUMP";
        SplashPanel splashPanel = new SplashPanel(JUMPWorkbench.splashImage(), title);
        SplashWindow splashWindow = new SplashWindow(splashPanel);
        //splashWindow.setVisible(true);
        
        // Create a new Workbench with WorkbenchFrame and WorkbenchContext.
        privateStaticField(JUMPWorkbench.class, "commandLine", new CommandLine());
        TaskMonitor monitor = new DummyTaskMonitor();
        Setup setup = new JUMPConfiguration();
        //JUMPWorkbench.main(args, title, setup, splashPanel, monitor);
        JUMPWorkbench workbench = new JUMPWorkbench(title, args, splashWindow, monitor);
        
        // Setup Workbench.
        setup.setup(workbench.getContext());
        OpenJumpConfiguration.postExtensionInitialization(workbench.getContext());
        return workbench;
    }
    
    public static void openFile(File file, WorkbenchContext context) {
        OpenFilePlugIn filePlugin = new OpenFilePlugIn(context, file);
        filePlugin.actionPerformed(new ActionEvent(filePlugin, 0, ""));
    }
    
    public static void installPlugIn(PlugIn plugin, WorkbenchContext context) 
            throws Exception {
        PlugInContext plugInContext = context.createPlugInContext();
        plugin.initialize(plugInContext);
    }
    
    /**
     * Configure PlugIn via fields in the object. Replaces the configuration of the 
     * plugin via an user dialog in PlugIn.execute().
     * 
     * @param plugin
     * @param parameters
     */
    public static void configurePlugIn(PlugIn plugin, Map<String, Object> parameters)
            throws Exception {
        for (String key : parameters.keySet()) {
            privateField(plugin, key, parameters.get(key));
        }
    }

    /**
     * Configure PlugIn via a dialog field. Replaces the configuration of the 
     * plugin via an user dialog in PlugIn.execute().
     * 
     * @param plugin
     * @param parameters
     * @param retrieveFieldNamesFromPlugIn
     */
    // TODO: Throw exception if plugin has no field "dialog".
    public static void configurePlugIn(PlugIn plugin, Map<String, Object> parameters, 
            boolean retrieveFieldNamesFromPlugIn) throws Exception {
        DialogParameters dialogParameters = new DialogParameters();
        for (String key : parameters.keySet()) {
            Object fieldValue = parameters.get(key);
            String fieldName = key;
            if (retrieveFieldNamesFromPlugIn) {
                fieldName = (String) privateStaticField(plugin.getClass(), fieldName);
            }
            dialogParameters.putField(fieldName, fieldValue);
        }
        privateField(plugin, "dialog", dialogParameters);
    }
    
    // TODO: Wait until plugin has finished.
    // TODO: Start UndoableEditReceiver (see AbstractPlugIn.toActionListener).
    public static void executePlugIn(PlugIn plugin, WorkbenchContext context) 
            throws Exception {
        TaskMonitor taskMonitor = new DummyTaskMonitor();
        PlugInContext plugInContext = context.createPlugInContext();
        //AbstractPlugIn.toActionListener(plugIn, context, taskMonitor);
        if (plugin instanceof ThreadedPlugIn) {
            ((ThreadedPlugIn) plugin).run(taskMonitor, plugInContext);
        }
        else {
            String message = "Only ThreadedPlugIn is supported for now.";
            throw new IllegalArgumentException(message);
        }
    }
    
}
