package com.fewstreet.iot_rc_controller;

/**
 * Created by wei on 4/18/17.
 */


public class CarControl {

    /**
     * steering and throttle are all from 0.0 to 1.0, 0.5 by default.
     *
     * I didn't put throttle and steering into one object, because the throttle and steering are not
     * used in c++. The data is directly send to RC-car when it is detected as controller data.
     * this will be faster.
     */
    public static final String controller = "controller";

    public String type_;
    public float throttle_;
    public float steering_;
    public long time_;

    public CarControl() {
        this.type_= controller;
        this.throttle_ = 0.5f;
        this.steering_ = 0.5f;
        this.time_ = System.currentTimeMillis();
    }
}
