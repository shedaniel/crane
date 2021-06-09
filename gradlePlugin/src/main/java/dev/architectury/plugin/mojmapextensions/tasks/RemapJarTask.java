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

import dev.architectury.mappingslayers.api.utils.MappingsUtils;
import dev.architectury.plugin.mojmapextensions.util.TinyRemapperMappingsHelper;
import dev.architectury.tinyremapper.OutputConsumerPath;
import dev.architectury.tinyremapper.TinyRemapper;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class RemapJarTask extends AbstractTask {
    private final RegularFileProperty input;
    private final RegularFileProperty output;
    private final RegularFileProperty mappings;
    private final Property<String> from;
    private final Property<String> to;
    private final ConfigurableFileCollection classpath;
    
    public RemapJarTask() {
        ObjectFactory objects = getProject().getObjects();
        this.input = objects.fileProperty();
        this.output = objects.fileProperty();
        this.mappings = objects.fileProperty();
        this.from = objects.property(String.class);
        this.to = objects.property(String.class);
        this.classpath = objects.fileCollection();
    }
    
    @InputFile
    public RegularFileProperty getInput() {
        return input;
    }
    
    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }
    
    @InputFile
    public RegularFileProperty getMappings() {
        return mappings;
    }
    
    @Input
    public Property<String> getFrom() {
        return from;
    }
    
    @Input
    public Property<String> getTo() {
        return to;
    }
    
    @OutputFiles
    public ConfigurableFileCollection getClasspath() {
        return classpath;
    }
    
    @TaskAction
    public void remap() throws IOException {
        String mappings = FileUtils.readFileToString(getMappings().getAsFile().get(), StandardCharsets.UTF_8);
        TinyRemapper remapper = TinyRemapper.newRemapper().logger(getProject().getLogger()::lifecycle)
                .logUnknownInvokeDynamic(false)
                .threads(Runtime.getRuntime().availableProcessors())
                .withMappings(TinyRemapperMappingsHelper.create(MappingsUtils.deserializeFromString(mappings), from.get(), to.get(), false))
                .build();
        remapper.readClassPath(getClasspath().getFiles().stream().map(File::toPath).toArray(Path[]::new));
        Path input = getInput().getAsFile().get().toPath();
        remapper.readInputs(input);
        try (OutputConsumerPath consumerPath = new OutputConsumerPath.Builder(getOutput().getAsFile().get().toPath()).build()) {
            consumerPath.addNonClassFiles(input);
            remapper.apply(consumerPath);
        } finally {
            remapper.finish();
        }
    }
}
