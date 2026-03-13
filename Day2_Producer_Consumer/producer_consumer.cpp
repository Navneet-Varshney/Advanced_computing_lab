#include <iostream>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>
#include <semaphore.h>
#include <cstdlib>
#include <ctime>
#include <chrono>

using namespace std;

int BUFFER_SIZE;

struct Shared {
    int in;
    int out;
    int buffer_count;

    sem_t mutex;
    sem_t empty;   
    sem_t full;    

    chrono::steady_clock::time_point start; 
};

Shared* sharedData;
int* buffer;

void log(const char* role, int id, const char* action, int item) {

    auto now = chrono::steady_clock::now();
    auto ms = chrono::duration_cast<chrono::milliseconds>
              (now - sharedData->start).count();

    cout << "[ " << ms << " ms ] "
         << role << id << " "
         << action << " "
         << item
         << " | Buffer count: "
         << sharedData->buffer_count
         << endl;
}


void producer(int id) {

    srand(time(NULL) ^ (getpid()));

    while (true) {

        int item = rand() % 1000;  

        sem_wait(&sharedData->empty);   // wait if buffer full
        sem_wait(&sharedData->mutex);   // enter critical section

        buffer[sharedData->in] = item;
        sharedData->in = (sharedData->in + 1) % BUFFER_SIZE;
        sharedData->buffer_count++;

        log("Producer ", id, "produced item", item);

        sem_post(&sharedData->mutex);   // leave critical section
        sem_post(&sharedData->full);    // increase full slots

        sleep(1);
    }
}

void consumer(int id) {

    while (true) {

        sem_wait(&sharedData->full);    // wait if buffer empty
        sem_wait(&sharedData->mutex);   // enter critical section

        int item = buffer[sharedData->out];
        sharedData->out = (sharedData->out + 1) % BUFFER_SIZE;
        sharedData->buffer_count--;

        log("Consumer ", id, "consumed item", item);

        sem_post(&sharedData->mutex);   // leave critical section
        sem_post(&sharedData->empty);   // increase empty slots

        sleep(2);
    }
}

int main() {

    cout << "Enter buffer size: ";
    cin >> BUFFER_SIZE;

    int p, c;

    cout << "Enter number of Producers: ";
    cin >> p;

    cout << "Enter number of Consumers: ";
    cin >> c;

    size_t totalSize = sizeof(Shared) + sizeof(int) * BUFFER_SIZE;

    void* ptr = mmap(
        NULL,
        totalSize,
        PROT_READ | PROT_WRITE,
        MAP_SHARED | MAP_ANONYMOUS,
        -1,
        0
    );

    if (ptr == MAP_FAILED) {
        perror("mmap failed");
        exit(EXIT_FAILURE);
    }

    sharedData = (Shared*) ptr;
    buffer = (int*)((char*)ptr + sizeof(Shared));

    sharedData->in = 0;
    sharedData->out = 0;
    sharedData->buffer_count = 0;
    sharedData->start = chrono::steady_clock::now();

    sem_init(&sharedData->mutex, 1, 1);
    sem_init(&sharedData->empty, 1, BUFFER_SIZE); 
    sem_init(&sharedData->full, 1, 0);

    // Create Producers
    for (int i = 0; i < p; i++) {
        pid_t pid = fork();
        if (pid == 0) {
            producer(i + 1);
            exit(0);
        }
    }

    // Create Consumers
    for (int i = 0; i < c; i++) {
        pid_t pid = fork();
        if (pid == 0) {
            consumer(i + 1);
            exit(0);
        }
    }
    
    for (int i = 0; i < p + c; i++) {
        wait(NULL);
    }

    return 0;
}