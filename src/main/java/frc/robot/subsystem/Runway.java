package frc.robot.subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import frc.molib.dashboard.Entry;
import frc.molib.lights.DigitalLight;
import frc.molib.sensors.DigitalInput;
import frc.molib.utilities.Console;
import frc.robot.Robot;

/** Runway Subsystem: Handles transport of and scoring game pieces */
public class Runway {
    //Parent NetworkTable
    private static final NetworkTable tblRunway = Robot.tblSubsystem.getSubTable("Runway");

    //Dashboard Values
    private static final Entry<Boolean> entLoaded = new Entry<>(tblRunway, "Loaded");

    //Motors
    private static final VictorSPX mtrDirector = new VictorSPX(5);
    private static final TalonFX mtrReel_T = new TalonFX(6);
    private static final TalonFX mtrReel_B = new TalonFX(7);

    //Sensors
    private static final DigitalInput bmpLoaded = new DigitalInput(0, false);

    //LEDs
    private static final DigitalLight ledIndicator = new DigitalLight(PneumaticsModuleType.CTREPCM, 0);

    //Power Buffer Variables
    private static double mReelPower_T = 0.0;
    private static double mReelPower_B = 0.0;
    private static double mDirectorPower = 0.0;

////System-wide Control

    /** Private constructor to prevent individual instances from being created */
    private Runway() {}

    /** Runs once at Robot startup to configure subsystem components */
    public static void init() {
        Console.logMsg("Runway Subsystem Initializing...");

        //Motor Inversions
        mtrDirector.setInverted(true);
        mtrReel_T.setInverted(true);
        mtrReel_B.setInverted(true);

        //Motor Neutral Mode configuration
        mtrDirector.setNeutralMode(NeutralMode.Brake);
        mtrReel_T.setNeutralMode(NeutralModeValue.Coast);
        mtrReel_B.setNeutralMode(NeutralModeValue.Coast);

        Console.logMsg("Runway Initialization Complete");
    }

    /** Initialize Dashboard values */
    public static void initDashboard() {
        entLoaded.set(isLoaded());
    }

    /** Call regularly to push new values to Dashboard */
    public static void updateDashboard() {
        entLoaded.set(isLoaded());
    }

    /** Disable the entire subsystem */
    public static void disable() {
        disableDirector();
        disableReels();
        disableLEDs();
    }

////Sensors

    /**
     * Read whether the Runway is holding a game piece
     * @return True if there is a game piece present
     */
    public static boolean isLoaded() {
        return bmpLoaded.get();
    }

////Director

    /**
     * Apply power to the Director motor
     * @param power [-1.0 to 1.0]
     */
    public static void setDirectorPower(double power) {
        mDirectorPower = power;
    }

    /** Turn on the Director with the preset power to fire */
    public static void enableDirector() {
        setDirectorPower(1.0);
    }

    /** Turn on the Director in reverse */
    public static void reverseDirector() {
        setDirectorPower(-0.5);
    }

    /** Turn off the Director */
    public static void disableDirector() {
        setDirectorPower(0.0);
    }

////Reels

    /**
     * Apply power to the Reel motors individually
     * @param powerTop      [-1.0 to 1.0] Power to the top Reel motor
     * @param powerBottom   [-1.0 to 1.0] Power to the bottom Reel motor
     */
    public static void setReelPower(double powerTop, double powerBottom) {
        mReelPower_T = powerTop;
        mReelPower_B = powerBottom;
    }

    /**
     * Apply identical power to both Reel motors
     * @param power [-1.0 to 1.0] Power to both Reel motors
     */
    public static void setReelPower(double power) {
        setReelPower(power, power);
    }
    
    /** Turn on the Reels at the preset powers for our Speaker Shot */
    public static void enableReels_Speaker() {
        setReelPower(0.75, 0.85);
    }

    /** Turn on the Reels at the preset powers for our Amp Shot */
    public static void enableReels_Amp() {
        setReelPower(0.15, 0.20);
    }

    /** Trun on the Reels in reverse */
    public static void reverseReels() {
        setReelPower(-0.05);
    }

    /** Turn off the Reels */
    public static void disableReels() {
        setReelPower(0.0, 0.0);
    }

////LEDs

    /** Turn on LEDs to indicate system is ready to intake game piece */
    public static void enableLEDs() {
        ledIndicator.turnOn();
    }

    /** Turn off LEDs to indicate system is not ready to intake game piece*/
    public static void disableLEDs() {
        ledIndicator.turnOff();
    }

//////////////////////////////////////////////////

	/** Call regularly to interpret sensor input, and apply buffered values to components. */
    public static void periodic() {
        //Sensor Saftey Automation
        if(isLoaded()) setDirectorPower(MathUtil.clamp(mDirectorPower, 0.0, 1.0));

        //Apply Buffer values
        mtrDirector.set(ControlMode.PercentOutput, mDirectorPower);
        mtrReel_T.set(mReelPower_T);
        mtrReel_B.set(mReelPower_B);
    }
}
