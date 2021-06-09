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

import net.fabricmc.stitch.merge.JarMerger;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class MergeJarsTask extends AbstractTask {
	private final RegularFileProperty client;
	private final RegularFileProperty server;
	private final RegularFileProperty merged;

	public MergeJarsTask() {
		ObjectFactory objects = getProject().getObjects();
		this.client = objects.fileProperty();
		this.server = objects.fileProperty();
		this.merged = objects.fileProperty();
	}

	@InputFile
	public RegularFileProperty getClient() {
		return client;
	}

	@InputFile
	public RegularFileProperty getServer() {
		return server;
	}

	@OutputFile
	public RegularFileProperty getMerged() {
		return merged;
	}

	@TaskAction
	public void merge() throws IOException {
        File merged = this.getMerged().getAsFile().get();
        if (merged.getParentFile() != null) {
            merged.getParentFile().mkdirs();
        }
        merged.delete();
		try (JarMerger merger = new JarMerger(client.getAsFile().get(), server.getAsFile().get(), merged)) {
			merger.merge();
		}
	}
}
