/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import world.model.StaticThreat;
import world.model.Obstacle;
import config.NonStaticInitConfig;
import config.StaticInitConfig;
import java.util.Vector;
import uav.UAV;
import uav.UAVBase;
import util.ConflictCheckUtil;
import world.model.Target;

/**
 *
 * @author Yulin_Zhang
 */
public class World {

    /**
     * *
     * ----------environment settings----------------------------
     */
    //bound of the canvas x_left_up,y_left_up,x_right_down,y_right_down
    public static int bound_width = 800;
    public static int bound_height = 600;

    private int scout_num;
    private int enemy_num;
    private int static_threat_num;
    private int attacker_num;

    private float threat_radius = 100;
    private float attacker_patrol_range;

    //robot coordinates, robot_coordinates[1][0], robot_coordinates[1][1] represents the x, y coordinate of robot 1
    private UAVBase uav_base;
    private Vector<Obstacle> obstacles;
    private Vector<StaticThreat> static_threats;

    /**
     * * internal variables
     *
     */
    private Vector<UAV> attackers;
    private Vector<UAV> enemy_uavs;
    private Vector<UAV> scouts;
    private int time_step = 0;

    private float theta_increase_for_enemy_uav = (float) Math.PI / 40;

    public World(NonStaticInitConfig init_config) {
        initParameterFromInitConfig(init_config);
        initUAVs();
    }

 
    public void initParameterFromInitConfig(NonStaticInitConfig init_config) {
        this.bound_width = init_config.getBound_width();
        this.bound_height = init_config.getBound_height();

        this.attacker_num = init_config.getAttacker_num();
        this.scout_num = init_config.getScout_num();
        this.enemy_num = init_config.getEnemy_num();
        this.static_threat_num = init_config.getStatic_threat_num();

        this.threat_radius = init_config.getThreat_radius();
        this.attacker_patrol_range = init_config.getAttacker_patrol_range();

        this.static_threats = init_config.getStatic_threats();
        this.obstacles = init_config.getObstacles();
        this.uav_base = init_config.getUav_base();
    }

    private void initScoutsAndAttackers() {
        float[] uav_base_coordinate = uav_base.getCoordinate();
        int uav_base_height = uav_base.getBase_height();
        int uav_base_width = uav_base.getBase_width();
        float[] uav_base_center = new float[2];
        uav_base_center[0] = uav_base_coordinate[0] + uav_base_width / 2;
        uav_base_center[1] = uav_base_coordinate[1] + uav_base_height / 2;
        for (int i = 0; i < scout_num; i++) {
            UAV scout = new UAV(i, static_threats.get(0), StaticInitConfig.SIDE_A, uav_base_center, obstacles);
            scouts.add(scout);
        }
        for (int i = 0; i < attacker_num; i++) {
            UAV attacker = new UAV(i, StaticInitConfig.NON_ROLE, StaticInitConfig.SIDE_A, uav_base_center, obstacles);
            attackers.add(attacker);
        }
    }

    private void initEnemyUAV() {
        for (int i = 0; i < enemy_num; i++) {
            int target_to_protect = i % static_threat_num;
            float[] target_coordinates = static_threats.get(target_to_protect).getCoordinates();
            float[] attacker_coordinates = new float[2];
            float theta_from_target = (float) (Math.random() * Math.PI * 2);
            for (float dist = attacker_patrol_range; dist > 0; dist = dist / 2f) {
                attacker_coordinates[1] = target_coordinates[1] + dist * (float) Math.sin(theta_from_target);
                attacker_coordinates[0] = target_coordinates[0] + dist * (float) Math.cos(theta_from_target);
                if (attacker_coordinates[0] < bound_width) {
                    boolean available = !ConflictCheckUtil.checkPointInObstaclesAndThreats(obstacles, attacker_coordinates[0], attacker_coordinates[1]);
                    if (available) {
                        break;
                    }
                }
            }
            UAV enemy_uav = new UAV(i, static_threats.get(target_to_protect), StaticInitConfig.SIDE_B, attacker_coordinates, obstacles);
            enemy_uavs.add(enemy_uav);
        }
    }

    private void initUAVs() {
        this.scouts = new Vector<UAV>();
        this.attackers = new Vector<UAV>();
        this.enemy_uavs = new Vector<UAV>();
        initScoutsAndAttackers();
        initEnemyUAV();
    }

    private void updateScoutCoordinate() {
        int i = 1;
        for (UAV scout : this.scouts) {
            boolean moved = scout.moveToNextWaypoint();
            Target uav_target = scout.getRole_target();
            if (moved || uav_target == null) {
                continue;
            }
            while (!moved) {
                System.out.println("generate path and previous size=" + scout.getPath_prefound().size());
//                scout.runRRTStar();
                scout.runRRT();
                moved = scout.moveToNextWaypoint();
            }
        }
    }

//    private void updateEnemyUAVCoordinate() {
//        for (UAV enemy : this.enemy_uavs) {
//            float theta_from_target = enemy.getTheta_around_target_for_enemy_uav();
//            theta_from_target += theta_increase_for_enemy_uav;
//            enemy.setTheta_around_target_for_enemy_uav(theta_from_target);
//            float[] attacker_coordinates = new float[2];
//            attacker_coordinates[0] = enemy.getCenter_coordinates()[0];
//            attacker_coordinates[1] = enemy.getCenter_coordinates()[1];
//            float[] target_coordinates = enemy.getRole_target().getCoordinates();
//            for (float dist = attacker_patrol_range; dist > 0; dist = dist / 2) {
//                attacker_coordinates[1] = target_coordinates[1] + dist * (float) Math.sin(theta_from_target);
//                attacker_coordinates[0] = target_coordinates[0] + dist * (float) Math.cos(theta_from_target);
//                if (attacker_coordinates[0] < bound_width) {
//                    boolean available = !ConflictCheckUtil.checkPointInObstaclesAndThreats(obstacles,  attacker_coordinates[0], attacker_coordinates[1]);
//                    if (available) {
//                        break;
//                    }
//                }
//            }
//            enemy.moveTo(attacker_coordinates[0], attacker_coordinates[1]);
//        }
//    }

    private void detectEvent() {
    }

    private void registerInfoRequirement() {
    }

    private void shareInfoAfterRegistration() {
    }

    public void updateAll() {
        detectEvent();
        registerInfoRequirement();
        shareInfoAfterRegistration();
        updateScoutCoordinate();
//        updateEnemyUAVCoordinate();
        this.time_step++;
        System.out.println("timestep=" + this.time_step);
    }

    public Vector<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(Vector<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public Vector<UAV> getAttackers() {
        return attackers;
    }

    public void setAttackers(Vector<UAV> attackers) {
        this.attackers = attackers;
    }

    public Vector<UAV> getScouts() {
        return scouts;
    }

    public void setScouts(Vector<UAV> scouts) {
        this.scouts = scouts;
    }

    public int getBound_width() {
        return bound_width;
    }

    public int getBound_height() {
        return bound_height;
    }

    public float getThreat_radius() {
        return threat_radius;
    }

    public float getAttacker_patrol_range() {
        return attacker_patrol_range;
    }

    public UAVBase getUav_base() {
        return uav_base;
    }

    public Vector<StaticThreat> getTargets() {
        return static_threats;
    }

    public Vector<UAV> getEnemy_uavs() {
        return enemy_uavs;
    }

}