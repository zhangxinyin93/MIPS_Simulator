import java.util.LinkedList;
import java.util.Queue;

/**
 * Reservation Station with 10 ALU entries
 */
public class ReservationStation {
    private Queue<ReservationStationEntry> reservationQueue;
    private int capacity;

    public ReservationStation() {
        this.reservationQueue = new LinkedList<>();
        this.capacity = 10;
    }

    public boolean add(ReservationStationEntry entry) {
        if(isFull()) {
            return false;
        }
        reservationQueue.add(entry);
        return true;
    }

    public ReservationStationEntry poll() {
        if(isEmpty()) return null;
        return reservationQueue.poll();
    }

    public Queue<ReservationStationEntry> getReservationQueue() {
        return reservationQueue;
    }

    public boolean isFull() {
        return reservationQueue.size() == capacity;
    }

    public boolean isEmpty() {
        return reservationQueue.size() == 0;
    }
}
