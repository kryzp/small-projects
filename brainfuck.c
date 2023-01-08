
/*
 * Brainfuck interpreter written in C
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef char byte;

#define MEMORY_BYTES_ALLOCATED 1024

static byte* GLOBAL_POINTER = 0;
static FILE* SOURCE_FILE = 0;

static void parse_instruction(char instruction);

int main(int argc, char** argv) {
    if (argc < 2) {
        printf("You need to also supply to input file name as an argument (e.g: ./bfi my_program.bf)\n");
        return -1;
    }
    char* source_filename = argv[1];
	SOURCE_FILE = fopen(source_filename, "r");
    if (!SOURCE_FILE) {
        printf("The source file could not be opened!\n");
        return -1;
    }
    printf("File loaded in...\n");
    byte* memory = malloc(MEMORY_BYTES_ALLOCATED);
    memset(memory, 0, MEMORY_BYTES_ALLOCATED);
    GLOBAL_POINTER = memory;
    printf("Memory allocated...\n");
    char instruction;
    do {
        instruction = fgetc(SOURCE_FILE);
		parse_instruction(instruction);
    } while (instruction != EOF);
    fclose(SOURCE_FILE);
    free(memory);
    return 0;
}

static void parse_instruction(char i) {
    if        (i == '>') { // increment data pointer
        GLOBAL_POINTER++;
    } else if (i == '<') { // decrement data pointer
        GLOBAL_POINTER--;
    } else if (i == '+') { // increment byte at pointer
        (*GLOBAL_POINTER)++;
    } else if (i == '-') { // decrement byte at pointer
        (*GLOBAL_POINTER)--;
    } else if (i == '.') { // output byte at pointer
        putchar(*GLOBAL_POINTER);
    } else if (i == ',') { // accept one byte of input, storing byte at pointer
        (*GLOBAL_POINTER) = getchar();
    } else if (i == '[') { // if byte at pointer is zero, jump forward to command after matching ] command
        if ((*GLOBAL_POINTER) == 0) {
            while (fgetc(SOURCE_FILE) != ']');
			fgetc(SOURCE_FILE);
        }
    } else if (i == ']') { // if byte at pointer is non-zero, jump back to command after matching [ command
        if ((*GLOBAL_POINTER) != 0) {
			int n = 0;
			char c;
			do {
				fseek(SOURCE_FILE, -2, SEEK_CUR);
				c = fgetc(SOURCE_FILE);
				if (c == '[') {
					n++;
				} else if (c == ']') {
					n--;
				}
			} while (n != 1);
			fseek(SOURCE_FILE, -1, SEEK_CUR);
		}
    }
}
