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

import dev.architectury.plugin.mojmapextensions.extensions.PluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public class MinecraftVersionBasedTask extends AbstractTask {
    private final Property<String> mcVersion;
    
    public MinecraftVersionBasedTask() {
        this.mcVersion = getProject().getObjects().property(String.class);
        this.mcVersion.convention(getProject().provider(() -> {
            PluginExtension extension = getExtension();
            return extension.getMinecraftVersion().get();
        }));
    }
    
    @Input
    public Property<String> getMcVersion() {
        return mcVersion;
    }
}
