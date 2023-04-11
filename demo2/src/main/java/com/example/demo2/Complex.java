package com.example.demo2;

public class Complex {

    double real;
    double imaginary;

    Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    static Complex add(Complex a, Complex b) {
        return new Complex(a.real + b.real, a.imaginary + b.imaginary);
    }

    static Complex square(Complex a) {
        return new Complex(Math.pow(a.real, 2) - Math.pow(a.imaginary, 2), 2 * a.real * a.imaginary);
    }
    
    double magnitude(){
        return Math.sqrt(Math.pow(real, 2) + Math.pow(imaginary, 2));
    }

    boolean equals(Complex a) {
        if (real == a.real && imaginary == a.imaginary) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Complex clone() {
        return new Complex(real, imaginary);
    }

    @Override
    public String toString() {
        if (imaginary >= 0) {
            return "" + real + " + " + imaginary + "i";
        } else {
            return "" + real + " - " + -imaginary + "i";
        }
    }

}
