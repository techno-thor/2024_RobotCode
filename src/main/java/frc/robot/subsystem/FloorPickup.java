package frc.robot.subsystem;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.networktables.NetworkTable;
import frc.robot.Robot;

/** Floor Pickup Subsystem: Handles getting game pieces from the floor and delivering them to the Runway */
@SuppressWarnings("unused")
public class FloorPickup {
    //Parent NetworkTable
    private static final NetworkTable tblFloorPickup = Robot.tblSubsystem.getSubTable("Floor Pickup");

////System-wide Control
    
    /** Private constructor to prevent individual instances from being created */
    private FloorPickup() {}

    /** Runs once at Robot startup to configure subsystem components */
    public static void init() {

    }

    /** Initialize Dashboard values */
    public static void initDashboard() {

    }

    /** Call regularly to push new values to Dashboard */
    public static void updateDashboard() {

    }

    /** Disable the entire subsystem */
    public static void disable() {
        
    }

//////////////////////////////////////////////////

    /** Call regularly interpret sensor input and apply buffered values to components. */
    public static void periodic() {

    }
}
