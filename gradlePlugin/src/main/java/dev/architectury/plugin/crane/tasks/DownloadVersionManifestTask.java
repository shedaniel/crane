/*
 * This file is part of architectury.
 * Copyright (C) 2021 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package dev.architectury.plugin.crane.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DownloadVersionManifestTask extends MinecraftVersionBasedTask {
    private final RegularFileProperty output;
    
    public DownloadVersionManifestTask() {
        this.output = getProject().getObjects().fileProperty();
        this.output.convention(() -> {
            String mcVersion = this.getMcVersion().get();
            return new File(getProject().getBuildDir(), "manifest/" + mcVersion + "/manifest.json");
        });
    }
    
    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }
    
    @TaskAction
    public void download() throws IOException {
        String mcVersion = getMcVersion().get();
        File output = this.output.getAsFile().get();
        if (output.getParentFile() != null) {
            output.getParentFile().mkdirs();
        }
        output.delete();
        URL url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        JsonObject manifest;
        try (InputStream stream = url.openStream()) {
            manifest = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        List<String> versions = new ArrayList<>();
        for (JsonElement version : manifest.getAsJsonArray("versions")) {
            JsonObject versionObj = version.getAsJsonObject();
            String id = versionObj.getAsJsonPrimitive("id").getAsString();
            versions.add(id);
            if (id.equals(mcVersion)) {
                String versionUrl = versionObj.getAsJsonPrimitive("url").getAsString();
                FileUtils.copyURLToFile(new URL(versionUrl), output);
                return;
            }
        }
        throw new IllegalStateException("Unfound version: " + mcVersion + "; Found " + String.join(", ", versions));
    }
}
