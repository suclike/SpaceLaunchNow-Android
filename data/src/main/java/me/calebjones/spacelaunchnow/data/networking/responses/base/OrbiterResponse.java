package me.calebjones.spacelaunchnow.data.networking.responses.base;

import com.google.gson.annotations.SerializedName;

import me.calebjones.spacelaunchnow.data.models.spacelaunchnow.Orbiter;

public class OrbiterResponse {
    @SerializedName(value="results")
    private Orbiter[] orbiters;

    public Orbiter[] getOrbiters() {
        return orbiters;
    }
}
