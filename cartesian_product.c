#include <stdio.h>

#define ARRAY_LENGTH(_arr) (sizeof((_arr)) / sizeof((*_arr)))

int main(int argc, char** argv)
{
    int set_a[] = { 1, 2 };
    int set_b[] = { 4, 8 };

    for (int i = 0; i < ARRAY_LENGTH(set_a); i++) {
        for (int j = 0; j < ARRAY_LENGTH(set_b); j++) {
            printf("(%d, %d), ", set_a[i], set_b[j]);
        }
    }

    printf("\n");

    return 0;
}

