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

import net.fabricmc.lorenztiny.TinyMappingsWriter;
import org.apache.commons.io.FileUtils;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.proguard.ProGuardReader;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CollectMojangMappingsTask extends MinecraftVersionBasedTask {
    private final RegularFileProperty clientMap;
    private final RegularFileProperty serverMap;
    private final RegularFileProperty output;
    
    public CollectMojangMappingsTask() {
        this.clientMap = getProject().getObjects().fileProperty();
        this.serverMap = getProject().getObjects().fileProperty();
        this.output = getProject().getObjects().fileProperty();
        this.output.convention(() -> {
            String mcVersion = this.getMcVersion().get();
            return new File(getProject().getBuildDir(), "manifest/" + mcVersion + "/mojmap.tiny");
        });
    }
    
    @InputFile
    public RegularFileProperty getClientMap() {
        return clientMap;
    }
    
    @InputFile
    public RegularFileProperty getServerMap() {
        return serverMap;
    }
    
    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }
    
    @TaskAction
    public void download() throws IOException {
        File output = this.output.getAsFile().get();
        if (output.getParentFile() != null) {
            output.getParentFile().mkdirs();
        }
        output.delete();
        saveTo(output, getClientMap().getAsFile().get().toPath(), getServerMap().getAsFile().get().toPath());
    }
    
    /*
     * This method is part of fabric-loom, licensed under the MIT License (MIT).
     *
     * Copyright (c) 2016, 2017, 2018 FabricMC
     */
    private void saveTo(File output, Path clientMappings, Path serverMappings) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            MappingSet mappingsSet = getMappingsSet(clientMappings, serverMappings);
            new TinyMappingsWriter(writer, "official", "named").write(mappingsSet);
            FileUtils.write(output, writer.toString(), StandardCharsets.UTF_8);
        }
    }
    
    /*
     * This method is part of fabric-loom, licensed under the MIT License (MIT).
     *
     * Copyright (c) 2016, 2017, 2018 FabricMC
     */
    private MappingSet getMappingsSet(Path clientMappings, Path serverMappings) throws IOException {
        MappingSet mappings = MappingSet.create();
        
        try (BufferedReader clientBufferedReader = Files.newBufferedReader(clientMappings, StandardCharsets.UTF_8);
             BufferedReader serverBufferedReader = Files.newBufferedReader(serverMappings, StandardCharsets.UTF_8)) {
            try (ProGuardReader proGuardReaderClient = new ProGuardReader(clientBufferedReader);
                 ProGuardReader proGuardReaderServer = new ProGuardReader(serverBufferedReader)) {
                proGuardReaderClient.read(mappings);
                proGuardReaderServer.read(mappings);
            }
        }
        
        return mappings.reverse();
    }
}
