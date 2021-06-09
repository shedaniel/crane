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

import cuchaz.enigma.command.MapSpecializedMethodsCommand;
import dev.architectury.mappingslayers.api.mutable.MutableTinyTree;
import dev.architectury.mappingslayers.api.utils.MappingsUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ExportTinyTask extends AbstractTask {
    private final RegularFileProperty minecraftJar;
    private final DirectoryProperty mappingsDir;
    private final RegularFileProperty output;
    
    public ExportTinyTask() {
        ObjectFactory objects = getProject().getObjects();
        this.minecraftJar = objects.fileProperty();
        this.mappingsDir = objects.directoryProperty();
        this.output = objects.fileProperty();
    }
    
    @InputFile
    public RegularFileProperty getMinecraftJar() {
        return minecraftJar;
    }
    
    @InputDirectory
    public DirectoryProperty getMappingsDir() {
        return mappingsDir;
    }
    
    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }
    
    @TaskAction
    public void export() throws Throwable {
        File output = this.output.getAsFile().get();
        if (output.getParentFile() != null) {
            output.getParentFile().mkdirs();
        }
        output.delete();
        new MapSpecializedMethodsCommand().run(minecraftJar.get().getAsFile().getAbsolutePath(), "enigma",
                mappingsDir.get().getAsFile().getAbsolutePath(), "tinyv2:intermediary:named", output.getAbsolutePath());
        MutableTinyTree tree = MappingsUtils.deserializeFromString(FileUtils.readFileToString(output, StandardCharsets.UTF_8));
        tree = MappingsUtils.reorderNamespaces(tree, Collections.singletonList("named"));
        tree.getClassesMutable().removeIf(classDef -> {
            classDef.getMethodsMutable().removeIf(methodDef -> {
                return methodDef.getParametersMutable().isEmpty();
            });
            return classDef.getMethodsMutable().isEmpty();
        });
        output.delete();
        FileUtils.write(output, MappingsUtils.serializeToString(tree), StandardCharsets.UTF_8);
    }
}
