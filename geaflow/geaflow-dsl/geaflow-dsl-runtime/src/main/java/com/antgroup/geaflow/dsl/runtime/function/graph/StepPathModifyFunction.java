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

package com.antgroup.geaflow.dsl.runtime.function.graph;

import com.antgroup.geaflow.common.type.IType;
import com.antgroup.geaflow.dsl.common.data.Path;
import com.antgroup.geaflow.dsl.common.data.Row;
import com.antgroup.geaflow.dsl.common.data.RowVertex;
import com.antgroup.geaflow.dsl.common.data.StepRecord;
import com.antgroup.geaflow.dsl.common.data.impl.DefaultPath;
import com.antgroup.geaflow.dsl.common.types.VertexType;
import com.antgroup.geaflow.dsl.runtime.expression.Expression;
import com.antgroup.geaflow.dsl.runtime.expression.construct.VertexConstructExpression;
import com.antgroup.geaflow.dsl.runtime.traversal.TraversalRuntimeContext;
import com.antgroup.geaflow.dsl.runtime.traversal.collector.StepCollector;
import com.antgroup.geaflow.dsl.runtime.traversal.data.GlobalVariable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class StepPathModifyFunction implements StepMapFunction {

    private final int[] updatePathIndices;

    private final Expression[] modifyExpressions;

    private final IType<?>[] fieldTypes;

    private TraversalRuntimeContext context;


    public StepPathModifyFunction(int[] updatePathIndices,
                                  Expression[] modifyExpressions,
                                  IType<?>[] fieldTypes) {
        this.updatePathIndices = Objects.requireNonNull(updatePathIndices);
        this.modifyExpressions = Objects.requireNonNull(modifyExpressions);
        assert updatePathIndices.length == modifyExpressions.length;
        this.fieldTypes = Objects.requireNonNull(fieldTypes);
    }

    @Override
    public void open(TraversalRuntimeContext context, FunctionSchemas schemas) {
        this.context = context;
        for (Expression expression : modifyExpressions) {
            StepFunction.openExpression(expression, context);
        }
    }

    @Override
    public void finish(StepCollector<StepRecord> collector) {

    }

    @Override
    public Path map(Row record) {
        Row[] values = new Row[fieldTypes.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = (Row) record.getField(i, fieldTypes[i]);
        }
        for (int i = 0; i < updatePathIndices.length; i++) {
            Row value = (Row) modifyExpressions[i].evaluate(record);
            updateGlobalVariable(modifyExpressions[i], value);
            values[updatePathIndices[i]] = value;
        }
        return new DefaultPath(values);
    }

    private void updateGlobalVariable(Expression modifyExpression, Row value) {
        // modify global variable to vertex.
        if (modifyExpression instanceof VertexConstructExpression) {
            VertexConstructExpression  vertexConstruct = (VertexConstructExpression) modifyExpression;

            List<GlobalVariable> globalVariables = vertexConstruct.getGlobalVariables();
            for (int globalVarIndex = 0; globalVarIndex < globalVariables.size(); globalVarIndex++) {
                GlobalVariable gv = globalVariables.get(globalVarIndex);
                // index of the global variable
                int index = gv.getIndex();
                VertexType vertexType = ((VertexType) vertexConstruct.getOutputType());
                IType<?> fieldType = vertexType.getType(index);
                Object fieldValue = value.getField(index, fieldType);
                // add field to vertex which will affect all the computing with this vertexId
                context.addFieldToVertex(((RowVertex) value).getId(), globalVarIndex,
                    globalVariables.size(), fieldValue);
            }
        }
    }

    @Override
    public List<Expression> getExpressions() {
        return ImmutableList.copyOf(modifyExpressions);
    }

    @Override
    public StepFunction copy(List<Expression> expressions) {
        assert expressions.size() == this.modifyExpressions.length;
        return new StepPathModifyFunction(updatePathIndices, expressions.toArray(new Expression[]{}), fieldTypes);
    }
}
