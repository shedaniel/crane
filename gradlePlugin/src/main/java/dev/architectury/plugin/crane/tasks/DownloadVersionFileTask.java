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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DownloadVersionFileTask extends MinecraftVersionBasedTask {
    private final RegularFileProperty manifest;
    private final Property<String> id;
    private final Property<String> classifier;
    private final RegularFileProperty output;
    
    public DownloadVersionFileTask() {
        this.manifest = getProject().getObjects().fileProperty();
        this.id = getProject().getObjects().property(String.class);
        this.classifier = getProject().getObjects().property(String.class);
        this.classifier.convention("");
        this.output = getProject().getObjects().fileProperty();
        this.output.convention(() -> {
            String mcVersion = this.getMcVersion().get();
            return new File(getProject().getBuildDir(), "manifest/" + mcVersion + "/" + getId().get() + getClassifier().get());
        });
    }
    
    @InputFile
    public RegularFileProperty getManifest() {
        return manifest;
    }
    
    @Input
    public Property<String> getId() {
        return id;
    }
    
    @Input
    public Property<String> getClassifier() {
        return classifier;
    }
    
    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }
    
    @TaskAction
    public void download() throws IOException {
        JsonObject manifest;
        try (InputStream stream = FileUtils.openInputStream(getManifest().getAsFile().get())) {
            manifest = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        File output = this.output.getAsFile().get();
        if (output.getParentFile() != null) {
            output.getParentFile().mkdirs();
        }
        output.delete();
        JsonObject downloads = manifest.getAsJsonObject("downloads").getAsJsonObject(getId().get());
        String url = downloads.getAsJsonPrimitive("url").getAsString();
        FileUtils.copyURLToFile(new URL(url), output);
    }
}
