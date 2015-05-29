/* 
 * Copyright (c) Yulin Zhang
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package world.uav;

import config.GraphicConfig;
import java.awt.Color;
import java.awt.Rectangle;
import util.DistanceUtil;
import util.RectangleUtil;
import util.VectorUtil;
import world.model.Target;
import world.model.shape.Circle;

/** UAV is the common features for both scouts and attackers.
 *
 * @author Yulin_Zhang
 */
public class UAV extends Unit {

    protected Color center_color;
    protected Color radar_color; //the radar color in world
    protected Circle uav_radar;
    protected double current_angle = 0;
    protected double max_angle =0;
    protected int speed = 5;
    protected boolean visible=true;
    protected float remained_energy=2000;
    
    
    
    public UAV(int index, Target target_indicated_by_role, int uav_type, float[] center_coordinates,float remained_energy) {
        super(index, target_indicated_by_role, uav_type, center_coordinates);
        this.remained_energy=remained_energy;
    }

    /** update the coordinate of center and radar of the scout according to its new coordinate.
     *
     * @param center_coordinate_x
     * @param center_coordinate_y
     */
    public void moveTo(float center_coordinate_x, float center_coordinate_y) {
        float moved_dist=DistanceUtil.distanceBetween(this.center_coordinates, new float[]{center_coordinate_x,center_coordinate_y});
        this.remained_energy-=moved_dist;
        uav_center.setCoordinate(center_coordinate_x, center_coordinate_y);
        uav_radar.setCoordinate(center_coordinate_x, center_coordinate_y);
        this.setCenter_coordinates(uav_radar.getCenter_coordinates());
    }

    /** extend Toward Goad considering the dynamics of the uav.
     * 
     * @param current_coordinate
     * @param current_angle
     * @param random_goal_coordinate
     * @param max_length
     * @param max_angle
     * @return 
     */
    protected float[] extendTowardGoalWithDynamics(float[] current_coordinate, double current_angle, float[] random_goal_coordinate, float max_length, double max_angle) {
        double toward_goal_angle = VectorUtil.getAngleOfVectorRelativeToXCoordinate(random_goal_coordinate[0] - current_coordinate[0], random_goal_coordinate[1] - current_coordinate[1]);
        double delta_angle = VectorUtil.getBetweenAngle(toward_goal_angle, current_angle);
        float[] new_node_coord = new float[2];
        if (delta_angle > max_angle) {
            double temp_goal_angle1 = VectorUtil.getNormalAngle(current_angle - max_angle);
            double delta_angle_1 = VectorUtil.getBetweenAngle(toward_goal_angle, temp_goal_angle1);

            double temp_goal_angle2 = VectorUtil.getNormalAngle(current_angle + max_angle);
            double delta_angle_2 = VectorUtil.getBetweenAngle(toward_goal_angle, temp_goal_angle2);

            if (delta_angle_1 < delta_angle_2) {
                toward_goal_angle = temp_goal_angle1;
            } else {
                toward_goal_angle = temp_goal_angle2;
            }
        }
        new_node_coord[0] = current_coordinate[0] + (float) (Math.cos(toward_goal_angle) * max_length);
        new_node_coord[1] = current_coordinate[1] + (float) (Math.sin(toward_goal_angle) * max_length);
        return new_node_coord;
    }

    /** check whether obstacle is in the rectangle, which is constructed by the current location of this uav and its target location.
     * 
     * @param obs_mbr
     * @return 
     */
    public boolean isObstacleInTargetMBR(Rectangle obs_mbr)
    {
        float[] target_coord=this.getTarget_indicated_by_role().getCoordinates();
        Rectangle rect = RectangleUtil.findMBRRect(this.center_coordinates, target_coord);
        if(rect.intersects(obs_mbr))
        {
            return true;
        }else{
            return false;
        }
    }
    
    public Circle getUav_radar() {
        return uav_radar;
    }

    public void setUav_radar(Circle uav_radar) {
        this.uav_radar = uav_radar;
    }

    public void initColor(int uav_index) {
        center_color = GraphicConfig.uav_colors.get(uav_index);
        radar_color = new Color(center_color.getRed(), center_color.getGreen(), center_color.getBlue(), 128);
    }

    public Color getCenter_color() {
        return center_color;
    }

    public Color getRadar_color() {
        return radar_color;
    }

    public double getCurrent_angle() {
        return current_angle;
    }

    public void setCurrent_angle(double current_angle) {
        this.current_angle = current_angle;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    
}
