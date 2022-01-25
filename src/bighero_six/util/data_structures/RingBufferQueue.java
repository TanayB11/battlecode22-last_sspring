package bighero_six.util.data_structures;

public class RingBufferQueue {
    public int size;
    public int[] rbq;
    public int front_ptr;
    public int end_ptr;

    public RingBufferQueue(int sz) {
        this.size = sz;
        rbq = new int[sz];
        front_ptr = 0;
        end_ptr = 0;
    }

    public void enqueue(int val) {
        rbq[end_ptr] = val;
        end_ptr++;
        end_ptr %= size;
    }

    // Not used for calculating moving averages
//    public int dequeue() {
//        int val = rbq[front_ptr];
//        rbq[front_ptr] = 0;
//        front_ptr++;
//        front_ptr %= size;
//        return val;
//    }

    public double calcAverageVal() {
        int total = 0;
        for (int i = 0; i < size; i++) {
            total += rbq[i];
        }
        return (double)total/size;
    }
}
