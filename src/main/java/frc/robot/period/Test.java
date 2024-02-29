package frc.robot.period;

import edu.wpi.first.networktables.NetworkTable;
import frc.molib.buttons.ButtonManager;
import frc.molib.hid.XboxController;
import frc.robot.Robot;
import frc.robot.subsystem.Chassis;
import frc.robot.subsystem.Hanger;
import frc.robot.subsystem.Runway;

/** Test Period: Handles human input to control the robot. Used for testing systems not yet suitable for Teleoperated */
@SuppressWarnings("unused")
public class Test {

    //Parent NetworkTable
    private static final NetworkTable tblTest = Robot.tblPeriod.getSubTable("Test");

    //Controllers
    private static final XboxController ctlDriver = new XboxController(0);
    private static final XboxController ctlOperator = new XboxController(1);

    /** Private constructor to prevent individual instances from being created */
    private Test() {}

    /** Runs once when Teleoperated is enabled to read period options from the Dashboard */
    public static void init() {
        //Clear any flags on controls
        ButtonManager.clearFlags();
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {
        
    }

//////////////////////////////////////////////////
    
    /** Call regularly to update PIDs, interpret sensor input, and apply buffered values to components. */
    public static void periodic() {
        //Update Subsystems
        Chassis.periodic();
		Runway.periodic();
        Hanger.periodic();
    }
}
