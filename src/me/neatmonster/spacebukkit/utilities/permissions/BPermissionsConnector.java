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
package me.neatmonster.spacebukkit.utilities.permissions;

import java.util.ArrayList;
import java.util.List;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.Group;
import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.api.util.Calculable;
import de.bananaco.bpermissions.api.util.CalculableType;

public class BPermissionsConnector implements PermissionsConnector {

    private final WorldManager manager;
    private final String version = "2.9.2";
    
    public BPermissionsConnector() {
        manager = WorldManager.getInstance();
    }
    
    @Override
    public String getPermissionsPluginName() {
        return "bPermissions";
    }

    @Override
    public String getPermissionsPluginVersion() {
        return version;
    }

    @Override
    public List<String> getUserNames() {
        List<String> result = new ArrayList<String>();
        for (World world : manager.getAllWorlds()) {
            for (Calculable user : world.getAll(CalculableType.USER)) {
                result.add(user.getName());
            }
        }
        return result;
    }

    @Override
    public List<String> getUserNames(String world) {
        World w = manager.getWorld(world);
        if (w == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (Calculable user : w.getAll(CalculableType.USER)) {
            result.add(user.getName());
        }
        return result;
    }

    @Override
    public List<String> getGroupNames() {
        List<String> result = new ArrayList<String>();
        for (World world : manager.getAllWorlds()) {
            for (Calculable user : world.getAll(CalculableType.GROUP)) {
                result.add(user.getName());
            }
        }
        return result;
    }

    @Override
    public List<String> getGroupNames(String world) {
        World w = manager.getWorld(world);
        if (w == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (Calculable user : w.getAll(CalculableType.GROUP)) {
            result.add(user.getName());
        }
        return result;
    }

    @Override
    public List<String> getGroupUsers(String groupName) {
        List<String> result = new ArrayList<String>();
        for (World w : manager.getAllWorlds()) {
            for (Calculable u : w.getAll(CalculableType.USER)) {
                if (u.hasGroup(groupName)) {
                    result.add(u.getName());
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getGroupUsers(String groupName, String world) {
        World w = manager.getWorld(world);
        if (w == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (Calculable u : w.getAll(CalculableType.USER)) {
            if (u.hasGroup(groupName)) {
                result.add(u.getName());
            }
        }
        return result;
    }

    @Override
    public List<String> getGroupPermissions(String groupName) {
        List<String> result = new ArrayList<String>();
        for (World world : manager.getAllWorlds()) {
            Group g = world.getGroup(groupName);
            if (g == null) {
                continue;
            }
            result.addAll(g.getPermissionsAsString());
        }
        return result;
    }

    @Override
    public List<String> getGroupPermissions(String groupName, String world) {
        World w = manager.getWorld(world);
        if (w == null) {
            return null;
        }
        Group g = w.getGroup(groupName);
        if (g == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        result.addAll(g.getPermissionsAsString());
        return result;
    }

    @Override
    public List<String> getAllGroupPermissions(String groupName) {
        List<String> result = new ArrayList<String>();
        for (World w : manager.getAllWorlds()) {
            Group g = w.getGroup(groupName);
            if (g == null) {
                continue;
            }
            result.addAll(g.getPermissionsAsString());
        }
        return result;
    }

    @Override
    public List<String> getUserPermissions(String userName) {
        List<String> result = new ArrayList<String>();
        for (World w : manager.getAllWorlds()) {
            User u = w.getUser(userName);
            if (u == null) {
                continue;
            }
            result.addAll(u.getPermissionsAsString());
        }
        return result;
    }

    @Override
    public List<String> getUsersWithPermission(String permission) {
        List<String> result = new ArrayList<String>();
        for (World w : manager.getAllWorlds()) {
            for (Calculable u : w.getAll(CalculableType.USER)) {
                if (u.hasPermission(permission)) {
                    result.add(u.getName());
                }
            }
        }
        return result;
    }

    @Override
    public boolean userHasPermission(String username, String permission,
            String world) {
        return ApiLayer.hasPermission(world, CalculableType.USER, username, permission);
    }

    @Override
    public List<String> getWorldsUserHasPermission(String username,
            String permission) {
        List<String> result = new ArrayList<String>();
        for (World w : manager.getAllWorlds()) {
            User u = w.getUser(username);
            if (u.hasPermission(permission)) {
                result.add(w.getName());
            }
        }
        return result;
    }

}
