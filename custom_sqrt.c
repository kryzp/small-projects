#include <stdio.h>

// just an exercise to check i understand newtons method
// literally took 10 seconds

double square_root(double x) {
    double approx = x / 2.0;
    for (int i = 0; i < 10; i++) {
        approx = 0.5 * ((x / approx) + approx);
    }
    return approx;
}

int main(void) {
    double num = 1000.0;
    double square_rooted = square_root(num);
    printf("%f\n", square_rooted);
    return 0;
}

