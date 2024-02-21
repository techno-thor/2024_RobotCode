package frc.robot.subsystem;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.networktables.NetworkTable;
import frc.molib.utilities.Console;
import frc.robot.Robot;

/** Hanger Subsystem: Handles climbing during the Endgame */
@SuppressWarnings("unused")
public class Hanger {
    //Parent NetworkTable
    private static final NetworkTable tblHanger = Robot.tblSubsystem.getSubTable("Hanger");

    //Motors
    private static final TalonFX mtrWinch_1 = new TalonFX(8);
    private static final TalonFX mtrWinch_2 = new TalonFX(9);

    //Constants
    private static final double MAX_HEIGHT = Double.POSITIVE_INFINITY;

    //Power Buffer Variables
    private static double mWinchPower = 0.0;

////System-wide Control

    /** Private constructor to prevent individual instances from being created */
    private Hanger() {}

    /** Runs once at Robot startup to configure subsystem components */
    public static void init() {
        Console.logMsg("Hanger Subsystem Initializing...");

        //Motor Inversions
        mtrWinch_1.setInverted(true);
        mtrWinch_2.setInverted(false);

        //Motor Neutral Mode configuration
        mtrWinch_1.setNeutralMode(NeutralModeValue.Brake);
        mtrWinch_2.setNeutralMode(NeutralModeValue.Brake);

        //Sensor Reset
        resetHeight();

        Console.logMsg("Hanger Initialization Complete");
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {

    }

    /** Call regularly to push new values to Dashboard */
    public static void updateDashboard() {

    }

    /** Disable the entire subsystem */
    public static void disable() {
        disableWinch();
    }

//Sensors

    /**
     * Get the approximate height of the Hanger
     * @return Height of the Hanger in inches
     */
    public static double getHeight() {
        //FIXME: Find real approximate conversion
        return mtrWinch_1.getPosition().getValue();
    }

    /** Reset Winch motor position to 0.0 */
    public static void resetHeight() {
        mtrWinch_1.setPosition(0.0);
    }

////Winch

    /**
     * Apply power to the Winch motors
     * @param power [-1.0 to 1.0]
     */
    public static void setWinchPower(double power) {
        mWinchPower = power;
    }

    /** Turn on the Winch motors to uncoil the hooks */
    public static void extendWinch() {
        setWinchPower(0.5);
    }

    /** Turn on the Winch motors in reverse to hang */
    public static void retractWinch() {
        setWinchPower(-0.5);
    }

    /** Turn off the Winch motors */
    public static void disableWinch() {
        setWinchPower(0.0);
    }

//////////////////////////////////////////////////

    /** Call regularly interpret sensor input and apply buffered values to components. */
    public static void periodic() {
        //Sensor Safety Automation
        if(getHeight() <= 0.0) mWinchPower = MathUtil.clamp(mWinchPower, 0.0, 1.0);
        if(getHeight() >= MAX_HEIGHT) mWinchPower = MathUtil.clamp(mWinchPower, -1.0, 0.0);

        //Apply Buffer values
        mtrWinch_1.set(mWinchPower);
        mtrWinch_2.set(mWinchPower);
    }
}
