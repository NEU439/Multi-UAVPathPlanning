/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world.model;

/**
 *
 * @author boluo
 */
public class Target extends EnvConstraint{
    protected float[] coordinates;
    protected int target_type=0;
    public Target(int index,float[] coordinates, int target_type) {
        super( index, null);
        this.coordinates=coordinates;
        this.target_type=target_type;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float[] coordinates) {
        this.coordinates = coordinates;
    }
}