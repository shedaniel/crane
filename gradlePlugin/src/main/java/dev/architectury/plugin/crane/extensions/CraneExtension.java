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

package dev.architectury.plugin.crane.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.io.File;

public class CraneExtension {
    private final Project project;
    private Property<String> minecraftVersion;
    
    public CraneExtension(Project project) {
        this.project = project;
        ObjectFactory objects = project.getObjects();
        this.minecraftVersion = objects.property(String.class);
    }
    
    public Property<String> getMinecraftVersion() {
        return minecraftVersion;
    }
    
    public File getMergedJar() {
        File file = new File(project.getBuildDir(), "manifest/" + getMinecraftVersion().get() + "/merged.jar");
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
    
    public File getMappedMergedJar() {
        File file = new File(project.getBuildDir(), "manifest/" + getMinecraftVersion().get() + "/mapped-merged.jar");
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
    
    public File getExportedFile() {
        File file = new File(project.getBuildDir(), "export/" + getMinecraftVersion().get() + ".tiny");
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
    
    public Directory getMappingsDir() {
        return project.getLayout().getProjectDirectory().dir("mappings");
    }
}
