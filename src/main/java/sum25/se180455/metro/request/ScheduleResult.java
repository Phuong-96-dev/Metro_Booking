package sum25.se180455.metro.request;

import sum25.se180455.metro.pojo.TrainSchedule;

import java.time.LocalTime;

public class ScheduleResult {
    private TrainSchedule schedule;
    private LocalTime departTime;
    private LocalTime arrivalTime;
    private long travelMinutes;

    public ScheduleResult(TrainSchedule schedule, LocalTime departTime, LocalTime arrivalTime, long travelMinutes) {
        this.schedule = schedule;
        this.departTime = departTime;
        this.arrivalTime = arrivalTime;
        this.travelMinutes = travelMinutes;
    }

    public TrainSchedule getSchedule() {
        return schedule;
    }

    public LocalTime getDepartTime() {
        return departTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public long getTravelMinutes() {
        return travelMinutes;
    }
}
