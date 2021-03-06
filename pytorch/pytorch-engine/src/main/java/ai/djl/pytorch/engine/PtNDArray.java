/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.pytorch.engine;

import ai.djl.Device;
import ai.djl.ndarray.Matrix;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.index.NDIndexFullSlice;
import ai.djl.ndarray.internal.NDFormat;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.ndarray.types.SparseFormat;
import ai.djl.pytorch.jni.JniUtils;
import ai.djl.pytorch.jni.NativeResource;
import ai.djl.pytorch.jni.Pointer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Predicate;

/** {@code PtNDArray} is the PyTorch implementation of {@link NDArray}. */
public class PtNDArray extends NativeResource implements NDArray {

    private static final int MAX_SIZE = 100;
    private static final int MAX_DEPTH = 10;
    private static final int MAX_ROWS = 10;
    private static final int MAX_COLUMNS = 20;

    private String name;
    private Device device;
    private DataType dataType;
    private Shape shape;
    private SparseFormat sparseFormat;
    private PtNDManager manager;
    private PtNDArrayEx ptNDArrayEx;

    /**
     * Constructs an PtTorch from a native handle and metadata (internal. Use {@link NDManager}
     * instead).
     *
     * @param manager the manager to attach the new array to
     * @param handle the pointer to the native PtTorch memory
     * @param device the device the new array will be located on
     * @param shape the shape of the new array
     * @param dataType the dataType of the new array
     */
    PtNDArray(PtNDManager manager, Pointer handle, Device device, Shape shape, DataType dataType) {
        this(manager, handle);
        this.device = device;
        // shape check
        if (Arrays.stream(shape.getShape()).anyMatch(s -> s < 0)) {
            throw new IllegalArgumentException("The shape must be >= 0");
        }
        this.shape = shape;
        this.dataType = dataType;
    }

    /**
     * Constructs an PyTorch from a native handle (internal. Use {@link NDManager} instead).
     *
     * @param manager the manager to attach the new array to
     * @param handle the pointer to the native PyTorch memory
     */
    public PtNDArray(PtNDManager manager, Pointer handle) {
        super(handle);
        this.manager = manager;
        this.ptNDArrayEx = new PtNDArrayEx(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDManager getManager() {
        return manager;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public DataType getDataType() {
        if (dataType == null) {
            dataType = JniUtils.getDataType(this);
        }
        return dataType;
    }

    /** {@inheritDoc} */
    @Override
    public Device getDevice() {
        if (device == null) {
            device = JniUtils.getDevice(this);
        }
        return device;
    }

    /** {@inheritDoc} */
    @Override
    public Shape getShape() {
        if (shape == null) {
            shape = JniUtils.getShape(this);
        }
        return shape;
    }

    /** {@inheritDoc} */
    @Override
    public SparseFormat getSparseFormat() {
        if (sparseFormat == null) {
            sparseFormat = JniUtils.getSparseFormat(this);
        }
        return sparseFormat;
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toDevice(Device device, boolean copy) {
        return JniUtils.to(this, getDataType(), device, copy);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toType(DataType dataType, boolean copy) {
        return JniUtils.to(this, dataType, getDevice(), copy);
    }

    /** {@inheritDoc} */
    @Override
    public Matrix toMatrix() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void attachGradient() {}

    /** {@inheritDoc} */
    @Override
    public PtNDArray getGradient() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public ByteBuffer toByteBuffer() {
        return JniUtils.getByteBuffer(this);
    }

    /** {@inheritDoc} */
    @Override
    public void set(Buffer data) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void set(NDIndex index, NDArray value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void set(NDIndex index, Number value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public void setScalar(NDIndex index, Number value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray get(NDIndex index) {
        // TODO add full support of index and refactor NDIndex
        NDIndexFullSlice fullSlice = index.getAsFullSlice(getShape()).orElse(null);
        if (isScalar()) {
            return this;
        }
        return JniUtils.get(this, 0, fullSlice.getMin()[0]);
    }

    /** {@inheritDoc} */
    @Override
    public void copyTo(NDArray array) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray booleanMask(NDArray index, int axis) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray zerosLike() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray onesLike() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public boolean contentEquals(Number number) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public boolean contentEquals(NDArray other) {
        return JniUtils.contentEqual(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray eq(Number other) {
        return eq(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray eq(NDArray other) {
        return JniUtils.eq(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray neq(Number other) {
        return neq(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray neq(NDArray other) {
        return JniUtils.neq(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray gt(Number other) {
        return gt(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray gt(NDArray other) {
        return JniUtils.gt(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray gte(Number other) {
        return gte(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray gte(NDArray other) {
        return JniUtils.gte(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray lt(Number other) {
        return lt(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray lt(NDArray other) {
        return JniUtils.lt(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray lte(Number other) {
        return lte(numberToNDArray(other));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray lte(NDArray other) {
        return JniUtils.lte(this, (PtNDArray) other);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray add(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray add(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sub(Number n) {
        return JniUtils.sub(this, n.doubleValue());
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sub(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mul(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mul(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray div(Number n) {
        return JniUtils.div(this, n.doubleValue());
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray div(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mod(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mod(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray pow(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray pow(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray addi(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray addi(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray subi(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray subi(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray muli(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray muli(NDArray others) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray divi(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray divi(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray modi(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray modi(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray powi(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray powi(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray maximum(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray maximum(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray minimum(Number n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray minimum(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray all() {
        return JniUtils.all(toType(DataType.BOOLEAN, true));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray any() {
        return JniUtils.any(toType(DataType.BOOLEAN, true));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray none() {
        return JniUtils.none(toType(DataType.BOOLEAN, true));
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray neg() {
        return JniUtils.neg(this, false);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray negi() {
        return JniUtils.neg(this, true);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray abs() {
        return JniUtils.abs(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray square() {
        return JniUtils.sqrt(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray cbrt() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray floor() {
        return JniUtils.floor(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray ceil() {
        return JniUtils.ceil(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray round() {
        return JniUtils.round(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray trunc() {
        return JniUtils.trunc(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray exp() {
        return JniUtils.exp(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray log() {
        return JniUtils.log(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray log10() {
        return JniUtils.log10(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray log2() {
        return JniUtils.log2(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sin() {
        return JniUtils.sin(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray cos() {
        return JniUtils.cos(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tan() {
        return JniUtils.tan(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray asin() {
        return JniUtils.asin(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray acos() {
        return JniUtils.acos(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray atan() {
        return JniUtils.atan(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sinh() {
        return JniUtils.sinh(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray cosh() {
        return JniUtils.cosh(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tanh() {
        return JniUtils.tanh(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray asinh() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray acosh() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray atanh() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toDegrees() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toRadians() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray max() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray max(int[] axes, boolean keepDims) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray min() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray min(int[] axes, boolean keepDims) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sum() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sum(int[] axes, boolean keepDims) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray prod() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray prod(int[] axes, boolean keepDims) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mean() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray mean(int[] axes, boolean keepDims) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray trace(int offset, int axis1, int axis2) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public NDList split(int sections, int axis) {
        return JniUtils.split(this, sections, axis);
    }

    /** {@inheritDoc} */
    @Override
    public NDList split(int[] indices, int axis) {
        long[] longIndices = Arrays.stream(indices).asLongStream().toArray();
        return JniUtils.split(this, longIndices, axis);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray flatten() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray reshape(Shape shape) {
        return JniUtils.reshape(this, shape.getShape());
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray expandDims(long axis) {
        return JniUtils.unsqueeze(this, axis);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray squeeze() {
        return JniUtils.squeeze(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray squeeze(long axis) {
        return JniUtils.squeeze(this, axis);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray squeeze(long[] axes) {
        // TODO: add workaround for PyTorch
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray logicalAnd(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray logicalOr(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray logicalXor(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray logicalNot() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray argSort(long axis, boolean ascending) {
        if (!ascending) {
            throw new UnsupportedOperationException("Only support ascending!");
        }
        return JniUtils.argSort(this, axis, false);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sort() {
        return sort(-1);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray sort(int axis) {
        return JniUtils.sort(this, axis, false);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray softmax(long[] axes, double temperature) {
        if (temperature != 1.0) {
            throw new UnsupportedOperationException("PyTorch softmax didn't suuport temperature");
        }
        return JniUtils.softmax(this, axes[0], getDataType());
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray logSoftmax(long[] axes, double temperature) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray cumSum() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray cumSum(int axis) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray isInfinite() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray isNaN() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray createMask(NDIndex index) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray createMask(Predicate<Number> predicate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tile(long repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tile(int axis, long repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tile(long[] repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray tile(Shape desiredShape) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray repeat(long repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray repeat(int axis, long repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray repeat(long[] repeats) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray repeat(Shape desiredShape) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray dot(NDArray other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray clip(Number min, Number max) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray swapAxes(long axis1, long axis2) {
        return JniUtils.transpose(this, axis1, axis2);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray transpose() {
        long[] shapeArray = getShape().getShape();
        // reverse the long array in-place
        for (int i = 0; i < shapeArray.length / 2; i++) {
            long tmp = shapeArray[i];
            shapeArray[i] = shapeArray[shapeArray.length - i - 1];
            shapeArray[shapeArray.length - i - 1] = tmp;
        }
        return transpose(shapeArray);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray transpose(long... axes) {
        return JniUtils.permute(this, axes);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray broadcast(Shape shape) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray argMax() {
        return JniUtils.argMax(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray argMax(long axis) {
        return JniUtils.argMax(this, axis, false);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray argMin() {
        return JniUtils.argMin(this);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray argMin(long axis) {
        return JniUtils.argMin(this, axis, false);
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray percentile(Number percentile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray percentile(Number percentile, int[] axes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray median() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray median(int[] axes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toDense() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray toSparse(SparseFormat fmt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArray nonzero() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override
    public PtNDArrayEx getNDArrayInternal() {
        return ptNDArrayEx;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toDebugString(MAX_SIZE, MAX_DEPTH, MAX_ROWS, MAX_COLUMNS);
    }

    /**
     * Runs the debug string representation of this {@code NDArray}.
     *
     * @param maxSize the maximum elements to print out
     * @param maxDepth the maximum depth to print out
     * @param maxRows the maximum rows to print out
     * @param maxColumns the maximum columns to print out
     * @return the debug string representation of this {@code NDArray}
     */
    public String toDebugString(int maxSize, int maxDepth, int maxRows, int maxColumns) {
        if (isReleased()) {
            return "This array is already closed";
        }
        return NDFormat.format(this, maxSize, maxDepth, maxRows, maxColumns);
    }

    /**
     * Convert number into Scalar NDArray.
     *
     * <p>In PyTorch, this value will be considered as c10::Scalar type for all purposes.
     *
     * @param number the number used for conversion
     * @return the Scalar {@code NDArray}
     */
    private PtNDArray numberToNDArray(Number number) {
        if (number instanceof Integer) {
            return (PtNDArray) manager.create(number.intValue());
        } else if (number instanceof Float) {
            return (PtNDArray) manager.create(number.floatValue());
        } else if (number instanceof Double) {
            return (PtNDArray) manager.create(number.doubleValue());
        } else if (number instanceof Long) {
            return (PtNDArray) manager.create(number.longValue());
        } else if (number instanceof Byte) {
            return (PtNDArray) manager.create(number.byteValue());
        } else {
            throw new IllegalArgumentException("Short conversion not supported!");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PtNDArray) {
            return contentEquals((PtNDArray) obj);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        Pointer pointer = handle.getAndSet(null);
        if (pointer != null) {
            JniUtils.deleteNdArray(pointer);
            manager.detach(getUid());
            manager = null;
        }
    }
}
