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

package com.antgroup.geaflow.dsl.runtime.traversal.data;

import com.antgroup.geaflow.common.binary.BinaryString;
import com.antgroup.geaflow.common.type.IType;
import com.antgroup.geaflow.dsl.common.data.Row;
import com.antgroup.geaflow.dsl.common.data.RowEdge;
import com.antgroup.geaflow.model.graph.edge.EdgeDirection;
import com.antgroup.geaflow.model.graph.edge.IEdge;
import java.util.Arrays;
import java.util.Objects;

public class FieldAlignEdge implements RowEdge {

    private final RowEdge baseEdge;

    private final int[] fieldMapping;

    public FieldAlignEdge(RowEdge baseEdge, int[] fieldMapping) {
        this.baseEdge = baseEdge;
        this.fieldMapping = fieldMapping;
    }

    @Override
    public Object getField(int i, IType<?> type) {
        int mappingIndex = fieldMapping[i];
        if (mappingIndex < 0) {
            return null;
        }
        return baseEdge.getField(mappingIndex, type);
    }

    @Override
    public void setValue(Row value) {
        baseEdge.setValue(value);
    }

    @Override
    public RowEdge withDirection(EdgeDirection direction) {
        return new FieldAlignEdge(baseEdge.withDirection(direction), fieldMapping);
    }

    @Override
    public RowEdge identityReverse() {
        return new FieldAlignEdge(baseEdge.identityReverse(), fieldMapping);
    }

    @Override
    public String getLabel() {
        return baseEdge.getLabel();
    }

    @Override
    public void setLabel(String label) {
        baseEdge.setLabel(label);
    }

    @Override
    public Object getSrcId() {
        return baseEdge.getSrcId();
    }

    @Override
    public void setSrcId(Object srcId) {
        baseEdge.setSrcId(srcId);
    }

    @Override
    public Object getTargetId() {
        return baseEdge.getTargetId();
    }

    @Override
    public void setTargetId(Object targetId) {
        baseEdge.setTargetId(targetId);
    }

    @Override
    public EdgeDirection getDirect() {
        return baseEdge.getDirect();
    }

    @Override
    public void setDirect(EdgeDirection direction) {
        baseEdge.setDirect(direction);
    }

    @Override
    public Row getValue() {
        return baseEdge.getValue();
    }

    @Override
    public IEdge<Object, Row> withValue(Row value) {
        return new FieldAlignEdge((RowEdge) baseEdge.withValue(value), fieldMapping);
    }

    @Override
    public IEdge<Object, Row> reverse() {
        return new FieldAlignEdge((RowEdge) baseEdge.reverse(), fieldMapping);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RowEdge)) {
            return false;
        }
        RowEdge that = (RowEdge) o;
        return Objects.equals(getSrcId(), that.getSrcId()) && Objects.equals(getTargetId(),
            that.getTargetId()) && getDirect() == that.getDirect() && Objects.equals(getBinaryLabel(), that.getBinaryLabel());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(baseEdge);
        result = 31 * result + Arrays.hashCode(fieldMapping);
        return result;
    }

    @Override
    public BinaryString getBinaryLabel() {
        return baseEdge.getBinaryLabel();
    }

    @Override
    public void setBinaryLabel(BinaryString label) {
        baseEdge.setBinaryLabel(label);
    }

    @Override
    public String toString() {
        return getSrcId() + "#" + getTargetId() + "#" + getBinaryLabel() + "#" + getDirect() + "#" + getValue();
    }
}
