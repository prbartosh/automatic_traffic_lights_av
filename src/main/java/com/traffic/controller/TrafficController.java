package com.traffic.controller;

import com.traffic.model.Intersection;
import com.traffic.model.Phase;

import java.util.List;

public interface TrafficController {
    List<String> step(Intersection intersection);
}
