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

package dev.architectury.plugin.crane.util;

import dev.architectury.tinyremapper.IMappingProvider;
import net.fabricmc.mapping.tree.*;

public class TinyRemapperMappingsHelper {
	private TinyRemapperMappingsHelper() { }

	private static IMappingProvider.Member memberOf(String className, String memberName, String descriptor) {
		return new IMappingProvider.Member(className, memberName, descriptor);
	}
    
    public static IMappingProvider create(TinyTree mappings, String from, String to, boolean remapLocalVariables) {
        return (acceptor) -> {
            for (ClassDef classDef : mappings.getClasses()) {
                String className = classDef.getName(from);
                acceptor.acceptClass(className, classDef.getName(to));
                
                for (FieldDef field : classDef.getFields()) {
                    acceptor.acceptField(memberOf(className, field.getName(from), field.getDescriptor(from)), field.getName(to));
                }
                
                for (MethodDef method : classDef.getMethods()) {
                    IMappingProvider.Member methodIdentifier = memberOf(className, method.getName(from), method.getDescriptor(from));
                    acceptor.acceptMethod(methodIdentifier, method.getName(to));
                    
                    if (remapLocalVariables) {
                        for (ParameterDef parameter : method.getParameters()) {
                            acceptor.acceptMethodArg(methodIdentifier, parameter.getLocalVariableIndex(), parameter.getName(to));
                        }
                        
                        for (LocalVariableDef localVariable : method.getLocalVariables()) {
                            acceptor.acceptMethodVar(methodIdentifier, localVariable.getLocalVariableIndex(),
                                    localVariable.getLocalVariableStartOffset(), localVariable.getLocalVariableTableIndex(),
                                    localVariable.getName(to));
                        }
                    }
                }
            }
        };
    }
}