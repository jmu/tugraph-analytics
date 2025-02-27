/*
 * Copyright 2023 AntGroup CO., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.antgroup.geaflow.console.biz.shared.convert;

import com.antgroup.geaflow.console.biz.shared.view.FunctionView;
import com.antgroup.geaflow.console.core.model.data.GeaflowFunction;
import com.antgroup.geaflow.console.core.model.file.GeaflowJarPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FunctionViewConverter extends DataViewConverter<GeaflowFunction, FunctionView> {

    @Autowired
    private RemoteFileViewConverter remoteFileViewConverter;

    @Override
    protected FunctionView modelToView(GeaflowFunction model) {
        FunctionView view = super.convert(model);
        view.setType(model.getType());
        view.setJarPackage(remoteFileViewConverter.convert(model.getJarPackage()));

        return view;
    }

    @Override
    protected GeaflowFunction viewToModel(FunctionView view) {
        GeaflowFunction geaflowFunction = super.viewToModel(view);
        geaflowFunction.setType(view.getType());
        return geaflowFunction;
    }

    public GeaflowFunction convert(FunctionView entity, GeaflowJarPackage jarPackage) {
        GeaflowFunction model = viewToModel(entity);
        model.setJarPackage(jarPackage);
        return model;
    }
}
