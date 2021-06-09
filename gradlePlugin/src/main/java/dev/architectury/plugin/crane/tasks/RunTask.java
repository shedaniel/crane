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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.JavaExecSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunTask extends AbstractTask {
    private final RegularFileProperty minecraftJar;
    private final DirectoryProperty mappingsDir;
    private final List<Action<? super JavaExecSpec>> specs = new ArrayList<>();
    
    public RunTask() {
        ObjectFactory objects = getProject().getObjects();
        this.minecraftJar = objects.fileProperty();
        this.mappingsDir = objects.directoryProperty();
    }
    
    @InputFile
    public RegularFileProperty getMinecraftJar() {
        return minecraftJar;
    }
    
    @InputDirectory
    public DirectoryProperty getMappingsDir() {
        return mappingsDir;
    }
    
    public void apply(Action<? super JavaExecSpec> action) {
        this.specs.add(action);
    }
    
    @TaskAction
    public void invoke() {
        getProject().javaexec(spec -> {
            spec.classpath(getRuntimeClasspath(getProject()));
            spec.args("cuchaz.enigma.gui.Main");
            spec.args("--jar", escapePath(getMinecraftJar().getAsFile().get()));
            spec.args("--mappings", escapePath(getMappingsDir().getAsFile().get()));
            spec.args("--profile", "enigma-profile.json");
            spec.setMain("dev.architectury.crane.bootstrap.CraneBootstrapper");
            for (Action<? super JavaExecSpec> action : this.specs) {
                action.execute(spec);
            }
        }).assertNormalExitValue().rethrowFailure();
    }
    
    private String escapePath(File file) {
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.contains(" ")) return "\"" + absolutePath + "\"";
        return absolutePath;
    }
    
    private static FileCollection getRuntimeClasspath(Project project) {
        ConfigurationContainer configurations = project.getBuildscript().getConfigurations();
        DependencyHandler handler = project.getDependencies();
        return configurations.getByName("classpath")
                .plus(project.getRootProject().getBuildscript().getConfigurations().getByName("classpath"))
                .plus(configurations.detachedConfiguration(handler.localGroovy()));
    }
}
