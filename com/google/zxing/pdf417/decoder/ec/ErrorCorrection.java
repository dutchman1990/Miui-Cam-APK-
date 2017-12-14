package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;

public final class ErrorCorrection {
    private final ModulusGF field = ModulusGF.PDF417_GF;

    private int[] findErrorLocations(ModulusPoly modulusPoly) throws ChecksumException {
        int degree = modulusPoly.getDegree();
        int[] iArr = new int[degree];
        int i = 0;
        for (int i2 = 1; i2 < this.field.getSize() && i < degree; i2++) {
            if (modulusPoly.evaluateAt(i2) == 0) {
                iArr[i] = this.field.inverse(i2);
                i++;
            }
        }
        if (i == degree) {
            return iArr;
        }
        throw ChecksumException.getChecksumInstance();
    }

    private int[] findErrorMagnitudes(ModulusPoly modulusPoly, ModulusPoly modulusPoly2, int[] iArr) {
        int i;
        int degree = modulusPoly2.getDegree();
        int[] iArr2 = new int[degree];
        for (i = 1; i <= degree; i++) {
            iArr2[degree - i] = this.field.multiply(i, modulusPoly2.getCoefficient(i));
        }
        ModulusPoly modulusPoly3 = new ModulusPoly(this.field, iArr2);
        int length = iArr.length;
        int[] iArr3 = new int[length];
        for (i = 0; i < length; i++) {
            int inverse = this.field.inverse(iArr[i]);
            iArr3[i] = this.field.multiply(this.field.subtract(0, modulusPoly.evaluateAt(inverse)), this.field.inverse(modulusPoly3.evaluateAt(inverse)));
        }
        return iArr3;
    }

    private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly modulusPoly, ModulusPoly modulusPoly2, int i) throws ChecksumException {
        if (modulusPoly.getDegree() < modulusPoly2.getDegree()) {
            ModulusPoly modulusPoly3 = modulusPoly;
            modulusPoly = modulusPoly2;
            modulusPoly2 = modulusPoly3;
        }
        ModulusPoly modulusPoly4 = modulusPoly;
        ModulusPoly modulusPoly5 = modulusPoly2;
        ModulusPoly zero = this.field.getZero();
        ModulusPoly one = this.field.getOne();
        while (modulusPoly5.getDegree() >= i / 2) {
            ModulusPoly modulusPoly6 = modulusPoly4;
            ModulusPoly modulusPoly7 = zero;
            modulusPoly4 = modulusPoly5;
            zero = one;
            if (modulusPoly4.isZero()) {
                throw ChecksumException.getChecksumInstance();
            }
            modulusPoly5 = modulusPoly6;
            ModulusPoly zero2 = this.field.getZero();
            int inverse = this.field.inverse(modulusPoly4.getCoefficient(modulusPoly4.getDegree()));
            while (modulusPoly5.getDegree() >= modulusPoly4.getDegree() && !modulusPoly5.isZero()) {
                int degree = modulusPoly5.getDegree() - modulusPoly4.getDegree();
                int multiply = this.field.multiply(modulusPoly5.getCoefficient(modulusPoly5.getDegree()), inverse);
                zero2 = zero2.add(this.field.buildMonomial(degree, multiply));
                modulusPoly5 = modulusPoly5.subtract(modulusPoly4.multiplyByMonomial(degree, multiply));
            }
            one = zero2.multiply(zero).subtract(modulusPoly7).negative();
        }
        int coefficient = one.getCoefficient(0);
        if (coefficient != 0) {
            int inverse2 = this.field.inverse(coefficient);
            ModulusPoly multiply2 = one.multiply(inverse2);
            ModulusPoly multiply3 = modulusPoly5.multiply(inverse2);
            return new ModulusPoly[]{multiply2, multiply3};
        }
        throw ChecksumException.getChecksumInstance();
    }

    public int decode(int[] iArr, int i, int[] iArr2) throws ChecksumException {
        int i2;
        ModulusPoly modulusPoly = new ModulusPoly(this.field, iArr);
        int[] iArr3 = new int[i];
        Object obj = null;
        for (i2 = i; i2 > 0; i2--) {
            int evaluateAt = modulusPoly.evaluateAt(this.field.exp(i2));
            iArr3[i - i2] = evaluateAt;
            if (evaluateAt != 0) {
                obj = 1;
            }
        }
        if (obj == null) {
            return 0;
        }
        ModulusPoly one = this.field.getOne();
        if (iArr2 != null) {
            for (int length : iArr2) {
                int exp = this.field.exp((iArr.length - 1) - length);
                one = one.multiply(new ModulusPoly(this.field, new int[]{this.field.subtract(0, exp), 1}));
            }
        }
        ModulusPoly[] runEuclideanAlgorithm = runEuclideanAlgorithm(this.field.buildMonomial(i, 1), new ModulusPoly(this.field, iArr3), i);
        ModulusPoly modulusPoly2 = runEuclideanAlgorithm[0];
        ModulusPoly modulusPoly3 = runEuclideanAlgorithm[1];
        int[] findErrorLocations = findErrorLocations(modulusPoly2);
        int[] findErrorMagnitudes = findErrorMagnitudes(modulusPoly3, modulusPoly2, findErrorLocations);
        i2 = 0;
        while (i2 < findErrorLocations.length) {
            int length2 = (iArr.length - 1) - this.field.log(findErrorLocations[i2]);
            if (length2 >= 0) {
                iArr[length2] = this.field.subtract(iArr[length2], findErrorMagnitudes[i2]);
                i2++;
            } else {
                throw ChecksumException.getChecksumInstance();
            }
        }
        return findErrorLocations.length;
    }
}
