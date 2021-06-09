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

package dev.architectury.plugin.mojmapextensions.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadLibrariesTask extends MinecraftVersionBasedTask {
    private final RegularFileProperty manifest;
    private final RegularFileProperty outputDirectory;
    
    public DownloadLibrariesTask() {
        this.manifest = getProject().getObjects().fileProperty();
        this.outputDirectory = getProject().getObjects().fileProperty();
        this.outputDirectory.convention(() -> {
            String mcVersion = this.getMcVersion().get();
            return new File(getProject().getBuildDir(), "manifest/" + mcVersion + "/libraries");
        });
    }
    
    @InputFile
    public RegularFileProperty getManifest() {
        return manifest;
    }
    
    @OutputDirectory
    public RegularFileProperty getOutputDirectory() {
        return outputDirectory;
    }
    
    @TaskAction
    public void download() throws Throwable {
        File outputDirectory = this.outputDirectory.getAsFile().get();
        try {
            JsonObject manifest;
            try (InputStream stream = FileUtils.openInputStream(getManifest().getAsFile().get())) {
                manifest = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            FileUtils.deleteDirectory(outputDirectory);
            outputDirectory.mkdirs();
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            JsonArray libraries = manifest.getAsJsonArray("libraries");
            try (ProgressBar progressBar = new ProgressBarBuilder()
                    .setConsumer(new DelegatingProgressBarConsumer(getProject().getLogger()::lifecycle))
                    .setInitialMax(libraries.size())
                    .setUpdateIntervalMillis(1000)
                    .setTaskName(":downloading libraries")
                    .setStyle(ProgressBarStyle.ASCII)
                    .showSpeed()
                    .build()) {
                for (JsonElement library : libraries) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            JsonObject libraryObj = library.getAsJsonObject();
                            JsonObject artifactObj = libraryObj.getAsJsonObject("downloads").getAsJsonObject("artifact");
                            String path = artifactObj.getAsJsonPrimitive("path").getAsString().replace('/', '-');
                            String url = artifactObj.getAsJsonPrimitive("url").getAsString();
                            File downloadTo = new File(outputDirectory, path);
                            FileUtils.copyURLToFile(new URL(url), downloadTo);
                            synchronized (progressBar) {
                                progressBar.step();
                            }
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }, service));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .exceptionally(throwable -> {
                            throw new RuntimeException(throwable);
                        })
                        .get();
            }
        } catch (Throwable throwable) {
            try {
                FileUtils.deleteDirectory(outputDirectory);
            } catch (Throwable t) {
                throwable.addSuppressed(t);
            }
            throw throwable;
        }
    }
}
