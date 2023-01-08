
/*
 * Does polynomial differentiation and integration through use of
 * an infinate matrix. Remember, integration here is literally the
 * just multiplying by the inverse differentiation matrix!
 */

#include <stdio.h>

void differentiate(double* poly, double* out, int len) {
    for (int i = 0; i < len-1; i++) {
        out[i] = poly[i+1] * (double)(i+1);
    }
}

void integrate(double* poly, double* out, int len) {
    for (int i = 1; i < len; i++) {
        out[i] = poly[i-1] / (double)i;
    }
}

int main(int argc, char** argv) {
    const unsigned SIZE = 4;

    double poly[SIZE] = { 1.0, 1.0, 1.0, 0.0 };
    double diffd[SIZE] = {0};
    double intgd[SIZE] = {0};

    differentiate(poly, diffd, SIZE);
    integrate(poly, intgd, SIZE);

    for (int i = 0; i < SIZE; i++) {
        printf("%.2f x^%d | d/dx=%.2f x^%d, int=%.2f x^%d\n", poly[i], i, diffd[i], i, intgd[i], i);
    }

    return 0;
}

