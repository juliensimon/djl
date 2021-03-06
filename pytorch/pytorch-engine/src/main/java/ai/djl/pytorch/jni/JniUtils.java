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
package ai.djl.pytorch.jni;

import ai.djl.Device;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.ndarray.types.SparseFormat;
import ai.djl.pytorch.engine.PtDeviceType;
import ai.djl.pytorch.engine.PtNDArray;
import ai.djl.pytorch.engine.PtNDManager;
import ai.djl.pytorch.engine.PtSymbolBlock;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * A class containing utilities to interact with the PyTorch Engine's Java Native Interface (JNI)
 * layer.
 */
@SuppressWarnings("MissingJavadocMethod")
public final class JniUtils {

    private JniUtils() {}

    private static int layoutMapper(SparseFormat fmt) {
        if (fmt == SparseFormat.DENSE) {
            return 0;
        } else if (fmt == SparseFormat.UNDEFINED) {
            throw new UnsupportedOperationException("Type not supported");
        } else {
            return 1;
        }
    }

    // TODO: Unchecked Datatype and device mapping
    public static PtNDArray createNdFromByteBuffer(
            PtNDManager manager,
            ByteBuffer data,
            Shape shape,
            DataType dType,
            SparseFormat fmt,
            Device device) {
        int layoutVal = layoutMapper(fmt);
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchFromBlob(
                        data,
                        shape.getShape(),
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray createEmptyNdArray(
            PtNDManager manager, Shape shape, DataType dType, Device device, SparseFormat fmt) {
        int layoutVal = layoutMapper(fmt);
        // TODO: set default type of require gradient
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchEmpty(
                        shape.getShape(),
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray createZerosNdArray(
            PtNDManager manager, Shape shape, DataType dType, Device device, SparseFormat fmt) {
        int layoutVal = layoutMapper(fmt);
        // TODO: set default type of require gradient
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchZeros(
                        shape.getShape(),
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray createOnesNdArray(
            PtNDManager manager, Shape shape, DataType dType, Device device, SparseFormat fmt) {
        int layoutVal = layoutMapper(fmt);
        // TODO: set default type of require gradient
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchOnes(
                        shape.getShape(),
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray arange(
            PtNDManager manager,
            int start,
            int end,
            int step,
            DataType dType,
            Device device,
            SparseFormat fmt) {
        int layoutVal = layoutMapper(fmt);
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchArange(
                        start,
                        end,
                        step,
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray arange(
            PtNDManager manager,
            double start,
            double end,
            double step,
            DataType dType,
            Device device,
            SparseFormat fmt) {
        int layoutVal = layoutMapper(fmt);
        return new PtNDArray(
                manager,
                PyTorchLibrary.LIB.torchArange(
                        start,
                        end,
                        step,
                        dType.ordinal(),
                        layoutVal,
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        false));
    }

    public static PtNDArray to(PtNDArray ndArray, DataType dataType, Device device, boolean copy) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchTo(
                        ndArray.getHandle(),
                        dataType.ordinal(),
                        new int[] {PtDeviceType.toDeviceType(device), device.getDeviceId()},
                        copy));
    }

    public static PtNDArray get(PtNDArray ndArray, long dim, long start) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchGet(ndArray.getHandle(), dim, start));
    }

    public static PtNDArray reshape(PtNDArray ndArray, long[] shape) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchReshape(ndArray.getHandle(), shape));
    }

    public static PtNDArray stack(NDArray[] arrays, int dim) {
        Pointer[] pointers =
                Arrays.stream(arrays)
                        .map(array -> ((PtNDArray) array).getHandle())
                        .toArray(Pointer[]::new);
        return new PtNDArray(
                (PtNDManager) arrays[0].getManager(), PyTorchLibrary.LIB.torchStack(pointers, dim));
    }

    public static PtNDArray softmax(PtNDArray ndArray, long dim, DataType dTpe) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchSoftmax(ndArray.getHandle(), dim, dTpe.ordinal()));
    }

    public static PtNDArray argMax(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchArgMax(ndArray.getHandle()));
    }

    public static PtNDArray argMax(PtNDArray ndArray, long dim, boolean keepDim) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchArgMax(ndArray.getHandle(), dim, keepDim));
    }

    public static PtNDArray argMin(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchArgMin(ndArray.getHandle()));
    }

    public static PtNDArray argMin(PtNDArray ndArray, long dim, boolean keepDim) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchArgMin(ndArray.getHandle(), dim, keepDim));
    }

    public static PtNDArray argSort(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchArgSort(ndArray.getHandle()));
    }

    public static PtNDArray argSort(PtNDArray ndArray, long dim, boolean keepDim) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchArgSort(ndArray.getHandle(), dim, keepDim));
    }

    public static PtNDArray sort(PtNDArray ndArray, long dim, boolean descending) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchSort(ndArray.getHandle(), dim, descending));
    }

    public static PtNDArray permute(PtNDArray ndArray, long[] dims) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchPermute(ndArray.getHandle(), dims));
    }

    public static PtNDArray transpose(PtNDArray ndArray, long dim1, long dim2) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.torchTranspose(ndArray.getHandle(), dim1, dim2));
    }

    public static boolean contentEqual(PtNDArray ndArray1, PtNDArray ndArray2) {
        return PyTorchLibrary.LIB.contentEqual(ndArray1.getHandle(), ndArray2.getHandle());
    }

    public static PtNDArray sub(PtNDArray ndArray, double scalar) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSub(ndArray.getHandle(), scalar));
    }

    public static PtNDArray div(PtNDArray ndArray, double scalar) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchDiv(ndArray.getHandle(), scalar));
    }

    public static NDList split(PtNDArray ndArray, long size, long axis) {
        Pointer[] ndPtrs = PyTorchLibrary.LIB.torchSplit(ndArray.getHandle(), size, axis);
        NDList list = new NDList();
        for (Pointer ptr : ndPtrs) {
            list.add(new PtNDArray(ndArray.getManager(), ptr));
        }
        return list;
    }

    public static NDList split(PtNDArray ndArray, long[] indices, long axis) {
        Pointer[] ndPtrs = PyTorchLibrary.LIB.torchSplit(ndArray.getHandle(), indices, axis);
        NDList list = new NDList();
        for (Pointer ptr : ndPtrs) {
            list.add(new PtNDArray(ndArray.getManager(), ptr));
        }
        return list;
    }

    public static PtNDArray squeeze(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSqueeze(ndArray.getHandle()));
    }

    public static PtNDArray squeeze(PtNDArray ndArray, long dim) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSqueeze(ndArray.getHandle(), dim));
    }

    public static PtNDArray unsqueeze(PtNDArray ndArray, long dim) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchUnsqueeze(ndArray.getHandle(), dim));
    }

    public static PtNDArray abs(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchAbs(ndArray.getHandle()));
    }

    public static PtNDArray floor(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchFloor(ndArray.getHandle()));
    }

    public static PtNDArray ceil(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchCeil(ndArray.getHandle()));
    }

    public static PtNDArray round(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchRound(ndArray.getHandle()));
    }

    public static PtNDArray trunc(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchTrunc(ndArray.getHandle()));
    }

    public static PtNDArray exp(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchExp(ndArray.getHandle()));
    }

    public static PtNDArray log(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchLog(ndArray.getHandle()));
    }

    public static PtNDArray log10(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchLog10(ndArray.getHandle()));
    }

    public static PtNDArray log2(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchLog2(ndArray.getHandle()));
    }

    public static PtNDArray sin(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSin(ndArray.getHandle()));
    }

    public static PtNDArray cos(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchCos(ndArray.getHandle()));
    }

    public static PtNDArray tan(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchTan(ndArray.getHandle()));
    }

    public static PtNDArray asin(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchASin(ndArray.getHandle()));
    }

    public static PtNDArray acos(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchAcos(ndArray.getHandle()));
    }

    public static PtNDArray atan(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchAtan(ndArray.getHandle()));
    }

    public static PtNDArray sqrt(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSqrt(ndArray.getHandle()));
    }

    public static PtNDArray sinh(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchSinh(ndArray.getHandle()));
    }

    public static PtNDArray cosh(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchCosh(ndArray.getHandle()));
    }

    public static PtNDArray tanh(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchTanh(ndArray.getHandle()));
    }

    public static PtNDArray all(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchAll(ndArray.getHandle()));
    }

    public static PtNDArray any(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchAny(ndArray.getHandle()));
    }

    public static PtNDArray none(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.torchNone(ndArray.getHandle()));
    }

    public static PtNDArray eq(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(), PyTorchLibrary.LIB.torchEq(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray neq(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(),
                PyTorchLibrary.LIB.torchNeq(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray gt(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(), PyTorchLibrary.LIB.torchGt(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray gte(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(),
                PyTorchLibrary.LIB.torchGte(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray lt(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(), PyTorchLibrary.LIB.torchLt(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray lte(PtNDArray self, PtNDArray other) {
        return new PtNDArray(
                self.getManager(),
                PyTorchLibrary.LIB.torchLte(self.getHandle(), other.getHandle()));
    }

    public static PtNDArray neg(PtNDArray self, boolean inplace) {
        return new PtNDArray(
                self.getManager(), PyTorchLibrary.LIB.torchNeg(self.getHandle(), inplace));
    }

    public static PtNDArray normalize(PtNDArray ndArray, float[] mean, float[] std) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.normalize(ndArray.getHandle(), mean, std));
    }

    public static PtNDArray resize(PtNDArray ndArray, long[] size, boolean alignCorners) {
        return new PtNDArray(
                ndArray.getManager(),
                PyTorchLibrary.LIB.resize(ndArray.getHandle(), size, alignCorners));
    }

    public static PtNDArray toTensor(PtNDArray ndArray) {
        return new PtNDArray(
                ndArray.getManager(), PyTorchLibrary.LIB.toTensor(ndArray.getHandle()));
    }

    public static DataType getDataType(PtNDArray ndArray) {
        int dataType = PyTorchLibrary.LIB.torchDType(ndArray.getHandle());
        return DataType.values()[dataType];
    }

    public static Device getDevice(PtNDArray ndArray) {
        int[] device = PyTorchLibrary.LIB.torchDevice(ndArray.getHandle());
        return new Device(PtDeviceType.fromDeviceType(device[0]), device[1]);
    }

    public static SparseFormat getSparseFormat(PtNDArray ndArray) {
        int layout = PyTorchLibrary.LIB.torchLayout(ndArray.getHandle());
        if (layout == 0) {
            return SparseFormat.DENSE;
        } else if (layout == 1) {
            return SparseFormat.COO;
        } else {
            throw new UnsupportedOperationException("Unsupported data format");
        }
    }

    public static Shape getShape(PtNDArray ndArray) {
        return new Shape(PyTorchLibrary.LIB.torchSizes(ndArray.getHandle()));
    }

    public static ByteBuffer getByteBuffer(PtNDArray ndArray) {
        ByteBuffer bb = PyTorchLibrary.LIB.torchDataPtr(ndArray.getHandle());
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    public static void deleteNdArray(Pointer handle) {
        PyTorchLibrary.LIB.torchDeleteTensor(handle);
    }

    public static void deleteModule(PtSymbolBlock block) {
        PyTorchLibrary.LIB.torchDeleteModule(block.getHandle());
    }

    public static PtSymbolBlock loadModule(PtNDManager manager, Path path) {
        Pointer handle = PyTorchLibrary.LIB.moduleLoad(path.toString());
        return new PtSymbolBlock(manager, handle);
    }

    public static void enableInferenceMode(PtSymbolBlock block) {
        PyTorchLibrary.LIB.moduleEval(block.getHandle());
    }

    public static NDList moduleForward(PtSymbolBlock block, NDList inputs) {
        // TODO: reconsider the usage of IValue
        // Currently, only map Tensor to IValue
        Pointer[] iValueHandles =
                inputs.stream()
                        .map(
                                ele ->
                                        PyTorchLibrary.LIB.iValueCreateFromTensor(
                                                ((PtNDArray) ele).getHandle()))
                        .toArray(Pointer[]::new);
        NDArray result =
                new PtNDArray(
                        (PtNDManager) inputs.head().getManager(),
                        PyTorchLibrary.LIB.iValueToTensor(
                                PyTorchLibrary.LIB.moduleForward(
                                        block.getHandle(), iValueHandles)));
        return new NDList(result);
    }
}
