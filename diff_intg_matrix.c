
/*
 * Does polynomial differentiation and integration through use of
 * an infinate matrix. Remember, integration here is literally the
 * just multiplying by the inverse differentiation matrix!
 */

#include <stdio.h>

void differentiate(double* poly, double* out, int len) {
    for (int i = 0; i < len-1; i++) {
        for (int j = 0; j < len; j++) {
            out[i] += poly[j] * (i+1 == j ? j : 0.0);
        }
    }
}

void integrate(double* poly, double* out, int len) {
    for (int i = 0; i < len+1; i++) {
        for (int j = 0; j < len; j++) {
            out[i] += poly[j] * (i == j+1 ? 1.0/(double)i : 0.0);
        }
    }
}

int main(int argc, char** argv) {
    const unsigned SIZE = 4;

    double poly[] = { 1.0, 1.0, 1.0, 0.0 };
    double diffd[SIZE] = {0};
    double intgd[SIZE] = {0};

    differentiate(poly, diffd, SIZE);
    integrate(poly, intgd, SIZE);

    for (int i = 0; i < SIZE; i++) {
        printf("%.2f x^%d | d/dx=%.2f x^%d, int=%.2f x^%d\n", poly[i], i, diffd[i], i, intgd[i], i);
    }

    return 0;
}

