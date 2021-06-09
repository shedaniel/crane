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

package dev.architectury.plugin.crane;

import dev.architectury.plugin.crane.extensions.CraneExtension;
import dev.architectury.plugin.crane.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class CranePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("crane", CraneExtension.class, project);
        
        setupTasks(project);
    }
    
    private void setupTasks(Project project) {
        TaskContainer tasks = project.getTasks();
        DownloadVersionManifestTask downloadManifest = tasks.create("downloadManifest", DownloadVersionManifestTask.class);
        DownloadLibrariesTask downloadLibraries = tasks.create("downloadLibraries", DownloadLibrariesTask.class, task -> {
            task.dependsOn(downloadManifest);
            task.getManifest().convention(downloadManifest.getOutput());
        });
        DownloadVersionFileTask downloadClientJar = tasks.create("downloadClientJar", DownloadVersionFileTask.class, task -> {
            task.dependsOn(downloadManifest);
            task.getManifest().convention(downloadManifest.getOutput());
            task.getId().set("client");
            task.getClassifier().set(".jar");
        });
        DownloadVersionFileTask downloadServerJar = tasks.create("downloadServerJar", DownloadVersionFileTask.class, task -> {
            task.dependsOn(downloadManifest);
            task.getManifest().convention(downloadManifest.getOutput());
            task.getId().set("server");
            task.getClassifier().set(".jar");
        });
        MergeJarsTask mergeJars = tasks.create("mergeJars", MergeJarsTask.class, task -> {
            task.dependsOn(downloadClientJar, downloadServerJar);
            task.getClient().convention(downloadClientJar.getOutput());
            task.getServer().convention(downloadServerJar.getOutput());
            task.getMerged().convention(() -> task.getExtension().getMergedJar());
        });
        DownloadVersionFileTask downloadClientMappings = tasks.create("downloadClientMappings", DownloadVersionFileTask.class, task -> {
            task.dependsOn(downloadManifest);
            task.getManifest().convention(downloadManifest.getOutput());
            task.getId().set("client_mappings");
            task.getClassifier().set(".map");
        });
        DownloadVersionFileTask downloadServerMappings = tasks.create("downloadServerMappings", DownloadVersionFileTask.class, task -> {
            task.dependsOn(downloadManifest);
            task.getManifest().convention(downloadManifest.getOutput());
            task.getId().set("server_mappings");
            task.getClassifier().set(".map");
        });
        CollectMojangMappingsTask collectMojangMappings = tasks.create("collectMojangMappings", CollectMojangMappingsTask.class, task -> {
            task.dependsOn(downloadClientMappings, downloadServerMappings);
            task.getClientMap().convention(downloadClientMappings.getOutput());
            task.getServerMap().convention(downloadServerMappings.getOutput());
        });
        RemapJarTask remapJar = tasks.create("remapJar", RemapJarTask.class, task -> {
            task.dependsOn(mergeJars, collectMojangMappings, downloadLibraries);
            task.getInput().convention(mergeJars.getMerged());
            task.getOutput().convention(() -> task.getExtension().getMappedMergedJar());
            task.getClasspath().from(downloadLibraries.getOutputDirectory().map(file -> file.getAsFile().listFiles()));
            task.getMappings().convention(collectMojangMappings.getOutput());
            task.getFrom().convention("official");
            task.getTo().convention("named");
        });
        tasks.create("run", RunTask.class, task -> {
            task.dependsOn(remapJar);
            task.getMinecraftJar().convention(remapJar.getOutput());
            task.getMappingsDir().convention(project.provider(() -> task.getExtension().getMappingsDir()));
        });
    }
}
