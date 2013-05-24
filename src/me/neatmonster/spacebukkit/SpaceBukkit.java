/*
 * This file is part of SpaceBukkit (http://spacebukkit.xereo.net/).
 *
 * SpaceBukkit is free software: you can redistribute it and/or modify it under the terms of the
 * Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license as published by the Creative Common organization,
 * either version 3.0 of the license, or (at your option) any later version.
 *
 * SpaceBukkit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Attribution-NonCommercial-ShareAlike
 * Unported (CC BY-NC-SA) license for more details.
 *
 * You should have received a copy of the Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license along with
 * this program. If not, see <http://creativecommons.org/licenses/by-nc-sa/3.0/>.
 */
package me.neatmonster.spacebukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.UUID;
import java.io.File;

import com.drdanick.rtoolkit.system.EventDispatchWorker;
import com.drdanick.rtoolkit.system.SingleWorkerPool;

import mcstats.Metrics;
import me.neatmonster.spacebukkit.actions.PlayerActions;
import me.neatmonster.spacebukkit.actions.ServerActions;
import me.neatmonster.spacebukkit.actions.SystemActions;
import me.neatmonster.spacebukkit.players.SBListener;
import me.neatmonster.spacebukkit.plugins.PluginsManager;
import me.neatmonster.spacebukkit.system.PerformanceMonitor;
import me.neatmonster.spacemodule.SpaceModule;
import me.neatmonster.spacemodule.api.ActionsManager;
import me.neatmonster.spacertk.SpaceRTK;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.drdanick.rtoolkit.system.EventDispatcher;
import com.drdanick.rtoolkit.event.ToolkitEventHandler;
/**
 * Main class of the Plugin
 */
public class SpaceBukkit extends JavaPlugin {
    public static SpaceRTK     spaceRTK = null;
    private static SpaceBukkit spacebukkit;

    public static SpaceBukkit getInstance() {
        return spacebukkit;
    }

    public int                  port;
    public int                  rPort;
    public int                  pingPort;
    public String               salt;
    public InetAddress          bindAddress;
    
    public int                  maxJoins;
    public int                  maxMessages;
    public int                  maxQuits;

    public PluginsManager       pluginsManager;
    public ActionsManager       actionsManager;
    public PanelListener        panelListener;
    public PerformanceMonitor   performanceMonitor;

    private YamlConfiguration   config;

    private final Timer         timer  = new Timer("SpaceBukkit Timer Thread", false);

    private EventDispatcher     edt;
    private ToolkitEventHandler eventHandler;
    private EventDispatchWorker toolkitEventWorker;

    @Override
    public void onDisable() {
        performanceMonitor.infanticide();
        timer.cancel();
        try {
            if (panelListener != null)
                panelListener.stopServer();
        } catch (final Exception e) {
            getLogger().severe(e.getMessage());
        }
        edt.setRunning(false);
        synchronized (edt) {
            edt.notifyAll();
        }
        toolkitEventWorker.setEnabled(false);
    }

    @Override
    public void onEnable() {
        spacebukkit = this;
        
        config = YamlConfiguration.loadConfiguration(new File("SpaceModule", "configuration.yml"));
        config.addDefault("General.salt", "<default>");
        config.addDefault("General.worldContainer", Bukkit.getWorldContainer().getPath());
        config.addDefault("General.bindIp", Bukkit.getServer().getIp());
        config.addDefault("SpaceBukkit.port", 2011);
        config.addDefault("SpaceRTK.port", 2012);
        config.addDefault("SpaceBukkit.maxJoins", 199);
        config.addDefault("SpaceBukkit.maxMessages", 199);
        config.addDefault("SpaceBukkit.maxQuits", 199);
        config.options().copyDefaults(true);
        salt = config.getString("General.salt", "<default>");
        if (salt.equals("<default>")) {
            salt = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            config.set("General.salt", salt);
        }
        config.set("General.worldContainer", Bukkit.getWorldContainer().getPath());
        port = config.getInt("SpaceBukkit.port", 2011);
        rPort = config.getInt("SpaceRTK.port", 2012);


        String bindAddressString = config.getString("General.bindIp", "0.0.0.0");
        if(bindAddressString.trim().isEmpty())
            bindAddressString = "0.0.0.0";
        try {
            bindAddress = InetAddress.getByName(bindAddressString);
        } catch(UnknownHostException e) {
            try {
                bindAddress = InetAddress.getLocalHost();
            } catch(UnknownHostException e2) {}
            System.err.println("Warning: Could not assign bind address " + bindAddressString + ":");
            System.err.println(e.getMessage());
            System.err.println("Will bind to loopback address: " + bindAddress.getHostAddress() + "...");
        }

        pingPort = config.getInt("SpaceBukkit.pingPort", 2014);
        maxJoins = config.getInt("SpaceBukkit.maxJoins", 199);
        maxMessages = config.getInt("SpaceBukkit.maxMessages", 199);
        maxQuits = config.getInt("SpaceBukkit.maxQuits", 199);
        try {
            config.save(SpaceModule.CONFIGURATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(edt == null)
            edt = new EventDispatcher(new SingleWorkerPool());

        if(toolkitEventWorker == null) {
            toolkitEventWorker = new EventDispatchWorker();
            toolkitEventWorker.setEnabled(true);
            edt.registerEventHandler(eventHandler, toolkitEventWorker);
        }

        if(!edt.isRunning()) {
            synchronized(edt) {
                edt.notifyAll();
            }
            edt.setRunning(true);
            Thread edtThread = new Thread(edt, "SpaceModule EventDispatcher");
            edtThread.setDaemon(true);
            edtThread.start();
        }

        if(!toolkitEventWorker.isRunning()) {
            toolkitEventWorker.setEnabled(true);
        }
        
        setupMetrics();

        new SBListener(this);
        pluginsManager = new PluginsManager();
        actionsManager = new ActionsManager();
        actionsManager.register(PlayerActions.class);
        actionsManager.register(ServerActions.class);
        actionsManager.register(SystemActions.class);
        panelListener = new PanelListener();
        performanceMonitor = new PerformanceMonitor();
        timer.scheduleAtFixedRate(performanceMonitor, 0L, 1000L);
    }
    
    /**
     * Sets up Metrics
     */
    private void setupMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            
            metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the RTK event dispatcher
     * @return event dispatcher
     */
    public EventDispatcher getEdt() {
        return edt;
    }

    /**
     * Gets the RTK event handler
     * @return event handler
     */
    public ToolkitEventHandler getEventHandler() {
        return eventHandler;
    }

    /**
     * A terrible hack to illegitimately create a ToolkitEventHandler
     */
    private class EventHandler extends ToolkitEventHandler {
    }

}
