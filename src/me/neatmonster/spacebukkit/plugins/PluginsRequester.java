/*
 * This file is part of SpaceBukkit (http://spacebukkit.xereo.net/).
 *
 * SpaceBukkit is free software: you can redistribute it and/or modify it under the terms of the
 * Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license as published by the Creative
 * Common organization, either version 3.0 of the license, or (at your option) any later version.
 *
 * SpaceRTK is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA) license for more details.
 *
 * You should have received a copy of the Attribution-NonCommercial-ShareAlike Unported (CC BY-NC-SA)
 * license along with this program. If not, see <http://creativecommons.org/licenses/by-nc-sa/3.0/>.
 */
package me.neatmonster.spacebukkit.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import me.neatmonster.spacebukkit.SpaceBukkit;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

/**
 * Requests the plugins from BukGet
 */
public class PluginsRequester implements Runnable {

    private static final String PLUGINS_URL = "http://api.bukget.org/3/plugins/";

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            final URLConnection connection = new URL(PLUGINS_URL).openConnection();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();

            List<JSONObject> apiResponse = (JSONArray) JSONValue.parse(stringBuffer.toString());

            // TODO Plugins with no name
            for (JSONObject obj : apiResponse) {
                String name = (String) obj.get("plugin_name");
                if (!name.isEmpty()) PluginsManager.pluginsNames.add(name);
            }

            SpaceBukkit.getInstance().getLogger().info("Database contains " + PluginsManager.pluginsNames.size() + " plugins.");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
