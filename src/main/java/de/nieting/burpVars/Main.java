package de.nieting.burpVars;

import de.nieting.burpVars.UI.SettingsUI;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import de.nieting.burpVars.model.DataModel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.swing.*;
import java.awt.*;


public class Main implements BurpExtension {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.TRACE);
        LOGGER.debug("debug");
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);
        LOGGER.info("info");
        LOGGER.warn("warn");
        LOGGER.error("error");

//        JFrame frame = new JFrame("SettingsUI");
//        var m = DataModel.fromJson(null);
//        m.addNewVariable();
//        frame.setContentPane(new SettingsUI(m).panel1);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//
//        frame.setSize(new Dimension(1200, 1400));


    }

    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api) {
        new API(api);
        this.api = api;

        var logLevel = api.persistence().preferences().getString("LOG_LEVEL");
        if (logLevel != null) {
            Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.getLevel(logLevel));
        } else {
            Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        }

        var varModelJson = api.persistence().extensionData().getString("VARIABLES");
        var dataModel = DataModel.fromJson(varModelJson);

        api.extension().setName("BurpVar");

        Component ui = new SettingsUI(dataModel).panel1;
        api.userInterface().applyThemeToComponent(ui);
        api.userInterface().registerSuiteTab("Variables", ui);

        api.http().registerHttpHandler(new VariableHttpHandler(api, dataModel));

        api.userInterface().registerContextMenuItemsProvider(new VariableContextMenu(dataModel));

        LOGGER.info("Initialization complete...");
    }


}
